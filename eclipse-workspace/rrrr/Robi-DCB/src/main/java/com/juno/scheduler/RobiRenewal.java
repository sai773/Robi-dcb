package com.juno.scheduler;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.MDC;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.juno.controller.PurchaseController;
import com.juno.database.model.CpCallbacks;
import com.juno.database.model.Purchases;
import com.juno.database.model.Subscription;
import com.juno.database.service.SubscriptionInfoServiceImpl;
import com.juno.datapojo.PaymentResponse;
import com.juno.datapojo.PurchaseRequestDetail;
import com.juno.datapojo.RetryResponse;
import com.juno.datapojo.SmsInterval;
import com.juno.datapojo.SmsResponse;
import com.juno.datapojo.SubscriptionRetryHandler;
import com.juno.logs.CDRLogs;
import com.juno.logs.ErrorLogger;
import com.juno.logs.Logging;
import com.juno.redisRepo.PurchaseDetailValidationRepositoryImpl;
import com.juno.util.CommonHelper;
import com.juno.util.Constants;
import com.juno.util.InformContentProvider;

@Component
public class RobiRenewal implements Constants{

	@Autowired
	SubscriptionRetryHandler subRetry;
	
	@Autowired
	PurchaseDetailValidationRepositoryImpl purchaseDetailValidationRepositoryImpl;
	
	@Autowired
	PurchaseController pController;
	
	@Autowired
	SubscriptionInfoServiceImpl subscriptionInfoServiceImpl;

	@Autowired
	CommonHelper commonHelper;
	
	@Autowired
	InformContentProvider infocp;
	
	public static int graceCount;
	public static int parkCount;
	public static int graceRetryPerDay;
	public static int parkRetryPerDay;

	@Value("${GraceRetryPerDay}")
	public void setgraceRetryPerDay(int gracePerDayCount) {
		graceRetryPerDay = gracePerDayCount;
	}

	@Value("${ParkRetryPerDay}")
	public void setparkRetryPerDay(int parkPerDayCount) {
		parkRetryPerDay = parkPerDayCount;
	}

	@Value("${GraceDays}")
	public void setgraceDays(int graceDays) {
		graceCount = graceDays;
	}

	@Value("${ParkDays}")
	public void setparkDays(int parkDays) {
		parkCount = parkDays;
	}

	@Scheduled(cron = "${cron.sms.expression}", zone="Asia/Dhaka")
	public void remSMS() {
		Logging.getLogger().info("********* PreRenewal SMS Scheduler *********");
		try {
			pController.dataForPreRenewalSMS();
		} catch (UnsupportedEncodingException e) {
			ErrorLogger.getLogger().error("Exception in remSMS : "+ e.getMessage());
		}
	}

