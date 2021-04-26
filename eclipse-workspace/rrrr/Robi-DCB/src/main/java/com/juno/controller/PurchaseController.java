package com.juno.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.URI;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.juno.database.model.AppRefererDetails;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.juno.database.model.CpCredentials;
import com.juno.database.model.LoginDetails;
import com.juno.database.model.ProductInfo;
import com.juno.database.model.Purchases;
import com.juno.database.model.Subscription;
import com.juno.database.service.SubscriptionInfoServiceImpl;
import com.juno.datapojo.PaymentRequest;
import com.juno.datapojo.PaymentResponse;
import com.juno.datapojo.PurchaseRequestDetail;
import com.juno.datapojo.SmsInterval;
import com.juno.datapojo.SmsMessage;
import com.juno.datapojo.SmsRequest;
import com.juno.datapojo.SmsResponse;
import com.juno.datapojo.SubscriptionRetryHandler;
import com.juno.logs.CDRLogs;
import com.juno.logs.ErrorLogger;
import com.juno.logs.Logging;
import com.juno.redisRepo.PurchaseDetailValidationRepositoryImpl;
import com.juno.redisRepo.TokenRepoImpl;
import com.juno.scheduler.RobiRenewal;
import com.juno.util.AesEncryptDecrypt;
import com.juno.util.CommonHelper;
import com.juno.util.Constants;
import com.juno.util.InformContentProvider;
import com.juno.util.SecureImageResponse;
import com.juno.redisRepo.RedisMessagePublisher;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

@Controller
public class PurchaseController implements Constants{

	@Autowired
	PurchaseDetailValidationRepositoryImpl purchaseDetailValidationRepositoryImpl;

	@Autowired 
	TokenRepoImpl tokenRepoImpl;

	@Autowired
	CommonHelper auth;

	@Autowired
	SmsMessage smsMessage;

	@Autowired
	SubscriptionInfoServiceImpl subscriptionInfoServiceImpl;

	@Autowired
	SubscriptionRetryHandler subRetry;

	@Autowired
	RedisMessagePublisher redisMessagePublisher;

	@Autowired
	InformContentProvider infocp;

	@Autowired
	RobiRenewal robirnw;

	public static String aocURL;
	public static String getImgURL;
	public static String checkImgURL;
	public static int httpTimeout; 
	public static String botUrl;
	public static String ashieldOrgId;
	public static String currency;
	public static int apiDelayTimer;
	public static int sessionTimeout;

	@Value("${RobiURL}")
	private String robiUrl;

	@Value("${AocURL}")
	public void setAocUrl(String aocurl) {
		aocURL = aocurl;
	}

	@Value("${GetImgURL}")
	public void setgetImgUrl(String url) {
		getImgURL = url;
	}

	@Value("${CheckImgURL}")
	public void setcheckImgUrl(String url) {
		checkImgURL = url;
	}

	@Value("${httpTimeout}")
	public void sethttpTimeout(int timeout) {
		httpTimeout = timeout;
	}

	@Value("${BotURL}")
	public void setBotUrl(String burl) {
		botUrl = burl;
	}

	@Value("${AshieldOrgId}")
	public void setAshieldOrgId(String orgId) {
		ashieldOrgId = orgId;
	}

	@Value("${currency}")
	public void setCurrency(String cur) {
		currency = cur;
	}

	@Value("${ApiDelayTimer}")
	public void setApiDelayTimer(int timer) {
		apiDelayTimer = timer;
	}

	@Value("${CgSessionTimeout}")
	public void setSessionTimeout(int timeout) {
		sessionTimeout = timeout;
	}

	ArrayList<String> appPkgNameList = new ArrayList<String>();

	/**
	 * 
	 * @param serviceName
	 * @param serviceDesc
	 * @param serImgUrl
	 * @param action
	 * @param cpTxnid
	 * @param msisdn
	 * @param signature
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ParseException
	 */

