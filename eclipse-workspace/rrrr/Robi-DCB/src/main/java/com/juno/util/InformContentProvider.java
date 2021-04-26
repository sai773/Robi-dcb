package com.juno.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.MDC;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.juno.controller.PurchaseController;
import com.juno.database.model.CpDetails;
import com.juno.database.model.Purchases;
import com.juno.database.model.Subscription;
import com.juno.database.service.SubscriptionInfoServiceImpl;
import com.juno.datapojo.CallbackPayload;
import com.juno.datapojo.PurchaseRequestDetail;
import com.juno.logs.CDRLogs;
import com.juno.logs.ErrorLogger;
import com.juno.logs.Logging;
import com.juno.redisRepo.PurchaseDetailValidationRepositoryImpl;


@Component
public class InformContentProvider {

	@Autowired
	SubscriptionInfoServiceImpl impl;

	@Autowired
	CommonHelper commonHelper;

	public static int httpTimeout;

	@Value("${httpTimeout}")
	public void sethttpTimeout(int timeout) {
		httpTimeout = timeout;
	}
	
	@Autowired
	PurchaseDetailValidationRepositoryImpl purchaseDetailValidationRepositoryImpl;

	private String cpUrl = null;
	private String urlParameters = null;
	private String cpDeliveryStr = null;
	int statusCode = 0;
	int count = 0;
	private String m_msisdn = null;
	String cpResp = null;
	PurchaseRequestDetail purReq = null;
	Subscription sub = null;
	String urlstatus = null;
	String responseBody = null;
	int code = 0;
	String body = null;

	public InformContentProvider() {
		MDC.get("Robi-UNIQUE-ID");
	}

	public String informTOCP(HttpServletRequest request,
			HttpServletResponse response, PurchaseRequestDetail purReq, Subscription sub, String status, String type) {
		cpResp = redirectTOCP(request, response, purReq, status, type);
		Logging.getLogger().info("Updated Content provider and Logging to CDR");
		CDRLogs.getCDRWriter().logCDR(purReq, status);
		return cpResp;
	}