	@Scheduled(fixedDelay = 4500000, zone="Asia/Dhaka")
	//@Scheduled(fixedDelay = 5000, zone="Asia/Dhaka") //local testing
	public void graceParkRetryScheduler() {
		Logging.getLogger().info("********* GracePark Retry Scheduler *********");
		HttpServletRequest request = null;
		HttpServletResponse response = null;
		PaymentResponse payResp = null;
		RetryResponse rResp;
		String status = null;
		int SmsNotify = 0;
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<Subscription> subs = subscriptionInfoServiceImpl.SubscriptionFailedRetry();
		Logging.getLogger().info("&&&Fetching Grace/Park records from Subscription table : " + subs.size());
		if (subs != null && subs.size() != 0 && !subs.isEmpty()) {
			for (Subscription sub : subs) {
				PurchaseRequestDetail purSubDetails = null;
				try {
					Logging.getLogger().info("Subscription params from Subscription tables "
							+":requestId-"+sub.getPurchaseId()
							+":cptxnid-"+sub.getCpTxId()
							+":msisdn-"+sub.getMsisdn()
							+":price-"+sub.getAmount()
							+":action-"+sub.getAction()
							+":starttime-"+sub.getStartTime()
							+":endtime-"+sub.getEndTime()
							+":renewalstatus-"+sub.getRenewStatus()
							+":validity-"+sub.getValidity()
							+":retryCount-"+sub.getRetryCount()
							+":chrgStatus-"+sub.getCharged()
							+":retryTime-"+sub.getRetryTime()
							+":retryDay-"+sub.getRetryDays());

					Logging.getLogger().info("#########Performing SubRetry for Msisdn : " + sub.getMsisdn() + ", spid : "+sub.getSpId());

					Purchases purReq = subscriptionInfoServiceImpl.getUserPurchaseInfo(sub.getMsisdn(), sub.getPurchaseId(), sub.getServiceName());
					int session = (int) (Math.random()*9779) + 6000;
					String purId = Integer.toString(session) + sub.getPurchaseId();
					MDC.put("Robi-UNIQUE-ID", purId);
					purSubDetails = new PurchaseRequestDetail();
					purSubDetails.setSpid(sub.getSpId());
					purSubDetails.setCptxnid(sub.getCpTxId());
					purSubDetails.setPrice(purReq.getAmount());
					purSubDetails.setCurrency(purReq.getCur());
					purSubDetails.setSerName(sub.getServiceName());	
					purSubDetails.setSerProvName(sub.getSerProvName());
					purSubDetails.setValidity(sub.getValidity());
					purSubDetails.setPurchaseId(purId);

					if((sub.getAction().equalsIgnoreCase("act") && sub.getRetryDays() >= parkCount) || 
							(sub.getAction().equalsIgnoreCase("rnw") && sub.getRetryDays() >= graceCount)){
						purSubDetails.setAction("dct");
						purSubDetails.setNetType("sys-churn");
					} else {
						String actn = null;
						if(sub.getAction().equalsIgnoreCase("act")) {
							actn = "act_park";
						} else if(sub.getAction().equalsIgnoreCase("rnw") && sub.getRetryDays() < 1 && sub.getRetryCount() == 1){
							actn = "rnw";
						} else if(sub.getAction().equalsIgnoreCase("rnw") && sub.getRetryDays() >= 1){
							actn = "act_grace";
						} else {
							actn = (sub.getAction().equalsIgnoreCase("act")) ? "act_park" : "act_grace";
						}
						purSubDetails.setAction(actn);
						purSubDetails.setNetType(purReq.getNetype().equalsIgnoreCase("cellular") ? "cellular" : purReq.getNetype());
					}

					purSubDetails.setRequestTime(CommonHelper.getStrDate());
					purSubDetails.setMsisdn(sub.getMsisdn());

					if(purSubDetails.getAction().equalsIgnoreCase("dct")){
						status = Constants.SUCCESS;
						//update dct in subscription table
						subscriptionInfoServiceImpl.updateDeactivateStatusinSubscription(sub.getMsisdn(), sub.getServiceName(), "dct");
						//send dct sms only for rnw scenarios.
						if(sub.getAction().equalsIgnoreCase("rnw") || sub.getAction().equalsIgnoreCase("act")){
							String shortcode = subscriptionInfoServiceImpl.findShortCodeBySpId(sub.getSpId()); //get shortcode only
							if(shortcode!=null && !shortcode.equalsIgnoreCase("null")){
								pController.smsApi(sub.getMsisdn(), sub.getSpId(), purId, "null", sub.getValidity(), sub.getServiceName(), sub.getAmount(), "dct", shortcode);
							}
						}
					} else { //try payment
						payResp = pController.PaymentApi(purSubDetails);
						if(payResp != null && payResp.getTransactionOperationStatus().equalsIgnoreCase("Charged")){
							status = Constants.SUCCESS;
							rResp = SubscriptionRetryHandler.getInstance().RetryTime(sub, status, sub.getMsisdn(), purSubDetails);
							subscriptionInfoServiceImpl.UpdateRenewalActRequest(status, purReq.getPurchaseId(), sub.getMsisdn(), sub.getValidity(), sub.getRenewStatus() , purId, rResp, 
									sf.format(sub.getEndTime()), sub.getAction());

							//Send SMS for Grace --> Rnw flow for billing success scenario only
							/*if(sub.getAction().equalsIgnoreCase("rnw")){
								String shortcode = subscriptionInfoServiceImpl.findShortCodeBySpIdAndSrvName(sub.getSpId(), sub.getServiceName());//check for both shortcode + subsmsConfig
								if(shortcode!=null && !shortcode.equalsIgnoreCase("null")){
									pController.smsApi(sub.getMsisdn(), sub.getSpId(), purId, commonHelper.getEndDate(sub.getValidity(), "act"), sub.getValidity(), sub.getServiceName(), sub.getAmount(), "rnw", shortcode);
								}
							}*/

							//check for time frame of billing for grace and send notification sms.
							if(sub.getAction().equalsIgnoreCase("rnw")){
								int curHour = CommonHelper.getHour(); 
								if(curHour > 8 && curHour < 20){ //Remainder SMS for hour compare which is greater than 8am and less than 8pm
									String shortcode = subscriptionInfoServiceImpl.findShortCodeBySpId(sub.getSpId()); //get shortcode only
									if(shortcode!=null && !shortcode.equalsIgnoreCase("null")){
										pController.smsApi(sub.getMsisdn(), sub.getSpId(), purId, CommonHelper.getStrDateFromDateFormat((Date) sf.parse(CommonHelper.getEndDate(sub.getValidity(), "act"))), 
												sub.getValidity(), sub.getServiceName(), sub.getAmount(), "rem", shortcode);
									}
								}
							}
						}
						else {
							if(payResp != null && payResp.getSerMessageId() != null || payResp.getPolMessageId() != null || payResp.getCode() != null) {
								if(payResp.getSerMessageId().equalsIgnoreCase("SVC0001")){
									status = Constants.MSISDN_NOT_ACTIVE;
								}else if(payResp.getSerMessageId().equalsIgnoreCase("SVC0002")){
									if(payResp.getSerVariables().contains("Service is deactivated")==true) { //DCT via STOP SMS or DOB Call
										Logging.getLogger().info("**Sub Grace/Park Failed -> Deactivating service with (Service is deactivated) response for STOP SMS/DOB CALL dct.");
										purSubDetails.setAction("dct");
										purSubDetails.setPrice("0");
										purSubDetails.setNetType("sms/bod");
										createDctRequestForApiFailure(sub, purId, purReq, "sms/bod");
										status = Constants.SUCCESS;
									} else {
										status = Constants.BAD_REQUEST;
									}
								}else if(payResp.getCode().equalsIgnoreCase("900901")) {
									status = Constants.INVALID_CREDENTIALS;
								}else if(payResp.getPolMessageId().equalsIgnoreCase("POL0001")) {
									if(payResp.getPolVariables().contains("Blacklisted Number")==true){ //DCT via Blacklist
										Logging.getLogger().info("**Sub Grace/Park Failed -> Deactivating service with (Blacklisted Number) response");
										purSubDetails.setAction("dct");
										purSubDetails.setPrice("0");
										purSubDetails.setNetType("sms/bod");
										createDctRequestForApiFailure(sub, purId, purReq, "sms/bod");
										status = Constants.SUCCESS;
									} else {
										status = Constants.MSISDN_BLOCKED;
									}
								}else if(payResp.getCode().equalsIgnoreCase("900800")) {
									status = Constants.LOW_BAL;
								}else {
									status = Constants.LOW_BAL;
								}
								//update for payment failures/ retry logics in subscription table
							} else {
								status = Constants.NETWORK_ERROR;
							}
							if(!purSubDetails.getAction().equalsIgnoreCase("dct")){
								rResp = SubscriptionRetryHandler.getInstance().RetryTime(sub, status, sub.getMsisdn(), purSubDetails);
								subscriptionInfoServiceImpl.UpdateRenewalActRequest(status, purReq.getPurchaseId(), sub.getMsisdn(), sub.getValidity(), sub.getRenewStatus() , 
										purId, rResp, sf.format(sub.getEndTime()), sub.getAction());
							}
						}
					}
					CDRLogs.getCDRWriter().logCDR(purSubDetails, status);
					infocp.redirectTOCP(request, response, purSubDetails, status, Constants.SERVER);
				} catch (Exception e) {
					ErrorLogger.getLogger().error("Exception in graceParkRetryScheduler : "+ e.getMessage());
				}finally{
					MDC.remove("Robi-UNIQUE-ID");
				}
			}
		}
	}