	@SuppressWarnings("unused")
	@RequestMapping(value = "/jsecure")
	public @ResponseBody void getPurchaseDetails(@RequestParam(value="sn") String serviceName, 
			@RequestParam(value="sd") String serviceDesc,
			@RequestParam(value="simgurl") String serImgUrl,   
			@RequestParam(value="action") String action, 
			@RequestParam(value="cptxnid") String cpTxnid, 
			@RequestParam(value="m") String msisdn, 
			@RequestParam(value="sig") String signature,
			//@RequestParam(value = "sig", required = false) String signature, //only for local testing
			HttpServletRequest request, HttpServletResponse response ) throws IOException, ParseException { 
		try{
			String remoteIp = request.getRemoteAddr() != null ? request.getRemoteAddr() : "null";
			String xforwardedIP = request.getHeader("X-FORWARDED-FOR") != null ? request.getHeader("X-FORWARDED-FOR") : "null";
			String clientIP = request.getHeader("CLIENT_IP") != null ? request.getHeader("CLIENT_IP") : "null";
			String referer = request.getHeader("referer") != null ? request.getHeader("referer") : "null";
			String acpt = request.getHeader("accept") != null ?request.getHeader("accept"):"null";
			String userAgent = request.getHeader("user-agent") != null?request.getHeader("user-agent") : "null";
			String headerName = null;
			String headerValue = null;
			String hmsisdn = request.getHeader("msisdn") != null ? request.getHeader("msisdn") : null;
			//msisdn = hmsisdn;
			String reqmsisdn = msisdn!=null?msisdn:null;
			String xreqWithRef = request.getHeader("x-requested-with")!= null ? request.getHeader("x-requested-with") : "null";
			String reqIframe = request.getHeader("sec-fetch-dest")!=null ? request.getHeader("sec-fetch-dest") : "null";

			String mip = Stream.of(xforwardedIP, remoteIp, clientIP)
					.filter(s -> s != null && !s.isEmpty() && !s.equalsIgnoreCase("null"))
					.collect(Collectors.joining("-"));

			StringBuilder strbldr=new StringBuilder();
			PurchaseRequestDetail purDetails = new PurchaseRequestDetail();
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Subscription sub;
			CpCredentials cred;
			String stats;
			String btresp = "";
			int random = (int)(Math.random() * 9999 + 1);
			String spid = "";
			String price = "";
			String validity = "";
			String serviceProvName = "";
			String purchaseID = cpTxnid + random;
			String requestTime = auth.getStrDate();
			MDC.put("Robi-UNIQUE-ID", purchaseID);

			Logging.getLogger().info("*** Purchase Params *** purchaseId : " + purchaseID + ", serviceName : " + serviceName + 
					", serviceDesc : " + serviceDesc + ", serImgUrl : " + serImgUrl + ", cpTxnid : " + cpTxnid + ", action : " + action + 
					", hmsisdn : " + hmsisdn + ", reqmsisdn : " + reqmsisdn + ", userAgent : " + userAgent + ", signature : " + signature + ", remoteIp : " + remoteIp +
					", X-forwardedIP : " + xforwardedIP + ", clientIP : " + clientIP + ", Referer : " + referer +
					", xreqRef : "+xreqWithRef+", IFrame : "+ reqIframe);

			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				headerName = headerNames.nextElement();
				Enumeration<String> headers = request.getHeaders(headerName);
				while (headers.hasMoreElements()) {
					headerValue = headers.nextElement();
				}
				Logging.getLogger().info("**HEADER --> "+headerName  + " : " + headerValue);
			}

			purDetails.setRequestTime(requestTime);
			purDetails.setPurchaseId(purchaseID);
			purDetails.setMsisdn(hmsisdn);
			purDetails.setAction(action);
			purDetails.setCptxnid(cpTxnid);
			purDetails.setSerDesc(serviceDesc);
			purDetails.setSerimgurl(serImgUrl);
			purDetails.setSerName(serviceName);
			purDetails.setCurrency(currency);
			purDetails.setTotalAmountCharged("0");
			purDetails.setAcpt(acpt != null ? acpt : "null");
			purDetails.setBua(userAgent);
			purDetails.setMip(mip);
			purDetails.setReferer(referer);
			purDetails.setXReqWithRef(xreqWithRef);
			purDetails.setIframe(reqIframe);

			/** Product Validation */

			ProductInfo pDetails = subscriptionInfoServiceImpl.findByServiceName(serviceName);
			if(pDetails == null) {
				Logging.getLogger().info("********* Price point not in range ***********");
				purDetails = XReqWithAnalysisFromDB(purDetails);
				CDRLogs.getCDRWriter().logCDR(purDetails, PRICEPOINT_NOT_CONFIGURED);
				infocp.redirectTOCP(request, response, purDetails, PRICEPOINT_NOT_CONFIGURED, REDIRECT);			
				return;
			}else {
				spid = pDetails.getSpId();
				price = pDetails.getAmount();
				validity = pDetails.getValidity();
				serviceProvName = pDetails.getServiceProvName();

				purDetails.setSpid(spid);
				purDetails.setPrice(price);
				if(validity.equalsIgnoreCase("0")) {
					purDetails.setValidity("PD");
				}else {
					purDetails.setValidity(validity);
				}
				purDetails.setSerProvName(serviceProvName);
				purDetails.setSmsShortCode(pDetails.getSmsShortCode());
			}

			/** Mandatory Parameters [cptxnid, action, serName,  serDesc, signature]  */
			int status = validateMandParamsReq(serviceName, cpTxnid, action, serviceDesc, signature);
			if(status == -1 || (!(!pDetails.getValidity().equalsIgnoreCase("0") && (action.equalsIgnoreCase("act") || action.equalsIgnoreCase("dct"))) && 
					(!(pDetails.getValidity().equalsIgnoreCase("0") && action.equalsIgnoreCase("PD"))))){
				//|| ((pDetails.getValidity().equalsIgnoreCase("0") && action.equalsIgnoreCase("PD")))) {
				purDetails = XReqWithAnalysisFromDB(purDetails);
				CDRLogs.getCDRWriter().logCDR(purDetails, MISSING_PARAMETER);
				infocp.redirectTOCP(request, response, purDetails, MISSING_PARAMETER, REDIRECT);			
				return;
			}


			/** Validating Signature **/
			cred = subscriptionInfoServiceImpl.getASsecretKey(spid);
			//if(signature==null && !auth.validateSignature(serviceName, cpTxnid, action, cred.getAsSecretKey(), signature)) { //local testing
			if(!auth.validateSignature(serviceName, cpTxnid, action, cred.getAsSecretKey(), signature)) {
				Logging.getLogger().info("******** Signature validation failed *********");
				purDetails = XReqWithAnalysisFromDB(purDetails);
				CDRLogs.getCDRWriter().logCDR(purDetails, INVALID_CREDENTIALS);
				infocp.redirectTOCP(request, response, purDetails, INVALID_CREDENTIALS, REDIRECT );
				return;
			}

			/** MSISDN Validation */
			if(hmsisdn != null && reqmsisdn != null) {
				if(hmsisdn.equals("0") || hmsisdn.equalsIgnoreCase("null") || reqmsisdn.equalsIgnoreCase("0") || reqmsisdn.equalsIgnoreCase(" ") || reqmsisdn.equalsIgnoreCase("null")) {
					Logging.getLogger().info("********* MSISDN validation failed *********");
					purDetails = XReqWithAnalysisFromDB(purDetails);
					CDRLogs.getCDRWriter().logCDR(purDetails, MSISDN_NOT_ACTIVE);
					infocp.redirectTOCP(request, response, purDetails, MSISDN_NOT_ACTIVE, REDIRECT);
					return;
				} else if(!hmsisdn.equalsIgnoreCase(reqmsisdn)){
					Logging.getLogger().info("********* User Authentication failed *********");
					purDetails = XReqWithAnalysisFromDB(purDetails);
					CDRLogs.getCDRWriter().logCDR(purDetails, AUTHENTICATION_FAILED);
					infocp.redirectTOCP(request, response, purDetails, AUTHENTICATION_FAILED, REDIRECT);
					return;	
				}
			}else{
				Logging.getLogger().info("********* User Authentication failed *********");
				purDetails = XReqWithAnalysisFromDB(purDetails);
				CDRLogs.getCDRWriter().logCDR(purDetails, AUTHENTICATION_FAILED);
				infocp.redirectTOCP(request, response, purDetails, AUTHENTICATION_FAILED, REDIRECT);
				return;
			}
			msisdn = hmsisdn;

			/** User Already Subscribed or not **/
			if(!action.equalsIgnoreCase("PD") && !action.equalsIgnoreCase("dct")) {
				//String sName = serviceName.substring(0, serviceName.length() - 9);
				String sName = serviceName.substring(0, serviceName.lastIndexOf(" "));
				sub = subscriptionInfoServiceImpl.getSubscriptionData(msisdn, spid, serviceName, action);
				if(sub != null && (sub.getAction().equalsIgnoreCase("act") || sub.getAction().equalsIgnoreCase("rnw"))){ 
					//&& sub.getCharged().equalsIgnoreCase("Y")){
					//if(sub.getServiceName().substring(0, serviceName.length() - 9).equalsIgnoreCase(sName)) {
					if(sub.getServiceName().substring(0, sub.getServiceName().lastIndexOf(" ")).equalsIgnoreCase(sName)) {
						//if(sub.getEndTime().compareTo(sf.parse(requestTime)) > 0) {
						Logging.getLogger().info("******** User Already Subscribed *********");
						purDetails = XReqWithAnalysisFromDB(purDetails);
						CDRLogs.getCDRWriter().logCDR(purDetails, USER_ALREADY_SUBSCRIBED);
						infocp.redirectTOCP(request, response, purDetails, USER_ALREADY_SUBSCRIBED, REDIRECT );
						return;
						//}
					}else {
						Logging.getLogger().info("NO Records Found");
					}
				}
			}

			/** Validating Duplicate Purchase Request */

			String dupCheck = tokenRepoImpl.getDuplicateCheck(cpTxnid);

			if(dupCheck != null) {
				Logging.getLogger().info("******** Duplicate Purchase Request **********");
				purDetails = XReqWithAnalysisFromDB(purDetails);
				CDRLogs.getCDRWriter().logCDR(purDetails, DUPLICATE_REQUEST);
				infocp.redirectTOCP(request, response, purDetails, DUPLICATE_REQUEST, REDIRECT);				
				return;
			}

			tokenRepoImpl.setDuplicateCheck(spid, cpTxnid, msisdn);

			/** Deactivation Request */

			if(action.equalsIgnoreCase("dct")) {
				String dbstatus = SUCCESS_RESPONSE;
				String stat = "";
				Purchases dctPurchase = new Purchases();
				sub = subscriptionInfoServiceImpl.getSubscriptionData(msisdn, spid, serviceName, action);

				if(sub==null){
					sub = subscriptionInfoServiceImpl.getSubscriptionData(msisdn, spid, serviceName, "act");
				}

				if(sub != null) {
					dbstatus = subscriptionInfoServiceImpl.updateDeactivateStatusinSubscription(sub.getMsisdn(), sub.getServiceName(), action);
					dctPurchase.setAction("dct");
					dctPurchase.setAmount("0");
					dctPurchase.setCharged("N");
					dctPurchase.setCpTxId(cpTxnid);
					dctPurchase.setAction(action);
					dctPurchase.setCur(currency);
					dctPurchase.setJunoStatus(SUCCESS);
					dctPurchase.setMsisdn(sub.getMsisdn());
					dctPurchase.setNetype("cellular");
					dctPurchase.setOperator("Robi");
					dctPurchase.setPpurchaseId(sub.getPurchaseId());
					dctPurchase.setPurchaseId(purchaseID);
					dctPurchase.setRequestTime(sf.parse(requestTime));
					dctPurchase.setServiceName(sub.getServiceName());
					dctPurchase.setServiceProvName(serviceProvName);
					dctPurchase.setSpId(spid);
					dctPurchase.setTotalAmountCharged("0");
					dctPurchase.setValidity(sub.getValidity());
					Logging.getLogger().info(dbstatus);
					stat = SUCCESS;
					String callbackstat = infocp.redirectTOCP(request, response, purDetails,  stat, SERVER);
					if(callbackstat.equalsIgnoreCase(SUCCESS_RESPONSE)) {
						smsApi(sub.getMsisdn(), spid, purchaseID, "null", sub.getValidity(), sub.getServiceName(), sub.getAmount(), "dct", pDetails.getSmsShortCode());
						dctPurchase.setCallbackStatus("Y");
						dctPurchase.setCallbackTime(auth.getStrDate());
						subscriptionInfoServiceImpl.insertNewActivation(dctPurchase);
					}else {
						stat = SERVER_ERROR;
					}
				}else {
					stat = SERVER_ERROR;
				}
				purDetails = XReqWithAnalysisFromDB(purDetails);
				CDRLogs.getCDRWriter().logCDR(purDetails, stat);
				infocp.redirectTOCP(request, response, purDetails, stat, REDIRECT);
				return;
			}

			try(CloseableHttpClient client = HttpClients.createDefault()) {
				HttpPost httpPost=new HttpPost(botUrl);
				List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("optxn", purchaseID));                                           
				params.add(new BasicNameValuePair("mip", mip));                                                
				params.add(new BasicNameValuePair("mdn", msisdn));                                         
				params.add(new BasicNameValuePair("bua", userAgent));                                 
				params.add(new BasicNameValuePair("oid", spid)); 
				params.add(new BasicNameValuePair("acpt", acpt));
				params.add(new BasicNameValuePair("srvId", serviceName));
				params.add(new BasicNameValuePair("ref", referer));
				httpPost.setEntity(new UrlEncodedFormEntity(params));

				RequestConfig conf = RequestConfig.custom()
						.setConnectTimeout(httpTimeout)
						.setConnectionRequestTimeout(httpTimeout)
						.setSocketTimeout(httpTimeout).build();
				httpPost.setConfig(conf);

				CloseableHttpResponse btResponse =client.execute(httpPost);
				Logging.getLogger().info("** HE-BOT URL : "+httpPost + ", PARAMS : " + params);
				if (btResponse.getStatusLine().getStatusCode() == 200) {
					try(BufferedReader br = new BufferedReader(new InputStreamReader(btResponse.getEntity().getContent()))){
						String readLine;
						while (((readLine = br.readLine()) != null)) {
							strbldr.append(readLine);
						}
					}
					btresp = strbldr.toString();
					if(btresp != null) {
						Logging.getLogger().info("******** HE-BOT RESPONSE : " + btresp);
						purDetails.setHeResp(btresp);
						if(btresp.equalsIgnoreCase(NOACTION)) {
							purDetails.setImgType("3a");
						}else if(btresp.equalsIgnoreCase(SUSPECT)) {
							purDetails.setImgType("8c");
						}else if(btresp.equalsIgnoreCase(BLOCK)) {
							Logging.getLogger().info("******** Error on Operator Validation *********");
							purDetails = XReqWithAnalysisFromDB(purDetails);
							CDRLogs.getCDRWriter().logCDR(purDetails, OP_VALIDATION_FAILED);
							infocp.redirectTOCP(request, response, purDetails, OP_VALIDATION_FAILED, REDIRECT );
							return;
						}else{
							Logging.getLogger().info("******** Server Error *********");
							purDetails = XReqWithAnalysisFromDB(purDetails);
							CDRLogs.getCDRWriter().logCDR(purDetails, SERVER_ERROR);
							infocp.redirectTOCP(request, response, purDetails, SERVER_ERROR, REDIRECT );
							return;
						}
					}
				} else {
					btresp = null;
					purDetails.setImgType("8c"); 
				}
			} catch (IOException e) {
				ErrorLogger.getLogger().error("Error Connection Timeout/BotModule notreachable, optxn:" + purchaseID+", "+e);
				purDetails.setImgType("8c");
			} 

