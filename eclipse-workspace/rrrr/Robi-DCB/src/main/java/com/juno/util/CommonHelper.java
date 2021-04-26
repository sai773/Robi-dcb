package com.juno.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.jasper.tagplugins.jstl.core.Url;
import org.apache.log4j.MDC;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.juno.controller.PurchaseController;
import com.juno.database.model.CpCredentials;
import com.juno.database.model.CpDetails;
import com.juno.database.service.SubscriptionInfoServiceImpl;
import com.juno.datapojo.PurchaseRequestDetail;
import com.juno.datapojo.SmsMessage;
import com.juno.datapojo.SmsRequest;
import com.juno.datapojo.SmsResponse;
import com.juno.datapojo.TokenResponse;
import com.juno.logs.ErrorLogger;
import com.juno.logs.Logging;
import com.juno.redisRepo.TokenRepoImpl;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

@Component
public class CommonHelper {

	@Autowired
	TokenRepoImpl tokenRepo;

	@Autowired
	SubscriptionInfoServiceImpl impl;

	private static String robiUrl;

	@Value("${RobiURL}")    
	public void setrobiUrl(String url) {
		CommonHelper.robiUrl = url;
	}

	ClientConfig config = new DefaultClientConfig();  
	Client client = Client.create(config);  
	WebResource service = null;
	int statusCode = 0;
	String responseBody = null;
	Gson gson = new Gson();
	static MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

	public static  String GenerateSign(String secretKey, String data) throws NoSuchAlgorithmException,
	InvalidKeyException, UnsupportedEncodingException {
		Mac mac = Mac.getInstance("HmacSHA512");
		mac.init(new SecretKeySpec(secretKey.getBytes(), "HmacSHA512"));
		byte[] hexBytes = new Hex().encode(mac.doFinal(data.getBytes()));
		String securitySignature = new String(hexBytes, "UTF-8");
		return securitySignature;
	}