	@Scheduled(cron = "${cron.renewal.expression}", zone="Asia/Dhaka")
	public void RenewalScheduler() {
		Logging.getLogger().info("********* Renewal Scheduler *********");
		PaymentResponse payResp = null;
		SmsResponse smsResp = null;
		String status = null;
		RetryResponse rResp;
		HttpServletRequest request = null;
		HttpServletResponse response = null;
		List<Subscription> subs = subscriptionInfoServiceImpl.getSubRenewalData();
		Logging.getLogger().info("&&&Fetching Renewal records from Subscription table : "+ subs.size());
		if (subs != null && subs.size() != 0 && !subs.isEmpty()) {
			Calendar c = Calendar.getInstance();
			int presentHour = c.getTime().getHours();
			PurchaseRequestDetail purSubDetails =null;
			for (Subscription sub : subs) {
				long startTime = System.currentTimeMillis();
				try{
					Logging.getLogger().info("***Subscription params from Subscription tables "
							+":requestId-"+sub.getPurchaseId()
							+":msisdn-"+sub.getMsisdn()
							+":price-"+sub.getAmount()
							+":action-"+sub.getAction()
							+":serviceName-"+sub.getServiceName()
							+":starttime-"+sub.getStartTime()
							+":endtime-"+sub.getEndTime()
							+":renewalstatus-"+sub.getRenewStatus()
							+":validity-"+sub.getValidity()
							+":retryCount-"+sub.getRetryCount()
							+":chrgStatus-"+sub.getCharged()
							+":retryTime-"+sub.getRetryTime()
							+":retryDay-"+sub.getRetryDays()
							);

					Purchases purReq = null;

					purReq = subscriptionInfoServiceImpl.getUserPurchaseInfo(sub.getMsisdn(), sub.getPurchaseId(), sub.getServiceName());
					if(purReq == null){
						purReq = subscriptionInfoServiceImpl.getUserPurchaseInfo(sub.getMsisdn(), null, sub.getServiceName());
					}

					Logging.getLogger().info("##Purchase details from getUserPurchaseInfo in Renewal thread "
							+":msisdn:"+purReq.getMsisdn()
							+":validity:"+purReq.getValidity()
							+":amount:"+purReq.getAmount()
							+":purchaseID:"+purReq.getPurchaseId()
							+":cptxnid:"+purReq.getCpTxId());

					SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					int sessionId = (int) (Math.random()*9990)+5000;

					String purchaseId = Integer.toString(sessionId) + sub.getPurchaseId();
					MDC.put("Robi-UNIQUE-ID", purchaseId);
					purSubDetails = new PurchaseRequestDetail();
					purSubDetails.setSpid(sub.getSpId());
					purSubDetails.setCptxnid(purReq.getCpTxId());
					purSubDetails.setPrice(purReq.getAmount());
					purSubDetails.setCurrency(purReq.getCur());
					purSubDetails.setSerName(purReq.getServiceName());	
					purSubDetails.setSerProvName(sub.getSerProvName());
					purSubDetails.setValidity(sub.getValidity());
					purSubDetails.setPurchaseId(purchaseId);
					purSubDetails.setRequestTime(commonHelper.getStrDate());
					purSubDetails.setMsisdn(sub.getMsisdn());
					purSubDetails.setAction("rnw");
					purSubDetails.setNetType(purReq.getNetype().equalsIgnoreCase("cellular") ? "cellular" : purReq.getNetype());

					payResp = pController.PaymentApi(purSubDetails);
					if(payResp != null && payResp.getTransactionOperationStatus().equalsIgnoreCase("Charged")){
						status = Constants.SUCCESS;
						rResp = SubscriptionRetryHandler.getInstance().RetryTime(sub, status, sub.getMsisdn(), purSubDetails);
						subscriptionInfoServiceImpl.UpdateRenewalActRequest(status, purReq.getPurchaseId(), sub.getMsisdn(), sub.getValidity(), sub.getRenewStatus() ,
								purchaseId, rResp, sf.format(sub.getEndTime()),"rnw");
						//pController.smsApi(sub.getMsisdn(), sub.getSpId(), purchaseId, sf.format(commonHelper.getEndDate(sub.getValidity(), "act")), sub.getValidity(), sub.getgetServiceName(), sub.getAmount(), "rnw");

						//String shortcode = subscriptionInfoServiceImpl.findShortCodeBySpId(sub.getSpId()); //get shortcode only
						String shortcode = subscriptionInfoServiceImpl.findShortCodeBySpIdAndSrvName(sub.getSpId(), sub.getServiceName());//check for both shortcode + subsmsConfig
						//if rnwSms is enabled send sms.
						if(shortcode!=null && !shortcode.equalsIgnoreCase("null")){
							pController.smsApi(sub.getMsisdn(), sub.getSpId(), purchaseId, commonHelper.getEndDate(sub.getValidity(), "act"), sub.getValidity(), sub.getServiceName(), sub.getAmount(), "rnw", shortcode);
						}
					}else {
						if(payResp != null && payResp.getSerMessageId() != null || payResp.getPolMessageId() != null || payResp.getCode() != null) {
							if(payResp.getSerMessageId().equalsIgnoreCase("SVC0001")){
								status = Constants.MSISDN_NOT_ACTIVE;
							}else if(payResp.getSerMessageId().equalsIgnoreCase("SVC0002")){
								if(payResp.getSerVariables().contains("Service is deactivated")==true) { //DCT via STOP SMS or DOB Call
									Logging.getLogger().info("**Renewal Failed -> Deactivating service with (Service is deactivated) response for STOP SMS/DOB CALL dct.");
									purSubDetails.setAction("dct");
									purSubDetails.setPrice("0");
									purSubDetails.setNetType("sms/bod");
									createDctRequestForApiFailure(sub, purchaseId, purReq, "sms/bod");
									status = Constants.SUCCESS;
								} else {
									status = Constants.BAD_REQUEST;
								}
							}else if(payResp.getCode().equalsIgnoreCase("900901")) {
								status = Constants.INVALID_CREDENTIALS;
							}else if(payResp.getPolMessageId().equalsIgnoreCase("POL0001")) {
								if(payResp.getPolVariables().contains("Blacklisted Number")==true){ //DCT via Blacklist
									Logging.getLogger().info("**Renewal Failed -> Deactivating service with (Blacklisted Number) response");
									purSubDetails.setAction("dct");
									purSubDetails.setPrice("0");
									purSubDetails.setNetType("sms/bod");
									createDctRequestForApiFailure(sub, purchaseId, purReq, "sms/bod");
									status = Constants.SUCCESS;
								} else {
									status = Constants.MSISDN_BLOCKED;
								}
							}else if(payResp.getCode().equalsIgnoreCase("900800")) {
								status = Constants.LOW_BAL;
							}else {
								status = Constants.LOW_BAL;
							}
							//update for payment failures/ retry logics in subscription table
						} else {
							status = Constants.NETWORK_ERROR;
						}
						if(!purSubDetails.getAction().equalsIgnoreCase("dct")){
							rResp = SubscriptionRetryHandler.getInstance().RetryTime(sub, status, sub.getMsisdn(), purSubDetails);
							subscriptionInfoServiceImpl.UpdateRenewalActRequest(status, purReq.getPurchaseId(), sub.getMsisdn(), sub.getValidity(), sub.getRenewStatus() ,
									purchaseId, rResp, sf.format(sub.getEndTime()), "rnw");
						}
					}
					//insert renewal requests into purchases table
					CDRLogs.getCDRWriter().logCDR(purSubDetails, status);
					infocp.redirectTOCP(request, response, purSubDetails, status, Constants.SERVER);
				}catch(Exception e) {
					ErrorLogger.getLogger().error("Exception in RenewalScheduler : " + e.getMessage());
				} finally{
					MDC.remove("Robi-UNIQUE-ID");
				}
			}
		}
	}

