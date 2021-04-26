package com.juno.datapojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
	_amountTransaction_req amountTransaction;
	
	public PaymentRequest(PurchaseRequestDetail pur){
		amountTransaction = new _amountTransaction_req();
		amountTransaction.clientCorrelator = new String(pur.getPurchaseId());
		amountTransaction.endUserId = new String("tel:+" + pur.getMsisdn());
		amountTransaction.paymentAmount.chargingInformation.amount = new String(pur.getPrice());
		amountTransaction.paymentAmount.chargingInformation.currency = new String("BDT");
		amountTransaction.paymentAmount.chargingInformation.description = new String(pur.getSerName());
		amountTransaction.paymentAmount.chargingMetaData.onBehalfOf = new String(pur.getSerProvName());
		amountTransaction.referenceCode = new String("REF-12345");
		amountTransaction.transactionOperationStatus = new String("Charged");
	}
}
