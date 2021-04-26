package com.juno.datapojo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.juno.database.model.ProductInfo;
import com.juno.database.service.SubscriptionInfoServiceImpl;
import com.juno.logs.ErrorLogger;
import com.juno.logs.Logging;
import com.juno.util.CommonHelper;



@Service
public class SmsMessage {

	@Autowired
	SubscriptionInfoServiceImpl subInfoServiceImpl;

	private String content=null;

	public String smsformat(String val, String desc, String amount, String endTime, String smsType, String spid) {
		try{
			if(smsType != null) {
				ProductInfo pinfo = subInfoServiceImpl.findByValidity(val, spid);
				String valInWords = !val.equalsIgnoreCase("PD")?CommonHelper.getValidityInWords(pinfo.getValidity()):"";
				
				if(smsType.equalsIgnoreCase("act") && !val.equalsIgnoreCase("PD")){
					content = "You have subscribed for "+ desc + " at BDT " + pinfo.getAmount() + "(+VAT+SD+SC)/"
							+ valInWords +". Service is auto-renewable. Data charge applicable. Next renewal is on "+ 
							endTime.substring(0, 10) + ". To unsubscribe, click " + pinfo.getUnSubUrl().trim() + 
							"  or type STOP and send SMS to " + pinfo.getSmsShortCode() +".";
				}else if(smsType.equalsIgnoreCase("rnw") && !val.equalsIgnoreCase("PD")) {
					content = "You subscription to "+ desc + " has been renewed at BDT " + pinfo.getAmount() + "(+VAT+SD+SC)/"
							+ valInWords +". Service is auto-renewable. Data charge applicable. Next renewal is on "+ 
							endTime.substring(0, 10) + ". To unsubscribe, click " + pinfo.getUnSubUrl().trim() 
							+ "  or type STOP and send SMS to " + pinfo.getSmsShortCode() +".";
				}else if(smsType.equalsIgnoreCase("rem") && !val.equalsIgnoreCase("PD")) {
					/*content = "You are currently subscribed for " + desc +". To unsubscribe, click "+ pinfo.getUnSubUrl() 
					+ " or type STOP and send SMS to " + pinfo.getSmsShortCode()+ ".";*/ //commented on 17/07/2019
					/*content = "Your pack "+ desc + " is due for renewal on "+endTime.substring(0, 10)+"."
							+ "It will be renewed for "+valInWords+" at BDT "+ pinfo.getAmount() +
							"(+VAT+SC+SD). To unsubscribe, click "+pinfo.getUnSubUrl()+
							"  or type STOP and send SMS to "+pinfo.getSmsShortCode()+".";*/ //commented on 19/07/2019
					
					content = "Your subscription to " + desc + " has been renewed at BDT "+pinfo.getAmount()+"(+VAT+SD+SC)/"+valInWords+"."
							+ " Service is auto-renewable. Data charge applicable. Next renewal is on "+endTime.substring(0, 10)+"."
							+ " To unsubscribe, click "+pinfo.getUnSubUrl().trim()+"  or type STOP and send SMS to "+pinfo.getSmsShortCode()+".";
				}else if(smsType.equalsIgnoreCase("dct") && !val.equalsIgnoreCase("PD")) {
					content = "You have successfully unsubscribed from " + desc +". Thank you.";
				}else if(smsType.equalsIgnoreCase("PD") && val.equalsIgnoreCase("PD")){
					content = "You have purchased " + desc + " at BDT " + amount + "(+VAT+SD+SC). Enjoy it now!";
				}else if(smsType.equalsIgnoreCase("act_low_bal") && !val.equalsIgnoreCase("PD")){
					content = "You have subscribed for "+ desc + " at BDT " + pinfo.getAmount() +"(+VAT+SD+SC)/"+ valInWords +". "
							+ "Recharge your number to continue using the service. "
							+ "Data charges applicable. To unsubscribe, click " + pinfo.getUnSubUrl().trim() + 
							"  or type STOP and send SMS to " + pinfo.getSmsShortCode() +".";
				}
			}else {
				Logging.getLogger().info("No records for Transaction Details!!!!");
			}
		}catch(Exception e){
			ErrorLogger.getLogger().error("Exception in smsformat : "+e.getMessage());
		}
		return content;
	}
}