	@Scheduled(fixedDelay = 44000000, zone="Asia/Dhaka")
	public void failedCallbackRetry() {
		Logging.getLogger().info("********* Callback Failed Retry Scheduler *********");
		List<CpCallbacks> purq = subscriptionInfoServiceImpl.getCPCallbackDetails("P");	
		HttpServletRequest request = null;
		HttpServletResponse response = null;
		try{
			String statusCode = "";
			Logging.getLogger().info("&&&Fetching FailedCallback records from CP_Callbacks table : " + purq.size());
			if (purq != null && purq.size() != 0) {
				for (CpCallbacks pur : purq) {
					MDC.put("Robi-UNIQUE-ID", pur.getPurchaseId());
					long startTime = System.currentTimeMillis();
					String CPDeliveryStr = pur.getCpdeliveryStr();
					JSONObject json = null;
					try {
						json = new JSONObject(new Gson().toJson(CPDeliveryStr));
					} catch (JSONException e) {
						Logging.getLogger().error("Exception in (JSON) failedCallbackRetry : " + e.getMessage());
					}
					commonHelper.Post2Json(pur.getUrl(), json);
					if(statusCode.equalsIgnoreCase("SUCCESS")){
						subscriptionInfoServiceImpl.updateCallbackStatus("Y", pur.getPurchaseId());
						Logging.getLogger().info("**Callback Success with purchase id = "+ pur.getPurchaseId()+" status=" + statusCode);
					}else{
						subscriptionInfoServiceImpl.updateCallbackStatus("P",pur.getPurchaseId());
						Logging.getLogger().info("**Callback Failed with purchase id = "+ pur.getPurchaseId()+" status=" +statusCode);
					}
				} 
			}
		}catch(Exception e){
			ErrorLogger.getLogger().error("Exception in failedCallbackRetry : " + e.getMessage());
		}
	}

