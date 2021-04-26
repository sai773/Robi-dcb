package com.juno.datapojo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.juno.controller.PurchaseController;
import com.juno.database.model.Purchases;
import com.juno.database.model.Subscription;
import com.juno.database.service.SubscriptionInfoServiceImpl;
import com.juno.logs.CDRLogs;
import com.juno.logs.ErrorLogger;
import com.juno.logs.Logging;
import com.juno.util.CommonHelper;

@Service
public class SubscriptionRetryHandler {
	
	@Autowired
	SubscriptionInfoServiceImpl subscriptionInfoServiceImpl;
	
	@Autowired
	CommonHelper commonHelper;
	
	@Autowired
	PurchaseController pController;
	
	public static int graceRetryPerDay;
	public static int parkRetryPerDay;
	public static int graceCount;
	public static int parkCount;
	
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
	
	private static SubscriptionRetryHandler m_RetryHdl = null;

	public static synchronized SubscriptionRetryHandler getInstance(){
		if(m_RetryHdl==null)
		{
			m_RetryHdl = new SubscriptionRetryHandler();
		}
		return m_RetryHdl;
	}

	public void SubsInsertonChargeResponse(PurchaseRequestDetail pur, Purchases pd, Subscription sub, String validity, String status) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		MDC.get("Robi-UNIQUE-ID");
		
		//Purchases purReq = subscriptionInfoServiceImpl.getUserPurchaseInfoFrompurchID(pur.getPurchaseId());
		