	public String redirectTOCP(HttpServletRequest request,HttpServletResponse response, PurchaseRequestDetail purReq, 
			String status, String type) {
		MDC.put("Robi-UNIQUE-ID", purReq.getPurchaseId());
		String stat = "FAILURE";
		String opresp = null;
		String action = null;
		String chMode = null;
		int respCode;

		switch (purReq.getAction().toLowerCase()) {
		case "act":
		case "act_park":
			action = "Act"; break;
		case "rnw":
		case "act_grace":
			action = "Rnw"; break;
		case "dct":
			action = "Dct"; break;
		case "pd":
			action = "PD"; break;
		default:
			action = purReq.getAction(); break;
		}

		switch(status){
		case "J201":
			opresp = "Success"; 
			chMode = purReq.getValidity(); break;
		case "J202":
			opresp = "Low_Bal"; 
			chMode = (purReq.getAction().equalsIgnoreCase("act") || purReq.getAction().equalsIgnoreCase("act_park"))?"Parking":
				((purReq.getAction().equalsIgnoreCase("rnw")|| purReq.getAction().equalsIgnoreCase("act_grace"))?"Grace":"null");
			break;
		default:
			opresp = "Failure"; 
			chMode = purReq.getValidity(); break;
		}
		
		/*Remove/Delete Purchase records from Redis*/
		PurchaseRequestDetail pur = null;
		pur = purReq==null?purchaseDetailValidationRepositoryImpl.getPurchaseRequestDetail(purReq.getPurchaseId()):purReq;
		if(pur!=null && (pur.getNetType()!=null && !pur.getNetType().equalsIgnoreCase("ccgui"))){
			purchaseDetailValidationRepositoryImpl.deletePurchaseRequestDetail(purReq.getPurchaseId());
		}
		
		//REDIRECT TO MERCHANT
		if(type.equalsIgnoreCase("redirect")) {
			Logging.getLogger().info("Requested Id " + purReq.getPurchaseId() + " Redirect To CP");
			try {
				m_msisdn = purReq.getMsisdn()!=null?purReq.getMsisdn():"null";
				CpDetails resp = purReq.getSpid()!=null?impl.getAllurl(purReq.getSpid()):null;
				if (purReq.getValidity().equalsIgnoreCase("PD") && resp != null) {
					// fetch pd redirect url
					cpUrl = resp.getPdRedirectUrl();
				} else if (!purReq.getValidity().equalsIgnoreCase("PD")	&& resp != null) {
					// fetch sub redirect url
					cpUrl = resp.getRedirectUrl();
				}

				/*Logging.getLogger().info("cp_txid =" + purReq.getCptxnid() 
								+ ",amount="+ purReq.getPrice() + " ,CPURL= " + cpUrl);*/
				if (cpUrl != null) {
					urlParameters = "sn=" + URLEncoder.encode(purReq.getSerName() == null ? "null": purReq.getSerName(), "UTF-8")
					+ "&pr=" + URLEncoder.encode(purReq.getTotalAmountCharged() != null ? purReq.getTotalAmountCharged() : "0","UTF-8")
					+ "&m=" + URLEncoder.encode(m_msisdn == null ? "null": m_msisdn, "UTF-8")
					+ "&action=" + URLEncoder.encode(action == null ? "null": action, "UTF-8")
					+ "&cptxnid=" + URLEncoder.encode(purReq.getCptxnid() == null ? "null" : purReq.getCptxnid(),"UTF-8")
					+ "&charging_mode=" + URLEncoder.encode(chMode == null ? "null": chMode, "UTF-8")
					+ "&optxnid=" + URLEncoder.encode(purReq.getPurchaseId() == null ? "null" : purReq.getPurchaseId(), "UTF-8")
					+ "&status=" + URLEncoder.encode(status == null ? "null" : status,"UTF-8")
					+ "&opstatus=" + URLEncoder.encode(opresp == null ? "null" : opresp,"UTF-8");

					cpDeliveryStr = cpUrl + "?" + urlParameters;
					Logging.getLogger().info("Redirecting to CP on URL : " + cpDeliveryStr);
					response.setHeader("Access-Control-Allow-Origin",request.getHeader("Origin"));
					response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
					response.setHeader("Location", cpDeliveryStr);
					response.sendRedirect(cpDeliveryStr);
				} else {
					Logging.getLogger().info("Failed to send CP Redirect/Callback URL for Purchase Id "+ purReq.getPurchaseId());
				}
				stat = "SUCCESS";
			} catch (Exception e) {
				ErrorLogger.getLogger().error("InformContentprovider in redirectTOCP method  with purchase id="+(purReq.getPurchaseId() == null ? "null": purReq.getPurchaseId()), e);
			}
		}
		//CALLBACK TO MERCHANT
		else if(type.equalsIgnoreCase("serverHit")) {
			Logging.getLogger().info("Requested Id " + purReq.getPurchaseId() + " Server-To-Server Callback");
			try {
				m_msisdn=purReq.getMsisdn();
				CpDetails resp = impl.getAllurl(purReq.getSpid());
				cpUrl = resp.getCallbackUrl();

				if (cpUrl != null) {
					CallbackPayload payload=new CallbackPayload();
					payload.setAction(action);
					payload.setChargingMode(chMode);
					payload.setMsisdn(m_msisdn);
					payload.setStatus(opresp);
					payload.setOptxnid(purReq.getPurchaseId());
					payload.setPrice(purReq.getTotalAmountCharged());
					payload.setServiceName(purReq.getSerName());
					payload.setCptxnid(purReq.getCptxnid());
					JSONObject json = new JSONObject(new Gson().toJson(payload));
					Logging.getLogger().info("Server Callback to CP on URL : " + cpUrl+", params : "+ json);
					respCode = commonHelper.Post2Json(cpUrl, json);
					if(respCode == 200) {
						stat = "SUCCESS";
					}else {
						stat = "FAILURE";
						impl.insertCpdeliveryString(purReq, cpUrl , json.toString(), "P");
					}
				}else {
					Logging.getLogger().info("Failed to send CP Redirect/Callback URL for Purchase Id "+ purReq.getPurchaseId());
				}
			} catch (Exception e) {
				ErrorLogger.getLogger().error("InformContentprovider in redirectTOCP method  with purchase id="+(purReq.getPurchaseId() == null ? "null": purReq.getPurchaseId()), e);
			}
		}
		return stat;
	}

	public void CpInfoCallback(String url, String urlparams, PurchaseRequestDetail purReq)
			throws IOException {
		try {
			String infoCallbackURL = url + "?" + urlparams;
			Logging.getLogger().info("Info Callback Url..." + infoCallbackURL);
			org.apache.commons.httpclient.HttpClient m_httpClient = HTTPManager.getConnection();
			GetMethod m_Method = new GetMethod(infoCallbackURL);
			code = ((org.apache.commons.httpclient.HttpClient) m_httpClient).executeMethod(m_Method);
			body = m_Method.getResponseBodyAsString();
			Logging.getLogger().info("code is :" + code + "body is :" + body);

		} catch (Exception e) {
			ErrorLogger.getLogger().error("InformContentprovider in CpInfoCallback method  with purchase id="+(purReq.getPurchaseId() == null ? "null": purReq.getPurchaseId()), e);
		}
	}
}