	public void createDctRequestForApiFailure(Subscription sub, String purId, Purchases pur, String nettype){
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			//update db for dct status for stop sms/dob call from Robi.
			subscriptionInfoServiceImpl.updateDeactivateStatusinSubscription(sub.getMsisdn(), sub.getServiceName(), "dct");

			//create new purchase request and insert in DB
			Purchases dctPurchase = new Purchases();
			dctPurchase.setAmount("0");
			dctPurchase.setCharged("N");
			dctPurchase.setCpTxId(pur.getCpTxId());
			dctPurchase.setAction("dct");
			dctPurchase.setCur(pur.getCur());
			dctPurchase.setJunoStatus(SUCCESS);
			dctPurchase.setMsisdn(sub.getMsisdn());
			dctPurchase.setNetype(nettype.equalsIgnoreCase("null")?"otp/dob":nettype);
			dctPurchase.setOperator("Robi");
			dctPurchase.setPpurchaseId(sub.getPurchaseId());
			dctPurchase.setPurchaseId(purId);
			dctPurchase.setRequestTime(sf.parse(commonHelper.getStrDate()));
			dctPurchase.setServiceName(sub.getServiceName());
			dctPurchase.setServiceProvName(sub.getSerProvName());
			dctPurchase.setSpId(sub.getSpId());
			dctPurchase.setTotalAmountCharged("0");
			dctPurchase.setValidity(sub.getValidity());
			dctPurchase.setCallbackStatus("Y");	
			
			subscriptionInfoServiceImpl.insertNewActivation(dctPurchase);
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in createDctRequestForApiFailure : ",e);
		}
	}
}