		SmsResponse smsresp = null;
		String action = "";
		Logging.getLogger().info("Subscription Insertion/Updating for Subscription  : "+ pur.getAction() +", Msisdn : "+pur.getMsisdn());
		try {
			Subscription subs = null;
			if(sub == null){
				subs = subscriptionInfoServiceImpl.getSubscriptionData(pur.getMsisdn(), pur.getCptxnid(), pur.getSpid(), pur.getAction());
			} else {
				subs = sub;
			}
			
			RetryResponse rResp = null;
			if(subs != null && pur.getPurchaseId().equalsIgnoreCase(subs.getPurchaseId()) && 
						(subs.getAction().equalsIgnoreCase("dct") || subs.getCharged().equalsIgnoreCase("P"))){
				rResp = RetryTime(null, status, pur.getMsisdn(), pur);
			} else {
				rResp = RetryTime(sub == null ? subs : sub, status, pur.getMsisdn(), pur);
			}
			
			String chrg = status.equalsIgnoreCase("J201")?"Y":"P";

			if(pur.getAction().equalsIgnoreCase("act") || pur.getAction().equalsIgnoreCase("act_park")) {
				//update if you are already subscribed
				if(subs!=null && pur.getPurchaseId().equalsIgnoreCase(subs.getPurchaseId()) && 
						(subs.getAction().equalsIgnoreCase("dct") || subs.getCharged().equalsIgnoreCase("P"))) { 
					Logging.getLogger().info("####Subscription Act : " + chrg + " updating DB for dct record : " + pur.getPurchaseId());
					subscriptionInfoServiceImpl.UpdateActivateRequest(validity, pur, rResp, subs, status);
				} 
				else if(subs!=null && pur.getPurchaseId().equalsIgnoreCase(subs.getPurchaseId()) && 
						(subs.getCharged().equalsIgnoreCase("P") || subs.getCharged().equalsIgnoreCase("P")) && subs.getJunoStatus().equalsIgnoreCase("act")) {
					Logging.getLogger().info("####Subscription Act : " + chrg + ", updating DB: " + pur.getPurchaseId());
					subscriptionInfoServiceImpl.UpdateActivateRequest(validity, pur, rResp, subs, status);
				}
				else if(sub!=null && sub.getCharged().equalsIgnoreCase("P")) { //&& !sub.getStatus().equalsIgnoreCase("act")
					Logging.getLogger().info("####Subscription Act retry "+chrg+", updating DB: "+pur.getPurchaseId());
					subscriptionInfoServiceImpl.UpdateActivateRequest(validity, pur, rResp, sub, status);
				}
				else {
					if(status.equalsIgnoreCase("J201") || status.equalsIgnoreCase("J202")){
						Calendar c = Calendar.getInstance();
						String endDate = commonHelper.getEndDate(validity, pur.getAction());

						Subscription subscription = new Subscription();
							subscription.setSpId(pur.getSpid());
							subscription.setAmount(pur.getPrice());
						    subscription.setValidity(pur.getValidity());
						    subscription.setCpTxId(pur.getCptxnid());
						    if(status.equalsIgnoreCase("J202")){
						    	action = pur.getAction().equalsIgnoreCase("act") ? "act" : "rnw" ;
						    }else {
								action = pur.getAction();
							}
						    subscription.setAction(action);
						    subscription.setEndTime(sdf.parse(commonHelper.getEndDate(validity, pur.getAction())));
						    subscription.setMsisdn(pur.getMsisdn());
						    subscription.setPurchaseId(pur.getPurchaseId());
						    subscription.setStartTime(sdf.parse(commonHelper.getStrDate()));
						    subscription.setSerProvName(pur.getSerProvName());
						    subscription.setServiceName(pur.getSerName());
						    //subscription.setJunoStatus(status);
						    if(pur.getValidity().equalsIgnoreCase("PD")) {
						    	subscription.setCharged(status.equalsIgnoreCase("J201") ? "Y" : "F");
						    }
						    subscription.setCharged(status.equalsIgnoreCase("J201") ? "Y" : "P");
						    //subscription.setEndTime(sdf.parse(endDate));
							subscription.setRenewStatus(0);
							subscription.setRetryCount(rResp.getR_Count());
							//subscription.setCharged(rResp.getR_ChrgStatus());
							if(rResp.getR_time()!=null) {
								subscription.setRetryTime(sdf.parse(rResp.getR_time()));
							}
							subscription.setRetryDays(rResp.getR_days());	
							subscription.setCgstatusDesc(pur.getSerDesc());
							subscription.setJunoStatus(status.equalsIgnoreCase("J201") ? "J201" : "J202");
						    subscriptionInfoServiceImpl.insertSubscription(subscription);
					}else {
						Logging.getLogger().info("Not Entered into Subscription table because of this Failure " + status);
					}
				}
			} else if(sub.getAction().equalsIgnoreCase("rnw") || sub.getAction().equalsIgnoreCase("act_grace")) {
				//update subscription table for renewals
				Logging.getLogger().info("Subscription Rnw "+chrg+", updating DB: "+pur.getPurchaseId());

				subscriptionInfoServiceImpl.UpdateRenewalActRequest(status, pur.getPurchaseId(), pur.getMsisdn(), validity, sub.getRenewStatus(), pur.getPurchaseId(), rResp, sdf.format(sub.getEndTime()), "rnw");
			}
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in SubsInsertonChargeResponse : "+e.getMessage());
		}
	}

	public RetryResponse RetryTime(Subscription sub, String chrgCode, String msisdn, PurchaseRequestDetail pur) {
		RetryResponse rResp = null;
		String retryDate = null, status = null;
		int retryCount = 0, retryday = 0;
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+6")); 
			Calendar c = Calendar.getInstance();
			//c.add(Calendar.HOUR, 5);
			c.add(Calendar.HOUR, 4);
			c.add(Calendar.MINUTE, 30);
			retryDate = sdf.format(c.getTime());
	
			if(sub == null) { //Subscription 1st Activation
				if(chrgCode.equalsIgnoreCase("J201")) { //success   
					rResp = new RetryResponse(0, "Y", null, 0);
					return rResp;
				} else { //failure
					rResp = new RetryResponse(1, "P", retryDate, 0);
					return rResp;
				}
			}

			else if(sub != null) { //Renewal/Grace/Parking

				if(chrgCode.equalsIgnoreCase("J201")) { //success 
					rResp = new RetryResponse(0, "Y", null, 0);
					return rResp;
				} /*else if(!chrgCode.equalsIgnoreCase("J201")){ 
					rResp = new RetryResponse((sub.getRetryCount()==0 || sub.getRetryCount()==null) ? 1 : sub.getRetryCount(), "P", retryDate, 
							(sub.getRetryDays()==0 || sub.getRetryDays()==null) ? 1 : sub.getRetryDays());
					return rResp;
				}*/
				else {
					status = "P";

					if(sub.getRetryCount() >= graceRetryPerDay || sub.getRetryCount() >= parkRetryPerDay || sub.getRetryCount() == null){
						retryCount = 1;
					} else {
						retryCount = sub.getRetryCount() + 1;
					}

					if(sub.getRetryDays() == null){
						retryday = 1;
					}
					if(!sub.getAction().equalsIgnoreCase("dct")){ //rnw,grace,park retry

						if(sub.getRetryCount() >= graceRetryPerDay && 
								((sub.getAction().equalsIgnoreCase("act") && sub.getRetryDays() < parkCount) || 
										(sub.getAction().equalsIgnoreCase("rnw") && sub.getRetryDays() < graceCount))){
							retryday = sub.getRetryDays() + 1;
						} else if(sub.getRetryCount() < graceRetryPerDay && 
								((sub.getAction().equalsIgnoreCase("act") && sub.getRetryDays() < parkCount) || 
										(sub.getAction().equalsIgnoreCase("rnw") && sub.getRetryDays() < graceCount))){
							retryday = sub.getRetryDays();
						} else if(pur.getNetType().equalsIgnoreCase("sys-churn") &&
								((sub.getAction().equalsIgnoreCase("act") && sub.getRetryDays() >= parkCount) || 
								(sub.getAction().equalsIgnoreCase("rnw") && sub.getRetryDays() >= graceCount))){
							retryday = 0;
							status = "N";
						} else {
							retryday = 1;
						}

						rResp = new RetryResponse(retryCount, status, retryDate, retryday);
					}
				}
			}	
		}catch(Exception e){
			ErrorLogger.getLogger().error("Exception in RetryTime : "+e.getMessage());
		}
		return rResp;
	}
}
