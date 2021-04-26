package com.juno.datapojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponse {
	public _amountTransaction_resp amountTransaction;
	public _requestError requestError;
	public _fault faultError;
	
	public PaymentResponse(){
		amountTransaction = new _amountTransaction_resp();
		requestError = new _requestError();
		faultError = new _fault();
	}
	
	public _fault getFaultError() {
		return faultError;
	}

	public void setFaultError(_fault faultError) {
		this.faultError = faultError;
	}

	public String getClientCorrelator() {
		return amountTransaction.clientCorrelator;
	}
	public void setClientCorrelator(String clientCorrelator) {
		amountTransaction.clientCorrelator = clientCorrelator;
	}
	public String getEndUserId() {
		return amountTransaction.endUserId;
	}
	public void setEndUserId(String endUserId) {
		amountTransaction.endUserId = endUserId;
	}
	public _paymentAmount_resp getPaymentAmount() {
		return amountTransaction.paymentAmount;
	}
	public void setPaymentAmount(_paymentAmount_resp paymentAmount) {
		amountTransaction.paymentAmount = paymentAmount;
	}
	public String getReferenceCode() {
		return amountTransaction.referenceCode;
	}
	public void setReferenceCode(String referenceCode) {
		amountTransaction.referenceCode = referenceCode;
	}
	public String getTransactionOperationStatus() {
		return amountTransaction.transactionOperationStatus;
	}
	public void setTransactionOperationStatus(String transactionOperationStatus) {
		amountTransaction.transactionOperationStatus = transactionOperationStatus;
	}
	public String getServerReferenceCode() {
		return amountTransaction.serverReferenceCode;
	}
	public void setServerReferenceCode(String serverReferenceCode) {
		amountTransaction.serverReferenceCode = serverReferenceCode;
	}
	public String getResourceURL() {
		return amountTransaction.resourceURL;
	}
	public void setResourceURL(String resourceURL) {
		amountTransaction.resourceURL = resourceURL;
	}	
	public String getTotalAmountCharged() {
		return amountTransaction.paymentAmount.totalAmountCharged;
	}
	public void setTotalAmountCharged(String totalAmountCharged) {
		amountTransaction.paymentAmount.totalAmountCharged = totalAmountCharged;
	}
	public String getAmount() {
		return amountTransaction.paymentAmount.chargingInformation.amount;
	}
	public void setAmount(String amount) {
		amountTransaction.paymentAmount.chargingInformation.amount = amount;
	}
	public String getCurrency() {
		return amountTransaction.paymentAmount.chargingInformation.currency;
	}
	public void setCurrency(String currency) {
		amountTransaction.paymentAmount.chargingInformation.currency = currency;
	}
	public String getDescription() {
		return amountTransaction.paymentAmount.chargingInformation.description;
	}
	public void setDescription(String description) {
		amountTransaction.paymentAmount.chargingInformation.description = description;
	}
	public String getOnBehalfOf() {
		return amountTransaction.paymentAmount.chargingMetaData.onBehalfOf;
	}
	public void setOnBehalfOf(String onBehalfOf) {
		amountTransaction.paymentAmount.chargingMetaData.onBehalfOf = onBehalfOf;
	}
	/*	public String getPurchaseCategoryCode() {
		return amountTransaction.paymentAmount.chargingMetaData.purchaseCategoryCode;
	}
	public void setPurchaseCategoryCode(String purchaseCategoryCode) {
		amountTransaction.paymentAmount.chargingMetaData.purchaseCategoryCode = purchaseCategoryCode;
	}
	public String getChannel() {
		return amountTransaction.paymentAmount.chargingMetaData.channel;
	}
	public void setChannel(String channel) {
		amountTransaction.paymentAmount.chargingMetaData.channel = channel;
	}
	public String getTaxAmount() {
		return amountTransaction.paymentAmount.chargingMetaData.taxAmount;
	}
	public void setTaxAmount(String taxAmount) {
		amountTransaction.paymentAmount.chargingMetaData.taxAmount = taxAmount;
	}
	public String getServiceID() {
		return amountTransaction.paymentAmount.chargingMetaData.serviceID;
	}
	public void setServiceID(String serviceID) {
		amountTransaction.paymentAmount.chargingMetaData.serviceID = serviceID;
	}*/

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
	public String getPolText() {
		return requestError.policyException.text;
	}
	public void setPolText(String text) {
		requestError.policyException.text = text;
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
	public void setMessage(String msg) {
		faultError.message = msg;
	}
	public String getMessage() {
		return faultError.message;
	}
	public void setDescriptions(String description) {
		faultError.description = description;
	}
	public String getDescriptions() {
		return faultError.description;
	}
}
