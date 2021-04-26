package com.juno.logs;


import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.annotation.Autowired;

import com.juno.datapojo.PurchaseRequestDetail;
import com.juno.redisRepo.PurchaseDetailValidationRepositoryImpl;


public class CDRLogs {
	static Logger cdr = Logger.getLogger(CDRLogs.class.getName());
	private static final String CDR4JPROPERTIES = "log4j.properties";
	private static CDRLogs m_inst = null;

	private CDRLogs() {
	}

	@Autowired
	PurchaseDetailValidationRepositoryImpl purchaseDetailValidationRepositoryImpl;
	
	private String checkCDRParam(String param) {
		String retVal = "NA";
		try {
			if (param != null) {
				if (!param.equalsIgnoreCase(""))
					return param.replaceAll(",", " ");
				;
			}
			return retVal;
		} catch (Exception e) {
			Logging.getLogger().error("Exception", e);
		}
		return retVal;
	}

	public static void init() {
		PropertyConfigurator.configure(CDR4JPROPERTIES);
		LogManager.getLogger("CDRLogging").setLevel(Level.DEBUG);
		if (m_inst == null) {
			m_inst = new CDRLogs();
		}
	}

	public static CDRLogs getCDRWriter() {
		if (m_inst == null) {
			m_inst = new CDRLogs();
		}
		return m_inst;
	}

	public synchronized void dummylogCDRRotate() {
		cdr.debug("");
	}

	public synchronized void logCDR(PurchaseRequestDetail purRequest, String status) {
		try {
			String cdrStr = checkCDRParam(purRequest.getPurchaseId()) + ",";
			cdrStr += purRequest.getRequestTime() + ",";
			cdrStr += checkCDRParam(purRequest.getSpid()) + ",";
			cdrStr += checkCDRParam(purRequest.getCptxnid()) + ",";
			cdrStr += checkCDRParam(purRequest.getMsisdn()) + ",";
			cdrStr += checkCDRParam(purRequest.getSerName()) + ",";
			cdrStr += checkCDRParam(purRequest.getSerProvName()) + ",";
			cdrStr += checkCDRParam(purRequest.getCurrency()) + ",";
			cdrStr += checkCDRParam(purRequest.getMip()) +",";
			cdrStr += checkCDRParam(purRequest.getAcpt()) +",";
			cdrStr += checkCDRParam(purRequest.getBua()!=null?purRequest.getBua().replaceAll(",", "-"):"null") + ",";
			cdrStr += checkCDRParam(purRequest.getAction()) + ",";
			cdrStr += checkCDRParam(purRequest.getPrice()) + ",";
			cdrStr += checkCDRParam(purRequest.getValidity()) + ",";
			cdrStr += checkCDRParam(purRequest.getHeResp()) +",";
			cdrStr += checkCDRParam(purRequest.getImgType()) +",";
			cdrStr += checkCDRParam(purRequest.getConsentTime()) +",";
			cdrStr += checkCDRParam(purRequest.getAshieldResp()) +",";
			cdrStr += checkCDRParam(purRequest.getBua2()!=null?purRequest.getBua2().replaceAll(",", "-"):"null") +",";
			cdrStr += checkCDRParam(purRequest.getIp2()) +",";
			cdrStr += checkCDRParam(purRequest.getPlatform()) +",";
			cdrStr += checkCDRParam(purRequest.getScrnSize()) +",";
			cdrStr += checkCDRParam(purRequest.getTotalAmountCharged() == null ? "0" : purRequest.getTotalAmountCharged()) + ",";
			cdrStr += checkCDRParam("Robi") + ",";
			if(status.equalsIgnoreCase("J201")) {
				cdrStr += checkCDRParam(purRequest.getAction().equalsIgnoreCase("dct")?"stop":"charged") + ",";
			}else {
				cdrStr += checkCDRParam("failed") + ",";
			}
			cdrStr += checkCDRParam(purRequest.getNetType() == null ? "cellular" : purRequest.getNetType()) + ",";
			cdrStr += checkCDRParam(status) + ",";
			cdrStr += checkCDRParam(purRequest.getReferer()) + ","; //added on 07/04/2020 @Swe
			cdrStr += checkCDRParam(purRequest.getXReqWithRef()) + ",";
			cdrStr += checkCDRParam(purRequest.getIframe()) + ",";
			cdrStr += checkCDRParam(purRequest.getDevOsName()) + ",";
			cdrStr += checkCDRParam(purRequest.getBrowserName()) + ",";
			cdrStr += checkCDRParam(purRequest.getDeviceModel()) + ",";
			cdrStr += checkCDRParam(purRequest.getPlayStoreApp());

			String finalCDRstring = cdrStr.replaceAll("null", "NA");
			
			Logging.getLogger().info(finalCDRstring);
			cdr.debug(finalCDRstring);
		} catch (Exception e) {
			ErrorLogger.getLogger().error("Exception in logCDR : "+ e);
		}
	}

	// Get ValidityMode With ChargingMode
	public String getValidityMode(String chargingMode) {
		String duration = "";
		String validityMode = null;
		if (chargingMode.equalsIgnoreCase("DAILY")) {
			validityMode = "D1";
		} else if (chargingMode.equalsIgnoreCase("MONTHLY")) {
			validityMode = "M1";
		} else if (chargingMode.equalsIgnoreCase("60Days")) {
			validityMode = "M2";
		} else if (chargingMode.equalsIgnoreCase("7Days")) {
			validityMode = "W1";
		} else {
			duration = chargingMode.substring(0, 1);
			validityMode = "D" + duration;
		}
		return validityMode;
	}
}