	public static String GenerateDcbSign(String sign) throws NoSuchAlgorithmException {
		MessageDigest md = null;
		StringBuffer sb = new StringBuffer();
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			ErrorLogger.getLogger().info("Exception at GenerateDcbSign : " + e.getMessage());
		}
		if(md != null) {
			md.update(sign.getBytes());
			byte[] byteData = md.digest();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
		}
		return sb.toString();
	}

	public boolean validateSignature(String serviceName, String cpTxnId, String action, String key, String signature) {
		boolean result = false;
		try {
			String dataToBeSigned = serviceName + cpTxnId + action + key;
			if (serviceName != null) {
				String dataSignedAtJuno = GenerateDcbSign(dataToBeSigned);
				Logging.getLogger().info("validateSignature --> DataSignedAtJuno : " + dataSignedAtJuno);
				if (dataSignedAtJuno.contentEquals(signature)) {
					result = true;
				}
			} 
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception at validateSignature, Signature mis-match - ", e);
		}
		return result;
	}

	public  String authorization(String uid, String pwd){
		String credentials = uid + ":" + pwd;
		byte[] authBytes = Base64.encodeBase64(credentials.getBytes());
		String authHeaderValue = new String(authBytes);	
		return authHeaderValue;
	}

	public String AuthRestHttprequest(WebResource service, String type, String spid) {
		String Result = null;
		ClientResponse restResponse = null;
		try {
			if(type.equalsIgnoreCase("acc")){
				//Fetch Token Credentials from DB and process 
				CpCredentials cpCred = spid!=null?impl.getTokenCredentials(spid):null;
				if(cpCred!=null){
					restResponse = service.queryParam("grant_type","password")
							.queryParam("username", URLEncoder.encode(cpCred.getUsrName(),"UTF-8"))
							.queryParam("password", URLEncoder.encode(cpCred.getPwd(),"UTF-8"))
							.queryParam("scope", "PRODUCTION")
							.header("Accept", "application/json")
							.header("Content-Type", "application/x-www-form-urlencoded") 
							.header("Authorization", "Basic " + authorization(cpCred.getConsumerKey(), cpCred.getConsumerSecret()))
							.post(ClientResponse.class, "");
				}
			}
			statusCode = restResponse.getStatus();
			responseBody = restResponse.getEntity(String.class);
			URI createdURI = restResponse.getLocation();
			Logging.getLogger().info("AuthRestHttp request and response : " +restResponse.toString());
		} catch (UniformInterfaceException | ClientHandlerException | UnsupportedEncodingException e) {
			ErrorLogger.getLogger().error("Exception at AuthRestHttprequest, access token request - " + e.getMessage());
		} 
		return responseBody; 
	}

	//TODO
	public String GenerateAccessToken(String msisdn,String spId) {
		TokenResponse tResp = null;
		WebResource accTokenService = null;
		String httpUrl = null;
		String accToken = null;
		try{
			httpUrl = "/token";
			accTokenService = client.resource(robiUrl).path(httpUrl);
			responseBody = AuthRestHttprequest(accTokenService,"acc", spId);
			Logging.getLogger().info("Generate Token Response :"+responseBody.toString());
			tResp = gson.fromJson(responseBody, TokenResponse.class); 
			Logging.getLogger().info("Generated AccessToken : "+tResp.getAccess_token());
			if(tResp != null) {
				accToken = tResp.getAccess_token();
				tokenRepo.setAccessTokenKeyinRedis(spId, accToken, tResp.getExpires_in());
			}
		}catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in GenerateAccessToken : ", e);
		}finally{
		}
		return accToken;
	}

	public String SubRestHttprequest(WebResource service, String parameters, String type, String token) {
		ClientResponse restResponse = null;
		String Result = null;
		try{
			if(type.equalsIgnoreCase("GET") && token!=null) {
				restResponse = service.queryParams(queryParams)
						.header("Accept", "application/json")
						.header("Content-Type", "application/json") 
						.header("Authorization", "Bearer " + token)
						.get(ClientResponse.class);
			} 
			else if(type.equalsIgnoreCase("POST") && token!=null) {
				restResponse = service.queryParams(queryParams)
						.header("Accept", "application/json")
						.header("Content-Type", "application/json")
						.header("Authorization", "Bearer " + token)
						.post(ClientResponse.class, parameters);
			}
			statusCode = restResponse.getStatus();
			responseBody = restResponse.getEntity(String.class);
			//			URI createdURI = restResponse.getLocation();
			Logging.getLogger().info("Rest API response : " +restResponse.toString()
			+", statusCode : "+statusCode);

			switch(statusCode){
			case 200:
			case 201:
				Result = responseBody;
				break;
			case 500:
				Logging.getLogger().info("@@@@ ROBI API ERROR : Internal Server Error - 500");
				Result = null; break;
			case 503:
				if(responseBody.indexOf("You have exceeded your quota")!=-1){
					Logging.getLogger().info("API EXCEPTION : 'Message Throttled Out'");
					Result = responseBody;
					break;
				}else{
					Logging.getLogger().info("@@@@ ROBI API ERROR : Service Temporarily Unavailable/Busy - 503");
					Result = responseBody; break;
				}
			case 404:
				Logging.getLogger().info("@@@@ ROBI API ERROR : The requested resource is not available - 404");
				Result = responseBody; break;
			case 400:
				Logging.getLogger().info("@@@@ ROBI API ERROR : API Query operation failed/Bad Request - 400");
				Result = responseBody; break;
			case 401:
				if(responseBody.indexOf("User has insufficient credit for transaction")!=-1 || 
				responseBody.indexOf("Access Token Inactive")!=-1){
					Result =responseBody;
				} else {
					Logging.getLogger().info("@@@@ ROBI API ERROR : Authentication failure/PolicyException - 401");
					Result = null; 	
				}
				break;
			case 403:
				Logging.getLogger().info("@@@@ ROBI API ERROR : Forbidden/Server error - 403");
				Result = responseBody; break;
			case 405:
				Logging.getLogger().info("@@@@ ROBI API ERROR : Method not supported(GET/POST) - 405");
				Result = responseBody; break;
			case 429:
				Logging.getLogger().info("@@@@ ROBI API ERROR : Too many Requests - 429");
				Result = responseBody; break;
			default :
				Result = null;
				break;
			}
		}
		catch (NullPointerException e){
			ErrorLogger.getLogger().error("NullPointer Exception in SubRestHttprequest : ", e);
			Result = null;
		}
		catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in SubRestHttprequest : ", e);
		}
		return Result;
	}

	public int Post2Json(String initializeurl, JSONObject json){
		int responseString = 0;
		HttpClient httpClient = new DefaultHttpClient();
		try{
			MDC.get("Robi-UNIQUE-ID");
			HttpPost postRequest = new HttpPost(initializeurl);
			StringEntity input = new StringEntity(json.toString());
			input.setContentType("application/json;charset=UTF-8");
			postRequest.setEntity(input);
			input.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
			postRequest.setHeader("Accept","application/json");
			postRequest.setEntity(input); 
			HttpResponse jsonResponse = httpClient.execute(postRequest);
			responseString = jsonResponse.getStatusLine().getStatusCode();
			Logging.getLogger().info("CALLBACK URL : "+initializeurl+",JSON : "+input +",STATUS : "+ jsonResponse.getStatusLine().getStatusCode());
		}catch(Exception e){
			ErrorLogger.getLogger().error("Exception at Post2Json : ",e);
		}finally{
			httpClient.getConnectionManager().shutdown();
		}
		return responseString;
	}


	public static String getStrDate() {
		String date = null;
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date localTime = new Date(); 
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+6"));
			date = sdf.format(localTime);
		}catch(Exception e){
			ErrorLogger.getLogger().error("Exception in StandardTimeZone : ",e);
		}
		return date;
	}

	public static int getHour() {
		String hour = "0";
		try{
			SimpleDateFormat sf = new SimpleDateFormat("HH");
			sf.setTimeZone(TimeZone.getTimeZone("GMT+6"));
			hour = sf.format(new Date());
		}catch(Exception e){
			ErrorLogger.getLogger().error("Exception in getHour : ",e);
		}
		return Integer.parseInt(hour);
	}

	public static String getEndDate(String valid, String action) {
		int sum = 0;
		String num = "";
		String duration = "";
		String res = "";
		MDC.get("Robi-UNIQUE-ID");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+6")); 
		try{
			if(valid.length() == 2) {
				duration = valid.substring(0, 1);
				num = valid.substring(1, 2);
			} else if(valid.length() > 2) {
				duration = valid.substring(0, 1);
				num = valid.substring(1, 3);
			}

			if(duration.equalsIgnoreCase("D")) {
				sum = Integer.parseInt(num);
			} else if (duration.equalsIgnoreCase("W")) {
				sum = Integer.parseInt(num) * 7;
			} else if (duration.equalsIgnoreCase("M")) {
				sum = Integer.parseInt(num) * 30;
			} else if (duration.equalsIgnoreCase("Y")) {
				sum = Integer.parseInt(num) * 365;
			}

			if(action.equalsIgnoreCase("rnw") || action.equalsIgnoreCase("act_grace")) {
				res = String.valueOf(1);	
			} else {
				//sdf.setTimeZone(TimeZone.getTimeZone("GMT+6")); 
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE, sum);
				String endDate = sdf.format(c.getTime());
				//System.out.println("endDate : "+endDate);
				res = endDate;
			} 
		}catch(Exception e){
			ErrorLogger.getLogger().error("Exception in getEndDate : ",e);
		}
		return res;
	}

	public static Date getDateFromString(String dateStr) throws ParseException {
		Date ConveredDate = null; 		
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ConveredDate = (Date) sdf.parse(dateStr);
		return ConveredDate;
	}

	public static String getStrDateFromDateFormat(Date date) {
		String strdate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		strdate = sdf.format(date);
		return strdate;
	}

	public static String getValidityInWords(String val){
		String valInWords = "";
		if(val!=null && !val.equalsIgnoreCase("0")) {
			switch(val.substring(0, 1)) {
			case "D":
				if(val.length()==2) {
					if(val.substring(1, 2).equalsIgnoreCase("1")){
						valInWords = "Day";
					} else {
						valInWords = val.substring(1, 2)+"Days";
					}
				} else {
					valInWords = val.substring(1, 3)+"Days";
				}
				break;
			case "W":
				valInWords = "Week";
				break;
			case "M":
				valInWords = "Month";
				break;
			case "Y":
				valInWords = "Year";
				break;
			default:
				valInWords = "";
				break;
			}
		}
		//return val.substring(1, val.length())+valInWords;
		return valInWords;
	}
	
	public static String getDaysCount(String val) {
		String valInWords = "";
		if(val!=null && !val.equalsIgnoreCase("0")) {
			switch(val.substring(0, 1)) {
			case "D":
				if(val.length()==2) {
					if(val.substring(1, 2).equalsIgnoreCase("1")){
						valInWords = "DAILY";
					} else {
						valInWords = val.substring(1, 2)+"DAYS";
					}
				} else {
					valInWords = val.substring(1, 3)+"DAYS";
				}
				break;
			case "W":
				valInWords = "WEEKLY";
				break;
			case "M":
				valInWords = "MONTHLY";
				break;
			case "Y":
				valInWords = "YEARLY";
				break;
			default:
				valInWords = "";
				break;
			}
		}
		return valInWords;
	}
	
	public static PurchaseRequestDetail extractBuaInfo(PurchaseRequestDetail pur) {
		try{
			String os = "null";
			String browser = "null";
			String deviceModel = "null";
			
			if(pur!=null && pur.getBua().length()>30){
				String browserDetails = pur.getBua();
				String userAgent = browserDetails;
				String user = userAgent.toLowerCase();

				//=================DEVICE OS=======================//
				if (userAgent.toLowerCase().indexOf("windows") >= 0 ) {
					os = "Windows";
				} else if(userAgent.toLowerCase().indexOf("mac") >= 0) {
					os = "Mac";
				} else if(userAgent.toLowerCase().indexOf("x11") >= 0) {
					os = "Unix";
				} else if(userAgent.toLowerCase().indexOf("android") >= 0) {
					os = "Android";
				} else if(userAgent.toLowerCase().indexOf("iphone") >= 0) {
					os = "IPhone";
				} else{
					//os = "UnKnown, More-Info: "+userAgent;
					os = "NA";
				}

				//===============DEVICE BROWSER===========================//

				if (user.contains("msie")) { //MSIE
					String substring=userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
					browser=substring.split(" ")[0].replace("MSIE", "IE")+"-"+substring.split(" ")[1];
				} 
				else if(user.contains("wap")) {
		        	browser=(userAgent.substring(userAgent.indexOf("WAP")).split(" ")[0]);
		        }
				else if(user.contains("rv")) {
					browser="IE-" + user.substring(user.indexOf("rv") + 3, user.indexOf(")"));
				} 
				else if(user.contains("ucbrowser")) { //UCBrowser
					browser=(userAgent.substring(userAgent.indexOf("UCBrowser")).split(" ")[0]).replace("/", "-");
				}
				else if(user.contains("puffin")) { //Puffin
					browser=(userAgent.substring(userAgent.indexOf("Puffin")).split(" ")[0]).replace("/", "-");
				}
				else if(user.contains("samsungbrowser")) { //SamsungBrowser
					browser=(userAgent.substring(userAgent.indexOf("SamsungBrowser")).split(" ")[0]).replace("/", "-");
				}
				else if(user.contains("yabrowser")) { //Yandex Browser
					browser=(userAgent.substring(userAgent.indexOf("YaBrowser")).split(" ")[0]).replace("/", "-");
				}
				else if(user.contains("xiaomi") || user.contains("miuibrowser")) { // XiaoMi/MiuiBrowser
					if(user.contains("xiaomi")) {
						if(userAgent.indexOf("XIAOMI") != -1){
		        			browser=(userAgent.substring(userAgent.indexOf("XIAOMI")).split(" ")[0]).replace("/", "-");
		        		} else if(userAgent.indexOf("Xiaomi") != -1){
		        			browser=(userAgent.substring(userAgent.indexOf("Xiaomi")).split(" ")[0]).replace("/", "-");
		        		} else {
		        			browser=(userAgent.substring(userAgent.indexOf("XiaoMi")).split(" ")[0]).replace("/", "-");
		        		}
					} else if(user.contains("miuibrowser")) {
						browser=(userAgent.substring(userAgent.indexOf("MiuiBrowser")).split(" ")[0]).replace("/", "-");
					}
				}    
				else if(user.contains("iemobile")) { //IEMobile
					browser=(userAgent.substring(userAgent.indexOf("IEMobile")).split(" ")[0]).replace("/", "-");
				}
				else if(user.contains("edge")) { //Edge Mobile
					browser=(userAgent.substring(userAgent.indexOf("Edge")).split(" ")[0]).replace("/", "-");
				}
				else if(user.contains("bb")) { //Blackberry Browser
					browser=(userAgent.substring(userAgent.indexOf("BB")).split(" ")[0]).replace("/", "-");
				}
				else if(user.contains("dolfin")) { //Dolfin Browser
					browser=(userAgent.substring(userAgent.indexOf("Dolfin")).split(" ")[0]).replace("/", "-");
				}
				else if (user.contains("opr") || user.contains("opera") || user.contains("opios")) {

					if(user.contains("opera mobi")) { //Opera Mobi
						browser=((userAgent.substring(userAgent.indexOf("Opera Mobi")).split(" ")[0]).replace("/", "-"));
					} else if(user.contains("opera mini")) { //Opera Mini
						browser=((userAgent.substring(userAgent.indexOf("Opera Mini")).split(" ")[0]).replace("/", "-"));
					} else if(user.contains("opios")) { //OPiOS
						browser=((userAgent.substring(userAgent.indexOf("OPiOS")).split(" ")[0]).replace("/", "-"));
					} else if(user.contains("opera")) { //Opera
						if(user.contains("Version")){
							browser=(userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0]+"-"+(userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
						} else {
							browser=(userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).replace("/", "-");
						}
					} else if(user.contains("opr")) { //OPR
						browser=((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-")).replace("OPR", "Opera");
					}
				} 
				else if (user.contains("firefox") || user.contains("fxios")) { // Firefox, FxiOS
					if(user.contains("firefox")) {
						browser=(userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
					} else if(user.contains("fxios")) {
						browser=(userAgent.substring(userAgent.indexOf("FxiOS")).split(" ")[0]).replace("/", "-");
					}
				}
				else if (user.contains("chrome")) { //Chrome
					browser=(userAgent.substring(userAgent.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
				}
				else if (user.contains("safari")) { //Safari
		        	if(user.contains("version")) {
		        		browser=(userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0]+"-"+(userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
		        	} else {
		        		browser=(userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0];
		        	}
		        }
				else if ((user.indexOf("mozilla/7.0") > -1) || (user.indexOf("netscape6") != -1)  || (user.indexOf("mozilla/4.7") != -1) || (user.indexOf("mozilla/4.78") != -1) ||
						(user.indexOf("mozilla/4.08") != -1) || (user.indexOf("mozilla/3") != -1) ){
					//browser=(userAgent.substring(userAgent.indexOf("MSIE")).split(" ")[0]).replace("/", "-");
					browser = "Netscape-?";
				} 
				else {
					//browser = "UnKnown, More-Info: "+userAgent;
					browser = "NA";
				}

				//===============Device Model=====================//

				//user = (user.indexOf("(")!=-1)?user.substring(user.indexOf('(') + 1, user.indexOf(')')):user;
				user = (user.indexOf("(")!=-1 && user.indexOf(")")!=-1)?(user.substring(user.indexOf('(') + 1, user.indexOf(')'))):user;

				if(user.contains("iphone")){
					deviceModel = "iPhone";
				} 
				else if(user.contains("windows phone") || user.contains("microsoft") || user.contains("lumia") || user.contains("nokia")) {
					deviceModel = "Nokia";
				} 
				else if(user.contains("micromax") || user.contains("canvas")){
					deviceModel = "Micromax";
				}
				else if(user.contains("samsung") || user.contains("sm-")){
					deviceModel = "Samsung";
				}
				else if(user.contains(" mi ")){
					deviceModel = "Mi";
				}
				else if(user.contains("redmi")){
					deviceModel = "Redmi";
				}
				else if(user.contains("vivo")){
					deviceModel = "Vivo";
				}
				else if(user.contains("ultrafone")) {
		        	deviceModel = "Ultrafone";
		        }
				else if(user.contains("moto") || user.contains("moto ") || user.contains("motorola") || user.contains(" xt") ){
					deviceModel = "Motorola";
				}
				else if(user.contains("intex") || user.contains("aqua")){
					deviceModel = "Intex";
				}
				else if(user.contains("lenovo")){
					deviceModel = "Lenovo";
				}
				else if(user.contains("huawei") || user.contains("lio") || user.contains("che") || user.contains("cro-")){
					deviceModel = "Huawei";
				}
				else if(user.contains("tecno")){
					deviceModel = "Tecno";
				}
				else if(user.contains("nexus")){
					deviceModel = "Google";
				}
				else if(user.contains("lephone")){
					deviceModel = "Lephone";
				}
				else if(user.contains("gucci") || user.contains("xiaomi")){
					deviceModel = "Xiaomi";
				}
				else if(user.contains("letv") || user.contains("le ")){
					deviceModel = "LeTV";
				}
				else if(user.contains("aura") || user.contains("karbonn") || user.contains("maximus") || user.contains("titanium")){
					deviceModel = "Karbonn";
				}
				else if(user.contains("neo") || user.contains("neo power") || user.contains("konnect")){
					deviceModel = "Swipe";
				}
				else if(user.contains("asus")){
					deviceModel = "Asus";
				}
				else if(user.contains("iris")){
					deviceModel = "Lava";
				}
				else if(user.contains("revolution")){
					deviceModel = "Jivi Revolution";
				}
				else if(user.contains(" lg") || user.contains(" lg-") || user.contains(" lgm-")){
					deviceModel = "LG";
				}
				else if(user.contains("centric")){
					deviceModel = "Centric";
				}
				else if(user.contains("sapphire")){
					deviceModel = "SingTech";
				}
				else if(user.contains("one")){
					deviceModel = "OnePlus";
				}
				else if(user.contains("exmart")){
					deviceModel = "Exmart";
				}
				else if(user.contains("htc desire") || user.contains("htc") || user.contains("desire")){
					deviceModel = "HTC";
				}
				else if(user.contains("oppo") || user.contains("a51f") || user.contains("f1f")){
					deviceModel = "Oppo";
				}
				else if(user.contains("panasonic")){
					deviceModel = "Panasonic";
				}
				else if(user.contains("spice")){
					deviceModel = "Spice";
				}
				else if(user.contains("sony") || user.contains("h4113")){
					deviceModel = "Sony";
				}
				else if(user.contains("itel")){
					deviceModel = "Itel";
				}
				else if(user.contains("e-tel")){
					deviceModel = "E-tel";
				}
				else if(user.contains("afmid")){
					deviceModel = "Aftron";
				}
				else if(user.contains("alba")){
					deviceModel = "Alba";
				}
				else if(user.contains("honor") || user.contains("jat-l") || user.contains("ksa-l")){
					deviceModel = "Honor";
				}
				else if(user.contains("ag-02")){
					deviceModel = "Atouch";
				}
				else if(user.contains("sth100-2")){
					deviceModel = "Blackberry";
				}
				else if(user.contains("Doro Liberto")){
					deviceModel = "Doro";
				}
				else if(user.contains("gionee")){
					deviceModel = "Gionee";
				}
				else if(user.contains("extreme")){
					deviceModel = "Extreme";
				}
				else {
					deviceModel = "NA";
				}

				//===============Final Output=====================//
				//System.out.println("BUA OS Name --> "+os+", Browser Name --> "+browser+", DeviceModel Name --> "+deviceModel);
				pur.setDevOsName(os);
				pur.setBrowserName(browser);
				pur.setDeviceModel(deviceModel);
			}
		} catch(Exception e){
			ErrorLogger.getLogger().error("Exception at extractBuaInfo : ",e);
		}
		return pur;
	}
}
