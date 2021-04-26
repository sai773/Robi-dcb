package com.juno.datapojo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Service
public class SmsRequest {
	
	public SmsRequest() {
	}

	/*private static String shortCode;
	
		@Value("${shortcode}")    
	    public void setShortCode(String code) {
		 SmsRequest.shortCode = code;
	    }*/
	    
	_outboundSMSMessageRequest outboundSMSMessageRequest = null;

		public SmsRequest(String msisdn, String msg, String purId, String shortCode){
			outboundSMSMessageRequest = new _outboundSMSMessageRequest();
			String[] a = {"tel:" + msisdn};
			outboundSMSMessageRequest.address =  a;
			outboundSMSMessageRequest.senderAddress = new String("tel:"+ shortCode);
			outboundSMSMessageRequest.outboundSMSTextMessage.message = new String(msg);
			outboundSMSMessageRequest.clientCorrelator = new String(purId);
			outboundSMSMessageRequest.senderName = new String("");
		}
}
		
	
