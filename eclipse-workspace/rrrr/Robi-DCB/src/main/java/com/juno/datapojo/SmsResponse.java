package com.juno.datapojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SmsResponse {
	_outboundSMSMessageResponse outboundSMSMessageResponse;
	public _requestError requestError;
	public _fault faultError;
	
	public SmsResponse(){
		outboundSMSMessageResponse = new _outboundSMSMessageResponse();
		requestError = new _requestError();
		faultError = new _fault();
	}
	
	public _requestError getRequestError() {
		return requestError;
	}

	public void setRequestError(_requestError requestError) {
		this.requestError = requestError;
	}

	public _fault getFaultError() {
		return faultError;
	}

	public void setFaultError(_fault faultError) {
		this.faultError = faultError;
	}

	public Object getAddress(){
		return outboundSMSMessageResponse.address;
	}
	
	public void setAddress(String[] Address) {
		outboundSMSMessageResponse.address = Address;
	}
	
	public String getResourceURL() {
		return outboundSMSMessageResponse.deliveryInfoList.resourceURL;
	}
	public void setResourceURL(String resourceURL) {
		outboundSMSMessageResponse.deliveryInfoList.resourceURL = resourceURL;
	}
	public String getDeliveryInfoAddress() {
		return outboundSMSMessageResponse.deliveryInfoList.deliveryInfo[0].address;
	}
	public void setDeliveryInfoAddress(String address) {
		outboundSMSMessageResponse.deliveryInfoList.deliveryInfo[0].address = address;
	}
	public String getDeliveryStatus() {
		return outboundSMSMessageResponse.deliveryInfoList.deliveryInfo[0].deliveryStatus;
	}
	public void setDeliveryStatus(String deliveryStatus) {
		outboundSMSMessageResponse.deliveryInfoList.deliveryInfo[0].deliveryStatus = deliveryStatus;
	}
	public String getSenderAddress() {
		return outboundSMSMessageResponse.senderAddress;
	}
	public void setSenderAddress(String senderAddress) {
		outboundSMSMessageResponse.senderAddress = senderAddress;
	}
	public String getClientCorrelator() {
		return outboundSMSMessageResponse.clientCorrelator;
	}
	public void setClientCorrelator(String clientCorrelator) {
		outboundSMSMessageResponse.clientCorrelator = clientCorrelator;
	}
	public String getMessage() {
		return outboundSMSMessageResponse.outboundSMSTextMessage.message;
	}
	public void setMessage(String message) {
		outboundSMSMessageResponse.outboundSMSTextMessage.message = message;
	}
	public String getSenderName() {
		return outboundSMSMessageResponse.senderName;
	}
	public void setSenderName(String senderName) {
		outboundSMSMessageResponse.senderName = senderName;
	}
	public String getResourceUrl() {
		return outboundSMSMessageResponse.resourceURL;
	}
	public void setResourceUrl(String resourceUrl) {
		outboundSMSMessageResponse.resourceURL = resourceUrl;
	}
	public String getPolMessageId() {
		return requestError.policyException.messageId;
	}
	public void setPolMessageId(String messageId) {
		requestError.policyException.messageId = messageId;
	}
	public String getPolVariables() {
		return requestError.policyException.variables;
	}
	public void setPolVariables(String variables) {
		requestError.policyException.variables = variables;
	}
	public String getSerMessageId() {
		return requestError.serviceException.messageId;
	}
	public void setSerMessageId(String messageId) {
		requestError.serviceException.messageId = messageId;
	}
	public String getSerText() {
		return requestError.serviceException.text;
	}
	public void setSerText(String text) {
		requestError.serviceException.text = text;
	}
	public String getSerVariables() {
		return requestError.serviceException.variables;
	}
	public void setSerVariables(String variables) {
		requestError.serviceException.variables = variables;
	}
	public void setCode(String code) {
		faultError.code = code;
	}
	public String getCode() {
		return faultError.code;
	}
	public void setErMessage(String msg) {
		faultError.message = msg;
	}
	public String getErMessage() {
		return faultError.message;
	}
	public void setDescriptions(String description) {
		faultError.description = description;
	}
	public String getDescriptions() {
		return faultError.description;
	}
}
