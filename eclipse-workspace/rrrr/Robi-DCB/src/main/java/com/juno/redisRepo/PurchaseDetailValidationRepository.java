package com.juno.redisRepo;

import java.util.List;

import com.juno.datapojo.PurchaseRequestDetail;

public interface PurchaseDetailValidationRepository {
	void setPurchaseRequestDetail(String purchaseId, PurchaseRequestDetail purchaseRequestDetail);
	PurchaseRequestDetail getPurchaseRequestDetail(String purchaseId);
	void deletePurchaseRequestDetail(String purchaseId);
	void setCGSessionTimer(Integer timer);
	String getCGSessionTimer();
	void deleteCGSessionTimer();
}
