package com.juno.datapojo;



public class ChargingConstructs {
}

//Payment Request API
class _amountTransaction_req{
	String clientCorrelator = "";
	String endUserId = "";
	_paymentAmount_req paymentAmount = new _paymentAmount_req();
	String referenceCode = "";
	String transactionOperationStatus = "";
	public _amountTransaction_req(){ 
		clientCorrelator = new String("54321");
		endUserId = new String("tel:+");
		paymentAmount = new _paymentAmount_req();
		referenceCode = new String("REF-12345");
		transactionOperationStatus = new String("Charged");
	}
}

class _paymentAmount_req{
	_chargingInformation_req chargingInformation = new _chargingInformation_req();
	_chargingMetaData chargingMetaData = new _chargingMetaData();
	public _paymentAmount_req(){
		chargingInformation = new _chargingInformation_req();
		chargingMetaData = new _chargingMetaData();
	}
}

class _chargingInformation_req{
	String amount = "";
	String currency = "";
	String description = "";
	public _chargingInformation_req(){
		amount = new String("");
		currency = new String("BDT");
		description = new String("");
	}
}

class _chargingMetaData{
	String onBehalfOf = "";
	public _chargingMetaData(){
		onBehalfOf = new String("JunoTele Test");
	}
	public _chargingMetaData(String resp){
		onBehalfOf = new String("JunoTele Test");
	}
}

//Payment response API
class _amountTransaction_resp{
	_paymentAmount_resp paymentAmount = new _paymentAmount_resp();
	String clientCorrelator = "";
	String endUserId = "";
	String referenceCode = "";
	String transactionOperationStatus = "";
	String serverReferenceCode = "";
	String resourceURL = "";
	public _amountTransaction_resp(){
		paymentAmount = new _paymentAmount_resp();
		clientCorrelator = new String("");
		endUserId = new String("tel:+");
		referenceCode = new String("");
		transactionOperationStatus = new String("null");
		serverReferenceCode = new String("");
		resourceURL = new String("");
	}
}

class _paymentAmount_resp{
	String totalAmountCharged = "";
	_chargingInformation_resp chargingInformation = new _chargingInformation_resp();
	_chargingMetaData chargingMetaData = new _chargingMetaData();
	public _paymentAmount_resp(){
		totalAmountCharged = new String("0");
		chargingInformation = new _chargingInformation_resp();
		chargingMetaData = new _chargingMetaData("resp");
	}
}

class _chargingInformation_resp{
	String amount = "";
	String currency = "";
	String description = "";
	public _chargingInformation_resp(){
		amount = new String("0");
		currency = new String("BDT");
		description = new String("");
	}
}

//Request error response
class _requestError{
	_serviceException serviceException = new _serviceException();
	_policyException policyException = new _policyException();
	public _requestError() {
		serviceException = new _serviceException();
		policyException = new _policyException();
	}
}

//Service exception
class _serviceException{
	String messageId = "";
	String text = "";
	String variables = "";
	public _serviceException() {
		messageId = new String("null");
		text = new String("");
		variables = new String("");
	}
}

//Policy Exception
class _policyException{
	String messageId = "";
	String text = "";
	String variables = "";
	public _policyException() {
		messageId = new String("null");
		text = new String("");
		variables = new String("");
	}
}

//Fault response API
class _fault{
	String code = "";
	String message = "";
	String description = "";
	public _fault(){
		code = new String("900800");
		message = new String("Message Throttled Out");
		description = new String("You have exceeded your quota");
	}
}

//SMS api request
class _outboundSMSMessageRequest{
	String[] address = new String[1];
	String senderAddress = "";
	String clientCorrelator = "";
	_outboundSMSTextMessage outboundSMSTextMessage = new _outboundSMSTextMessage();
	String senderName = "";
	public _outboundSMSMessageRequest(){
		address[0] = new String("tel:");
		senderAddress = new String("tel:2128300");
		clientCorrelator = new String("123456");
		outboundSMSTextMessage = new _outboundSMSTextMessage();
		senderName = new String("JunoTele");
	}
}

//SMS api response
class _outboundSMSMessageResponse{
	String[] address = new String[1];
	String senderAddress = "";
	String clientCorrelator = "";
	String resourceURL = "";
	_outboundSMSTextMessage outboundSMSTextMessage = new _outboundSMSTextMessage();
	_receiptRequest receiptRequest = new _receiptRequest(); 
	String senderName = "";
	_deliveryInfoList deliveryInfoList = new _deliveryInfoList();
	public _outboundSMSMessageResponse(){
			address[0] = new String("tel:+");
			senderAddress = new String("tel:2128300");
			resourceURL = new String("");
			clientCorrelator = new String("123456");
			outboundSMSTextMessage = new _outboundSMSTextMessage();
			senderName = new String("JunoTele");
			deliveryInfoList = new _deliveryInfoList();
	}
}

class _outboundSMSTextMessage{
	String message = "";
	public _outboundSMSTextMessage(){
		message = new String("Juno Test messsage");
	}
}

class _receiptRequest{
	String notifyURL = "";
	String callbackData = "";
	public _receiptRequest(){
		notifyURL = new String("");
		callbackData = new String("Test SMS from JunoTele");
	}
}

class _deliveryInfoList{
	_deliveryInfo[] deliveryInfo = new _deliveryInfo[1]; 
	String resourceURL = "";
	public _deliveryInfoList(){
		deliveryInfo[0] = new _deliveryInfo();
		resourceURL = new String("");
	}
}

class _deliveryInfo{
	String address = "";
	String deliveryStatus = "";
	String messageReferenceCode = "";
	String operatorCode = "";
	String filterCriteria = ""; 
	public _deliveryInfo(){
		address = new String("tel:+");
		deliveryStatus = new String("MessageWaiting");
		messageReferenceCode = new String("");
		operatorCode = new String("");
		filterCriteria = new String("");
	}
}

//Receive SMS
class _inboundSMSMessageNotification{
	String callbackData = "";
	_inboundSMSMessage inboundSMSMessage = new _inboundSMSMessage();
	public _inboundSMSMessageNotification(){
		callbackData = new String("callbackdata");
		inboundSMSMessage = new _inboundSMSMessage();
	}
}

class _inboundSMSMessage{
	String dateTime = "";
	String destinationAddress = "";
	String messageId = "";
	String message = "";
	String senderAddress = "";	
	public _inboundSMSMessage(){
		dateTime = new String("2016-06-23 10:00:00");
		destinationAddress = new String("tel:+");
		messageId = new String("");
		message = new String("Test sms from Junotele");
		senderAddress = new String("tel:+");
	}
}

//Sent SMS Notification
class _deliveryInfoNotification{
	String callbackData = "";
	String serverReferenceCode = "";
	String messageReferenceCode = "";
	String senderAddress = "";
	_deliveryInfo deliveryInfo = new _deliveryInfo();
	public _deliveryInfoNotification(){
		callbackData = new String("");
		serverReferenceCode = new String("");
		messageReferenceCode =  new String("");
		senderAddress = new String("");
		deliveryInfo = new _deliveryInfo();
	}
}