			/** Store all request value into redis after completion of all validation */

			purchaseDetailValidationRepositoryImpl.setPurchaseRequestDetail(purchaseID, purDetails);
			/* Store Session Timeout Details in redis Queue */
			String message = "ASHIELD"+purchaseID+"#"+purDetails.getRequestTime();	
			redisMessagePublisher.publish(message);

			/** Call to Jsecure */

			try {
				stats = redirectToJsecure(purDetails, request, response);
				if(stats.equalsIgnoreCase(SUCCESS_RESPONSE)) {
					Logging.getLogger().info("Rendered Image in AOC : " + stats);
				}else {
					Logging.getLogger().info("Rendered Image in AOC : " + stats);
					purDetails = XReqWithAnalysisFromDB(purDetails);
					CDRLogs.getCDRWriter().logCDR(purDetails, NETWORK_ERROR);
					infocp.redirectTOCP(request, response, purDetails, NETWORK_ERROR, REDIRECT);
				}
			} catch (InvalidKeyException | NoSuchAlgorithmException e) {
				ErrorLogger.getLogger().error("Exception while redirecting getImage request : " + e.getMessage());
			} 
		}catch(Exception e){
			ErrorLogger.getLogger().error("Exception in PurchaseRequest : ", e);
		} finally{
			MDC.remove("Robi-UNIQUE-ID");
		}
	}

	/**
	 * 
	 * @param serName
	 * @param cptxnid
	 * @param action
	 * @param serDesc
	 * @param sign
	 * @return
	 */

	int validateMandParamsReq(String serName, String cptxnid, String action, String serDesc, String sign)
	{
		if(cptxnid.equalsIgnoreCase("null") || cptxnid.equalsIgnoreCase("")){
			Logging.getLogger().info( "**** Missing Mandatory Parameter cptxnid ****");
			return -1;
		}else if(action.equalsIgnoreCase("null") || action.equalsIgnoreCase("")){
			Logging.getLogger().info( "**** Missing Mandatory Parameter action ****");
			return -1;
		}else if(serName.equalsIgnoreCase("null") || serName.equalsIgnoreCase("")){
			Logging.getLogger().info( "**** Missing Mandatory Parameter ServiceName ****");
			return -1;
		}else if(serDesc.equalsIgnoreCase("null") || serDesc.equalsIgnoreCase("")){
			Logging.getLogger().info( "**** Missing Mandatory Parameter Service Description ****");
			return -1;
		}else if(sign.equalsIgnoreCase("null") ||sign.equalsIgnoreCase("")){
			Logging.getLogger().info( "**** Missing Mandatory Parameter Signature ****");
			return -1;
		}
		return 1;
	}

	public String redirectToJsecure(PurchaseRequestDetail pur, HttpServletRequest request, HttpServletResponse response) throws InvalidKeyException, NoSuchAlgorithmException, ClientProtocolException, IOException {
		StringBuilder imageStr = new StringBuilder();
		String status;
		String time = "0"; 
		RequestDispatcher rd;
		MDC.get( pur.getPurchaseId());
		try (CloseableHttpClient client = HttpClients.createDefault()){
			String size = "240x120";
			String optxn = pur.getPurchaseId();
			String mobileIp = pur.getMip()!=null?pur.getMip():"null";
			String msisdn = pur.getMsisdn()!=null?pur.getMsisdn():"null";
			String browserAgent = pur.getBua();
			String serviceId = pur.getSerName()!=null?pur.getSerName():"null"; //"null";
			String orgId = pur.getSpid();//ashieldOrgId;
			String imsi = "null";
			String circleId = "null";
			String imei = "null";
			String secKey = "abcde";
			String channel = "null";
			String acpt = pur.getAcpt();
			String sip = request.getRemoteAddr();
			String xfip = request.getHeader("X-FORWARDED-FOR") != null ? request.getHeader("X-FORWARDED-FOR") : "null";
			String itpe = pur.getImgType();
			String t1 = "null";
			String t2 = pur.getMsisdn();
			String t3 =  "null";
			String ts = String.valueOf(System.currentTimeMillis());
			String dataToBeHashed = optxn + ts + size +  mobileIp + msisdn + browserAgent + serviceId + orgId + imsi + circleId + imei +  channel +  acpt + sip + xfip + itpe + t1 + t2 + t3;
			String sig = CommonHelper.GenerateSign(secKey, dataToBeHashed);
			HttpPost httpPost=new HttpPost(getImgURL);
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("optxn", optxn));                                             
			params.add(new BasicNameValuePair("size", size));                                               
			params.add(new BasicNameValuePair("sig", sig));                                       
			params.add(new BasicNameValuePair("ts", ts));                                
			params.add(new BasicNameValuePair("mip", mobileIp));                         
			params.add(new BasicNameValuePair("bua", browserAgent));                     
			params.add(new BasicNameValuePair("sid", serviceId));                        
			params.add(new BasicNameValuePair("oid", orgId));                            
			params.add(new BasicNameValuePair("imsi", imsi));                            
			params.add(new BasicNameValuePair("cid", circleId));                         
			params.add(new BasicNameValuePair("imei", imei));  
			params.add(new BasicNameValuePair("channel", channel));
			params.add(new BasicNameValuePair("acpt", acpt));
			params.add(new BasicNameValuePair("sip", sip));
			params.add(new BasicNameValuePair("xfip", xfip));
			params.add(new BasicNameValuePair("itpe", itpe));
			params.add(new BasicNameValuePair("t1", t1));
			params.add(new BasicNameValuePair("t2", t2));
			params.add(new BasicNameValuePair("t3", t3));
			params.add(new BasicNameValuePair("msisdn", msisdn));
			httpPost.setEntity(new UrlEncodedFormEntity(params,StandardCharsets.UTF_8.name()));
			Logging.getLogger().info("** asGetImageRequest URL : " + getImgURL+ ", PARAMS : "+params); 
			RequestConfig conf = RequestConfig.custom().setConnectTimeout(httpTimeout).setConnectionRequestTimeout(httpTimeout).setSocketTimeout(httpTimeout).build();
			httpPost.setConfig(conf);
			CloseableHttpResponse imgresponse = client.execute(httpPost);
			Logging.getLogger().info("asGetImage Response : StatusCode = " + imgresponse.getStatusLine().getStatusCode());
			if (imgresponse.getStatusLine().getStatusCode() == 200) {
				BufferedReader br = new BufferedReader(new InputStreamReader(imgresponse.getEntity().getContent()));
				String readLine;
				while (((readLine = br.readLine()) != null)) {
					imageStr.append(readLine);
				}
			}
			SecureImageResponse result = new Gson().fromJson(imageStr.toString(), SecureImageResponse.class);
			String img1 = result.getImage1();
			String img2 = result.getImage2();
			String txt = result.getPimage();
			String pshare = "YES";
			if(itpe.equalsIgnoreCase("3a")) {
				rd = request.getRequestDispatcher("/WEB-INF/jsp/aoc.jsp");
			}else {
				rd = request.getRequestDispatcher("/WEB-INF/jsp/otp.jsp");
			}

			time = purchaseDetailValidationRepositoryImpl.getCGSessionTimer();
			time = (time==null || time.equalsIgnoreCase("0") || time.equalsIgnoreCase("null") || Integer.valueOf(time)<=0)?String.valueOf(sessionTimeout):time;

			request.setAttribute("img1", img1);
			request.setAttribute("img2", img2);
			request.setAttribute("optxn", optxn);
			request.setAttribute("pshare", pshare);
			request.setAttribute("pimg", txt);
			request.setAttribute("mimgURL", pur.getSerimgurl());
			request.setAttribute("pr", pur.getPrice());
			request.setAttribute("val", pur.getValidity());
			request.setAttribute("srvName", pur.getSerName());
			request.setAttribute("merchant", pur.getSerDesc());
			request.setAttribute("t", String.valueOf(time));
			request.setAttribute("checkforOtpFlow", ((itpe.equalsIgnoreCase("8c"))?"true":"false"));
			try {
				rd.forward(request, response);
			} catch (ServletException | IOException  e) {
				ErrorLogger.getLogger().info("Exception on redirectToJsecureImage : " + e.getMessage());
			} 
			if(result.getStatusCode() != null && result.getStatusCode().equalsIgnoreCase("JS201")){
				status = SUCCESS_RESPONSE;
			}else {
				status = FAILURE_RESPONSE;
			}
		}
		return status;
	}

	/** 
	 * 
	 * @param optxn
	 * @param param5
	 * @param response
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws ParseException
	 */

	@RequestMapping(value = "/chkImg")
	public @ResponseBody void getPurchaseDetails(@RequestParam(value="cgtrxId") String optxn, @RequestParam(value="param5") String param5,
			@RequestParam(value="en", required = false) String aesplatform,
			HttpServletResponse response, HttpServletRequest request) throws ClientProtocolException, IOException, ParseException {
		MDC.put("Robi-UNIQUE-ID", optxn);
		PurchaseRequestDetail pur = null;
		String dbstatus = "";
		String status = "";
		String respCode = "";
		Subscription sub = null;
		Purchases purchase = null;
		try {
			String decrypted_aesdata = null;
			//AES encryption Alogorithm from here
			aesplatform = (aesplatform!=null && !aesplatform.equalsIgnoreCase(""))?URLDecoder.decode(aesplatform, "UTF-8"):"";
			if (aesplatform != null && aesplatform.split("::").length == 3) {
				AesEncryptDecrypt aesEncryptDecrypt = new AesEncryptDecrypt(128, 100);
				Logging.getLogger().info("*********************AES Encrypted_platform from JS - " + aesplatform);
				String iv = aesplatform.split("\\::")[0];
				String salt = aesplatform.split("\\::")[1];
				String ciphertext = aesplatform.split("\\::")[2];

				Logging.getLogger().info("*********************AES encrypted value of aesplatform --> salt : " +salt+", iv : "+iv+", ciphertext : "+ciphertext);
				decrypted_aesdata = aesEncryptDecrypt.decrypt(salt, iv, optxn, ciphertext);
				Logging.getLogger().info("*********************AES Decrypted platform from JS - " + decrypted_aesdata);
			}

			String platform = decrypted_aesdata!=null?URLDecoder.decode(decrypted_aesdata.split("\\*")[0],"UTF-8"):"null";
			String scn_Size = decrypted_aesdata!=null?decrypted_aesdata.split("\\*")[1]:"null";
			String nav_bua = decrypted_aesdata!=null?URLDecoder.decode(decrypted_aesdata.split("\\*")[2],"UTF-8"):"null";
			String oscpu = decrypted_aesdata!=null?URLDecoder.decode(decrypted_aesdata.split("\\*")[3],"UTF-8"):"null";

			Logging.getLogger().info("*********************AES encrypted data --> Navigator_Platform : "+platform+
					", Navigator_userAgent : "+nav_bua+", ScreenWidthHeight : "+scn_Size+", Navigator_oscpu : "+oscpu);

			//Retrieve Header Parameters for Chk Image Request
			String remoteIp = request.getRemoteAddr() != null ? request.getRemoteAddr() : "null";
			String xforwardedIP = request.getHeader("X-FORWARDED-FOR") != null ? request.getHeader("X-FORWARDED-FOR") : "null";
			String clientIP = request.getHeader("CLIENT_IP") != null ? request.getHeader("CLIENT_IP") : "null";
			String referer = request.getHeader("referer") != null ? request.getHeader("referer") : "null";
			String acpt = request.getHeader("accept") != null ? request.getHeader("accept") : "null";
			String userAgent = request.getHeader("user-agent") != null ? request.getHeader("user-agent") : "null";
			String xreqWithRef = request.getHeader("x-requested-with")!= null ? request.getHeader("x-requested-with") : "null";
			String reqIframe = request.getHeader("sec-fetch-dest")!=null ? request.getHeader("sec-fetch-dest") : "null";

			String mip = Stream.of(xforwardedIP, remoteIp, clientIP)
					.filter(s -> s != null && !s.isEmpty() && !s.equalsIgnoreCase("null"))
					.collect(Collectors.joining("-"));

			Logging.getLogger().info("*** ChkImg Parameters *** purchaseId : " + optxn + ", UserAgent : " + userAgent +", remoteIp : " + remoteIp +
					", X-forwardedIP : " + xforwardedIP + ", clientIP : " + clientIP + ", Referer : " + referer+", acpt : "+acpt+
					", param5 : "+param5 +", xreqWithRef : "+ xreqWithRef + ", reqIframe : "+ reqIframe);

			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			PaymentResponse payResp = null;
			Purchases purc = null;
			SmsResponse smsresp = null;
			try(CloseableHttpClient client = HttpClients.createDefault()){
				StringBuilder imageStr = new StringBuilder();
				String sig = null;
				String datatobehashed = optxn + param5;
				String secretKey = "abcde";
				sig = CommonHelper.GenerateSign(secretKey, datatobehashed);
				//Add ip2,bua2,plf,scrnsize to AS
				HttpGet httpGet = new HttpGet(checkImgURL + "?optxn=" + optxn + "&param5=" + param5 + "&sig=" + sig
						+"&bua=" + URLEncoder.encode(nav_bua, "UTF-8") + "&ip=" + mip + "&plf=" + URLEncoder.encode(platform,"UTF-8") + 
						"&srnsize=" + URLEncoder.encode(scn_Size,"UTF-8")+"&oscpu=" + URLEncoder.encode(oscpu,"UTF-8"));

				Logging.getLogger().info("** asChkImageRequest URL : " + httpGet.toString()); 

				CloseableHttpResponse imgresponse = client.execute(httpGet);
				if (imgresponse.getStatusLine().getStatusCode() == 200) {
					BufferedReader br = new BufferedReader(new InputStreamReader(imgresponse.getEntity().getContent()));
					String readLine;
					while (((readLine = br.readLine()) != null)) {
						imageStr.append(readLine);
					}
				}
				SecureImageResponse result = new Gson().fromJson(imageStr.toString(), SecureImageResponse.class);
				Logging.getLogger().info("** AOC Response on chkImg : " + result.getResult() + ", StatusCode : " + result.getStatusCode());
				pur = purchaseDetailValidationRepositoryImpl.getPurchaseRequestDetail(optxn);

				if(pur!=null){
					pur.setAshieldResp(result.getResult());
					pur.setConsentTime(auth.getStrDate());
					pur.setBua2(nav_bua);
					pur.setIp2(mip);
					pur.setPlatform(platform);
					pur.setScrnSize(scn_Size);

					if(result.getStatusCode().equalsIgnoreCase("JS205")) {
						status = SESSION_TIMEOUT;
						pur = XReqWithAnalysisFromDB(pur);
						CDRLogs.getCDRWriter().logCDR(pur, status);
						infocp.redirectTOCP(request, response, pur, status, REDIRECT);
					}else if(result.getResult() != null && result.getResult().equalsIgnoreCase("YES")){
						/** Payment API call */
						payResp = PaymentApi(pur);

						if(payResp != null) {
							Logging.getLogger().debug("SERVICE MESSAGE : " + payResp.getSerMessageId()
							+ ", POLICY MESSAGE : " + payResp.getPolMessageId());

							if(payResp.getTransactionOperationStatus().equalsIgnoreCase("Failed") || payResp.getSerMessageId() != null
									|| payResp.getPolMessageId() != null){
								if(payResp.getSerMessageId().equalsIgnoreCase("SVC0001")){
									status = MSISDN_NOT_ACTIVE;
								}else if(payResp.getSerMessageId().equalsIgnoreCase("SVC0002")){
									status = ACTIVATION_WITHIN_30DAYS; 
								}else if(payResp.getCode().equalsIgnoreCase("900901")) {
									status = INVALID_CREDENTIALS;
								}else if(payResp.getPolMessageId().equalsIgnoreCase("POL0001")){
									status = MSISDN_BLOCKED;
								}
								if(!status.equalsIgnoreCase("")){
									pur = XReqWithAnalysisFromDB(pur);
									CDRLogs.getCDRWriter().logCDR(pur, status);
									infocp.redirectTOCP(request, response, pur, status, REDIRECT);
									return;
								}
							}else {
								Logging.getLogger().debug("No service & policy Error");
							}

							if(!(payResp.getTransactionOperationStatus().equalsIgnoreCase("Failed"))) {
								pur.setEndTime(auth.getEndDate(pur.getValidity(), pur.getAction()));
								pur.setTotalAmountCharged((payResp.getTotalAmountCharged()==null || payResp.getTotalAmountCharged().equalsIgnoreCase("null") ||
										payResp.getTotalAmountCharged().equalsIgnoreCase(""))?"0.0":payResp.getTotalAmountCharged());
								pur.setSerDesc((payResp.getDescription()==null ||payResp.getDescription().equalsIgnoreCase("") ||
										payResp.getDescription().equalsIgnoreCase("null"))?pur.getSerName():payResp.getDescription());
							}
							purc = new Purchases();
							purc.setAmount(pur.getPrice());
							purc.setChargeTime(sf.parse(auth.getStrDate()));
							purc.setCpTxId(pur.getCptxnid());
							purc.setCur(pur.getCurrency());
							purc.setValidity(pur.getValidity());
							purc.setMsisdn(pur.getMsisdn());
							purc.setNetype("cellular");
							purc.setOperator("Robi");
							purc.setPurchaseId((payResp.getClientCorrelator()==null || payResp.getClientCorrelator().equalsIgnoreCase("") || 
									payResp.getClientCorrelator().equalsIgnoreCase("null"))?pur.getPurchaseId():payResp.getClientCorrelator());
							purc.setRequestTime(sf.parse(pur.getRequestTime()));
							purc.setServiceName(pur.getSerName());
							purc.setServiceProvName(pur.getSerProvName());
							purc.setSpId(pur.getSpid());
							pur.setTotalAmountCharged((payResp.getTotalAmountCharged()==null || payResp.getTotalAmountCharged().equalsIgnoreCase("null") ||
									payResp.getTotalAmountCharged().equalsIgnoreCase(""))?"0.0":payResp.getTotalAmountCharged());
							if(payResp.getTransactionOperationStatus() != null) {
								if(payResp.getTransactionOperationStatus().equalsIgnoreCase("Charged")) {
									purc.setAction(pur.getAction().equalsIgnoreCase("act") ? "act" : pur.getAction());
									purc.setTotalAmountCharged(payResp.getTotalAmountCharged());
									purc.setJunoStatus("J201");
									purc.setCharged("Y");
									status = SUCCESS;
									if(!pur.getAction().equalsIgnoreCase("PD")) {
										respCode = infocp.redirectTOCP(request, response, pur, status, SERVER);
									}
									Logging.getLogger().info("Server To Server Callback response : " + respCode);

									if(pur.getValidity().equalsIgnoreCase("PD")) {
										smsresp = smsApi(pur.getMsisdn(), pur.getSpid(), optxn, "null", pur.getValidity(), pur.getSerName(), pur.getPrice(), "PD", pur.getSmsShortCode());
									} else {
										smsresp = smsApi(pur.getMsisdn(), pur.getSpid(), optxn, pur.getEndTime(), pur.getValidity(), pur.getSerName(), pur.getPrice(), "act", pur.getSmsShortCode());
									}
								}else if(payResp.getTransactionOperationStatus().equalsIgnoreCase("Failed") || 
										payResp.getCode().equalsIgnoreCase("900800")) {
									purc.setJunoStatus("J202");
									purc.setAction(pur.getAction().equalsIgnoreCase("act") ? "act" : pur.getAction());
									purc.setCharged("P");
									status = LOW_BAL;
									smsresp = smsApi(pur.getMsisdn(), pur.getSpid(), optxn, pur.getEndTime(), pur.getValidity(), pur.getSerName(), pur.getPrice(), "act_low_bal", pur.getSmsShortCode());
								}else {
									purc.setJunoStatus("J107");
									purc.setAction(pur.getAction().equalsIgnoreCase("act") ? "act" : pur.getAction());
									purc.setCharged("P");
									status = NETWORK_ERROR;
								}
							}

							//add consent status, bua, xreqwith, referer in DB on 09/04/2020 @Swe
							purc.setCgstatusCode("Y");
							purc.setSourceApp(pur.getXReqWithRef());
							purc.setBrowser(pur.getBua());
							purc.setAdsource(pur.getReferer());
							
							dbstatus = subscriptionInfoServiceImpl.insertNewActivation(purc);
							
							if(dbstatus.equalsIgnoreCase(SUCCESS_RESPONSE)) {
								Logging.getLogger().info("DATA inserted Successfully in Purchases table");
							} else {
								Logging.getLogger().info("DATA Insertion Failed - Purchases table");
							}
							//to-do , dont call below api on PD requests
							if(!pur.getValidity().equalsIgnoreCase("PD")) {
								subRetry.SubsInsertonChargeResponse(pur, purchase , sub , pur.getValidity(), status);
							}
							pur = XReqWithAnalysisFromDB(pur);
							CDRLogs.getCDRWriter().logCDR(pur, status);
							infocp.redirectTOCP(request, response, pur, status, REDIRECT);
						} else {
							status = NETWORK_ERROR;
							pur = XReqWithAnalysisFromDB(pur);
							CDRLogs.getCDRWriter().logCDR(pur, status);
							infocp.redirectTOCP(request, response, pur, status, REDIRECT);
						}
					}else if(result.getResult() != null && result.getResult().equalsIgnoreCase("NO") || result.getResult().equalsIgnoreCase("NONE")){
						status = USER_CANCELLED_THE_REQUEST;
						pur = XReqWithAnalysisFromDB(pur);
						CDRLogs.getCDRWriter().logCDR(pur, status);
						infocp.redirectTOCP(request, response, pur, status, REDIRECT);
					}
				} else { //session timeout deleted from redis
					Logging.getLogger().info("DATA Doesn't exists in Redis (Already Session Timed out) for PurchaseId - "+optxn);
				}
			} catch (InvalidKeyException e){
				ErrorLogger.getLogger().error("Exception in chkImg PurchaseDetails : " + e.getMessage());
			}
		} catch (JsonSyntaxException | NoSuchAlgorithmException | UnsupportedOperationException e ) {
			ErrorLogger.getLogger().error("Exception in chkImg PurchaseDetails : " + e.getMessage());
		} finally{
			MDC.remove("Robi-UNIQUE-ID");
		}
	}

	/**
	 * 
	 * @param txn
	 */
	@RequestMapping(value = "/sessTOut")
	public @ResponseBody void PurchaseSessionTimedOut(@RequestParam(value="txn") String optxn, HttpServletResponse response, HttpServletRequest request){
		MDC.put("Robi-UNIQUE-ID", optxn);
		PurchaseRequestDetail pur = null;
		String status = "";
		try {
			Logging.getLogger().info("** AOC SessionTimeout Request from JSP, optxn : "+optxn);
			pur = purchaseDetailValidationRepositoryImpl.getPurchaseRequestDetail(optxn);
			if(pur!=null){
				status = SESSION_TIMEOUT;
				pur = XReqWithAnalysisFromDB(pur);
				CDRLogs.getCDRWriter().logCDR(pur, status);
				purchaseDetailValidationRepositoryImpl.deletePurchaseRequestDetail(optxn);
				infocp.redirectTOCP(request, response, pur, status, REDIRECT);
			} else {
				Logging.getLogger().info("DATA Doesn't exists in Redis (Already Session Timed out) for PurchaseId - "+optxn);
			}
		} catch (Exception e ) {
			ErrorLogger.getLogger().error("Exception in PurchaseSessionTimedOut : " + e.getMessage());
		}
	}

	//API to set CG session Timer 
	@RequestMapping(value = "/setCgTimer")
	public @ResponseBody String setCgPageExpireTime(@RequestParam(value="timer") int sestimer, HttpServletResponse response, HttpServletRequest request){
		String status = FAILURE_RESPONSE;
		try {
			Logging.getLogger().info("**setCgTimer - Setting CG Session Timer : "+sestimer);
			if(sestimer>0){
				//set in redis/check in redis if data already exists, update value for rediskey
				String timer = "0"; 
				timer = purchaseDetailValidationRepositoryImpl.getCGSessionTimer();
				if(timer==null || timer.equalsIgnoreCase("0") || timer.equalsIgnoreCase("null") || Integer.valueOf(timer)>0){
					purchaseDetailValidationRepositoryImpl.setCGSessionTimer(sestimer);
				} 
				status = SUCCESS_RESPONSE;
			} else if(sestimer==0){
				purchaseDetailValidationRepositoryImpl.deleteCGSessionTimer();
			}
		} catch (Exception e ) {
			ErrorLogger.getLogger().error("Exception in setCgPageExpireTime : " + e);
		}
		return status;
	}

	/**
	 * 
	 * @param purchaseId
	 * @param spId
	 * @param msisdn
	 * @return
	 */
	public String tokenCheck(String spId, String msisdn) {
		String accToken = null;
		accToken = tokenRepoImpl.getAccessTokenKeyinRedis(spId);
		if(accToken == null) {
			accToken = auth.GenerateAccessToken(msisdn, spId);
		}
		return accToken;
	}

	/**
	 * 
	 * @param pur
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public PaymentResponse PaymentApi(PurchaseRequestDetail pur) throws UnsupportedEncodingException{
		ClientConfig config = new DefaultClientConfig();  
		Client client = Client.create(config);  
		PaymentResponse pResp = null;
		WebResource payService = null;
		String responseBody = null;
		Gson gson = new Gson();
		String token = tokenCheck(pur.getSpid(), pur.getMsisdn());
		String msisdn = URLEncoder.encode("tel:+"+ pur.getMsisdn(), "ISO-8859-1");
		String httpUrl = "/payment/v1/" + msisdn + "/transactions/amount";
		payService = client.resource(robiUrl).path(httpUrl);
		try {
			PaymentRequest payreq = new PaymentRequest(pur);
			String urlparams = gson.toJson(payreq);
			Logging.getLogger().info("** PAYMENT-REQUEST ==> " + payService + ", " + urlparams);
			responseBody = auth.SubRestHttprequest(payService, urlparams, "POST", token);
			if(responseBody != null){
				pResp = gson.fromJson(responseBody, PaymentResponse.class);
				Logging.getLogger().info("** PAYMENT-RESPONSE ==> " + responseBody);
				if(pResp != null) {
					if(pResp.getTransactionOperationStatus().equalsIgnoreCase("charged")) {
						Logging.getLogger().info("****Payment API Response : AmtCharged - " + pResp.getTotalAmountCharged()+", Msisdn - "+pResp.getEndUserId()+
								", SvrRefCode - "+pResp.getServerReferenceCode()+", TrnOpStatus - "+pResp.getTransactionOperationStatus()+", clientco - "+pResp.getClientCorrelator()
								+", refCode - "+pResp.getReferenceCode()+", OnbhfOf - "+pResp.getOnBehalfOf()+", Desc - "+pResp.getDescription());
					}else if(pResp.getTransactionOperationStatus().equalsIgnoreCase("charged") && !pResp.getPolMessageId().equalsIgnoreCase("null")){
						Logging.getLogger().debug("****Payment API Response (PolicyException Occured): MsgId - "+pResp.getPolMessageId()
						+", Variable - "+pResp.getPolVariables());
					}else if(pResp.getTransactionOperationStatus().equalsIgnoreCase("charged") && !pResp.getSerMessageId().equalsIgnoreCase("null")){
						Logging.getLogger().debug("****Payment API Response (serviceException Occured): MsgId - "+pResp.getSerMessageId()
						+", Variable - "+pResp.getSerVariables()+", Text - "+pResp.getSerText());
					}else {
						Logging.getLogger().info("****Payment API Response (SOME ERROR OCCURED)");
					}
				}
			}
		} catch (JsonSyntaxException e) {
			ErrorLogger.getLogger().error("Exception in Payment API - " + e.getMessage());
		}
		return pResp;
	}

	/**
	 * 
	 * @param sub
	 * @param smsType
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public SmsResponse smsApi(String msisdn, String spid, String purId, String endTime, String val, String serDesc, String amount,
			String smsType, String shortcode) throws UnsupportedEncodingException {
		SmsResponse smsResp = null;
		try{
			ClientConfig config = new DefaultClientConfig();  
			Client client = Client.create(config);  
			WebResource smsService = null;
			String responseBody = null;
			Gson gson = new Gson();
			String enmsisdn = URLEncoder.encode("tel:" + msisdn ,"ISO-8859-1");
			String httpUrl = "/smsmessaging/v1/outbound/" + enmsisdn + "/requests";
			smsService = client.resource(robiUrl).path(httpUrl);
			String token = tokenCheck(spid, msisdn);
			Logging.getLogger().info("SMS API Token : "+token+", SPID = "+spid);
			String message = smsMessage.smsformat(val, serDesc, amount, endTime, smsType, spid);
			SmsRequest smsreq = new SmsRequest(msisdn, message, purId, shortcode);
			String urlparams = gson.toJson(smsreq);
			Logging.getLogger().info("** SMS-REQUEST ==> " + smsService + ", " + urlparams);
			responseBody = auth.SubRestHttprequest(smsService, urlparams, "POST", token);
			if(responseBody != null) {
				smsResp = gson.fromJson(responseBody, SmsResponse.class);
				Logging.getLogger().info("** SMS-RESPONSE ==> " + responseBody);
			}
		} catch(Exception e){
			ErrorLogger.getLogger().error("Exception in smsApi : "+e.getMessage());
		}
		return smsResp;
	}

	public void dataForPreRenewalSMS() throws UnsupportedEncodingException {
		int SmsNotify = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			//Using SpId checks
			/*String preSmsSpId = subscriptionInfoServiceImpl.findSPreSmsSpId(); 
			if(preSmsSpId!=null && !preSmsSpId.equalsIgnoreCase("")){
				List<Subscription> subs = subscriptionInfoServiceImpl.SubRenewalSMS(preSmsSpId, null);*/

			//Using ServiceName checks
			String preSmsServiceName = subscriptionInfoServiceImpl.findSubPreSmsServiceName();
			if(preSmsServiceName!=null && !preSmsServiceName.equalsIgnoreCase("")){	
				List<Subscription> subs = subscriptionInfoServiceImpl.SubRenewalSMS(null, preSmsServiceName);

				Logging.getLogger().info("Pre-SMS subs List : "+subs);
				if(subs != null && subs.size() != 0){
					for (Subscription sub : subs) {
						Logging.getLogger().info("Subscription params from Subscription tables for Renew SMS .... "
								+":requestId-"+sub.getPurchaseId()
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
								+":smsCount-"+sub.getSmsCount()
								+":retryDay-"+sub.getRetryDays());
						sdf.setTimeZone(TimeZone.getTimeZone("GMT+6"));
						Calendar c = Calendar.getInstance();
						if(sub.getValidity().equalsIgnoreCase("D1")){
							c.add(Calendar.HOUR, -2);
						}else if(sub.getValidity().equalsIgnoreCase("W1") || sub.getValidity().equalsIgnoreCase("M1")) {
							c.add(Calendar.DATE, -1);
						}
						String cdate = sdf.format(c.getTime());
						SmsNotify = SmsInterval.getInterval(cdate, sub.getValidity());
						//System.out.println("***** pre-sms --> sms cdate : "+cdate+", smsNotify : "+SmsNotify+",endtime : "+sub.getEndTime());
						if((sub.getEndTime().compareTo(c.getTime()) == 1) && (SmsNotify == 1)){
							Logging.getLogger().info("!!! Notifying the User with Pre-Renewal SMS !!! ");
							Logging.getLogger().info("@@@@@ Performing Pre renewal sms for Msisdn : " + sub.getMsisdn());
							Thread.sleep(apiDelayTimer);
							String shortcode = subscriptionInfoServiceImpl.findShortCodeBySpId(sub.getSpId());
							smsApi(sub.getMsisdn(), sub.getSpId(), sub.getPurchaseId(), CommonHelper.getStrDateFromDateFormat(sub.getEndTime()), sub.getValidity(), sub.getServiceName(), sub.getAmount(), "rem", shortcode);
						}
					}
				}
			}
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in PreRenewalSMS- " + e.getMessage());
		}
	}

	/* De-activation Portal Code */
	//CCGUI Msisdn Validation API
	@RequestMapping(value = "/MSISDNValidate")
	public @ResponseBody void getCCGuiMsisdnValidationDetails(@RequestParam(value="msisdn") String mobileNumber, 
			HttpServletRequest request, HttpServletResponse response ) throws IOException { 

		PrintWriter out = response.getWriter();
		try{
			Subscription dbResp = subscriptionInfoServiceImpl.checkSubMsisdn(mobileNumber); 
			if(dbResp!=null){
				Logging.getLogger().info("MSISDN Available - "+mobileNumber);
				response.getWriter().print(true);
			}else{
				Logging.getLogger().info("MSISDN Not Available - "+mobileNumber);
				response.getWriter().print(false);
			}
		}catch(Exception e){
			ErrorLogger.getLogger().error("Exception in MSISDNValidate : ",e);
		}finally{
			out.close();
		}
	}

	//CCGUI Login API
	@RequestMapping(value = "/adminLoginForm")
	public @ResponseBody void getCCGuiLoginDetails(@RequestParam(value="username") String usr,
			@RequestParam(value="password") String pwd,   
			HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException { 
		LoginDetails loginValues = null;
		try {
			Logging.getLogger().info("CC GUI Login request for User : "+usr+", pwd : "+pwd);
			loginValues = subscriptionInfoServiceImpl.findLoginDetailsByUsr(usr); 

			RequestDispatcher rd;
			if(loginValues!=null){
				session.setAttribute("uname", usr);
				session.setAttribute("pwd", pwd);
				session.setAttribute("cid", loginValues.getSpId());
				rd = request.getRequestDispatcher("/WEB-INF/jsp/cc.jsp");
			}else{
				request.setAttribute("errorMessage","Username or Password is wrong");
				rd = request.getRequestDispatcher("/WEB-INF/jsp/login.jsp");
			}
			rd.forward(request, response);
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in adminLoginForm : ",e);
		}
	}

	//CCGUI Data Extraction API
	@RequestMapping(value = "/RobiMsisdnDataReports")
	public @ResponseBody void getDeactivationDataToDashboard(@RequestParam(value="msisdn") String mobileNumber, 
			@RequestParam(value="cpid") String spid,
			HttpServletRequest request, HttpServletResponse response ) throws IOException { 
		PrintWriter out = response.getWriter();
		RequestDispatcher rd;
		Logging.getLogger().info("CC-Requested RobiMsisdnDataReports, Msisdn : "+mobileNumber+", Spid : "+spid);
		try{
			List<Subscription> tableDetailsList = subscriptionInfoServiceImpl.getMsisdnRecords(mobileNumber, spid); 

			//Implemeted on 06/04/2020 @Swe
			//From extracted data, check for 1st activation request and extract as below and display same on UI(Customer Care Portal).

			/*	Request DateTime:2020-04-01 18:12:43
			Ad Source:http://clickmob.c0c.xyz/rest/ck/o/1686/4492462?
			Source APP:com.aktham.calculator
			Browser:Mozilla/5.0 (Linux; Android 4.4.2; Hol-U19 Build/HUAWEIHol-U19) AppleWebKit/537.36 (KHTML- like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36
			Network:Data
			Device OS:Android
			Device Model: HUawei
			Consent: Success	*/

			Logging.getLogger().info("Response MSISDN Data List Size --> "+tableDetailsList.size());

			if(tableDetailsList.size()>0){
				//Last Activation record extraction
				//get Purchase details from Purchases table
				Purchases pur = subscriptionInfoServiceImpl.getUserPurchaseInfo(tableDetailsList.get(0).getMsisdn(), 
						tableDetailsList.get(0).getPurchaseId(), tableDetailsList.get(0).getServiceName());
				request.setAttribute("reqTime", pur.getRequestTime().toString());
				request.setAttribute("network", (pur.getNetype()!=null && pur.getNetype().equalsIgnoreCase("cellular"))?"Data":"Wifi");
				request.setAttribute("consent", (pur.getCgstatusCode()!=null && pur.getCgstatusCode().equalsIgnoreCase("Y"))?"Success":"Failure");
				request.setAttribute("adsrc", pur.getAdsource()!=null?pur.getAdsource():"null");
				request.setAttribute("bua", pur.getBrowser()!=null?pur.getBrowser():"null");
				request.setAttribute("srcapp", pur.getSourceApp()!=null?pur.getSourceApp():"null");
				
				if(pur.getBrowser()!=null){
					PurchaseRequestDetail purReq = new PurchaseRequestDetail();
					purReq.setBua(pur.getBrowser());
					purReq = CommonHelper.extractBuaInfo(purReq);
					
					request.setAttribute("devos", (purReq.getDevOsName()!=null && !purReq.getDevOsName().equalsIgnoreCase("NA"))?purReq.getDevOsName():"null");
					request.setAttribute("devmodel", (purReq.getDeviceModel()!=null && !purReq.getDeviceModel().equalsIgnoreCase("NA"))?purReq.getDeviceModel():"null");
				}
			}

			request.setAttribute("tableData", tableDetailsList);
			rd = request.getRequestDispatcher("/WEB-INF/jsp/responseMsisdnDetails.jsp");
			rd.forward(request, response);
		}catch (Exception e){
			ErrorLogger.getLogger().error("Exception in RobiMsisdnDataReports : ", e);			
		}finally{
			out.close();
		}
	}

	@RequestMapping(value = "/RobiDeactivateRequest")
	public @ResponseBody void getdeactivationRequestFromCCGui(@RequestParam(value="deactivateId") String deactivateId, 
			@RequestParam(value="userid") String loginUserId,
			@RequestParam(value="cpid", required=false) String spid,
			HttpServletRequest request, HttpServletResponse response ) throws IOException, ParseException { 

		PrintWriter out = response.getWriter();
		Logging.getLogger().info("CC-Deactivate RobiDeactivateRequest RequestedId : "+deactivateId);
		String responseCode = "";
		PurchaseRequestDetail dctCdrReq =null;
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{
			Subscription sub = subscriptionInfoServiceImpl.findSubDataByPurId(deactivateId); 
			if(sub != null) {
				String requestId = sub.getPurchaseId();
				String srvName = sub.getServiceName();
				String validity = sub.getValidity();
				String msisdn = sub.getMsisdn();
				String status = sub.getJunoStatus();
				String action = sub.getAction();

				String dbResp = null;
				spid = spid==null?"null":spid;
				//get data from Purchases table using Subscription Data
				Purchases purReq = null;
				purReq = subscriptionInfoServiceImpl.getUserPurchaseInfo(sub.getMsisdn(), sub.getPurchaseId(), sub.getServiceName());

				//Create Purchase Details for Callback and CDR data
				dctCdrReq = new PurchaseRequestDetail();
				int session = (int) (Math.random()*9779) + 6000;
				String purId = Integer.toString(session) + sub.getPurchaseId();

				dctCdrReq.setSpid(sub.getSpId());
				dctCdrReq.setCptxnid(purReq.getCpTxId());
				dctCdrReq.setPrice(purReq.getAmount());
				dctCdrReq.setCurrency(purReq.getCur());
				dctCdrReq.setSerName(purReq.getServiceName());	
				dctCdrReq.setSerProvName(sub.getSerProvName());
				dctCdrReq.setValidity(sub.getValidity());
				dctCdrReq.setPurchaseId(purId);
				dctCdrReq.setRequestTime(CommonHelper.getStrDate());
				dctCdrReq.setMsisdn(sub.getMsisdn());
				dctCdrReq.setAction("dct");
				dctCdrReq.setNetType("ccgui");
				dctCdrReq.setPrice("0");

				if(action.equalsIgnoreCase("act") || action.equalsIgnoreCase("rnw")){
					responseCode = SUCCESS;
					//Create Dummy Purchase Request for DCT request and store in Purchases DB
					robirnw.createDctRequestForApiFailure(sub, purId, purReq, "ccgui");
					String callbackstat = infocp.redirectTOCP(request, response, dctCdrReq, responseCode, Constants.SERVER);
					Logging.getLogger().info("Sending CCGui DCT Callback to merchant : "+callbackstat);

					String shortcode = subscriptionInfoServiceImpl.findShortCodeBySpIdAndSrvName(sub.getSpId(), sub.getServiceName());
					if(shortcode!=null && !shortcode.equalsIgnoreCase("null")){
						smsApi(sub.getMsisdn(), sub.getSpId(), purReq.getPurchaseId(), "null", sub.getValidity(), sub.getServiceName(), sub.getAmount(), "dct", shortcode);
					} else {
						responseCode = NETWORK_ERROR;
					}
					Logging.getLogger().info("Update CC-Deactivate in DB : "+dbResp+", Dct-Status : "+responseCode);

				} else if(action.equalsIgnoreCase("dct")){
					responseCode = SUCCESS;
				}
				else {
					responseCode = NETWORK_ERROR;
				}
				CDRLogs.getCDRWriter().logCDR(dctCdrReq, responseCode);
				out.print(responseCode);
			} else {
				responseCode = NETWORK_ERROR;
				out.print(responseCode);
			}
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in RobiDeactivateRequest : ",e);
		}finally{
			out.close();
		}
	}	


	/* Method to Validate XReqWith Data with PlayStore App Details from Database and write CDR */
	//Added on 07/04/2020 @Swe
	//private void XReqWithAnalysisFromDB (PurchaseRequestDetail pur, String StatusCode){
	public PurchaseRequestDetail XReqWithAnalysisFromDB (PurchaseRequestDetail pur){

		//App Name/ App Package Name Analyser
		if(pur.getXReqWithRef()!=null && !(pur.getXReqWithRef().equalsIgnoreCase("0") || pur.getXReqWithRef().equalsIgnoreCase("null"))){
			AppRefererDetails appDetails = null;
			appDetails = subscriptionInfoServiceImpl.findAppReferer(pur.getXReqWithRef());
			if(appDetails!=null){
				pur.setPlayStoreApp((appDetails.getAppStatus()!=null && 
						!(appDetails.getAppStatus().equalsIgnoreCase("NULL") || appDetails.getAppStatus().equalsIgnoreCase(" ") || appDetails.getAppStatus().equalsIgnoreCase("0")))
						?appDetails.getAppStatus():"NA");
			} else {
				//check for atleast one dot or GMT check
				int dotcnt = 0; 
				dotcnt = pur.getXReqWithRef().trim().split("\\.").length;
				pur.setPlayStoreApp((pur.getXReqWithRef().contains("GMT") || dotcnt<=1)?Constants.INVALID_PLAY_STORE_APP:"NA");
			}
		}

		//BUA Info Extractor (DeviceOSName, DeviceModel, DeviceBrowser details) 
		pur = CommonHelper.extractBuaInfo(pur);
		return pur;
		//CDRLogs.getCDRWriter().logCDR(pur, StatusCode);
	}

	/* API to Insert PlayStore App Name */
	//Added on 07/04/2020 @Swe
	@RequestMapping(value = "/AddAppName")
	public @ResponseBody String insertAppPkgNameToDB(@RequestParam(value = "param", required = false) String param, 
			@RequestParam(value = "value", required = false) List<String> value) {
		try {
			ArrayList<String> wlist = null;
			Logging.getLogger().info("*****InsertAppPkgNameToDB / AddAppName, param : "+param+", value : "+value+"*****");
			switch(param){
			case "appname":
				wlist = appPkgNameList;
				break;

			default: break;
			}
			if(value.size()==0 || value.equals("") || value==null){
				wlist.clear();
			} else {
				wlist.clear();
				wlist.addAll(value);
				//Insert into DB and clear data from list.
				subscriptionInfoServiceImpl.insertAppPkgName(wlist);
				wlist.clear();
			}
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in insertAppPkgNameToDB : " + e);
			return Constants.FAILURE_RESPONSE;
		}
		return Constants.SUCCESS_RESPONSE;
	}
}