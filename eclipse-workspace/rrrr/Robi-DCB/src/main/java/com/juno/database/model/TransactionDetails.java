//package com.juno.database.model;
//
//import java.util.Date;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.SecondaryTable;
//import javax.persistence.Table;
//
//import lombok.Data;
//
//@Data
//@Entity
//@Table(name = "Purchases")
//@SecondaryTable(name = "Subscription")
//public class TransactionDetails implements java.io.Serializable {
//	
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//
//	@Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//	@Column(name = "id")
//	private Integer id;
//
//   	@Column(name = "PurchaseID")
//	private String purchaseId;
//	
//	@Column(name = "RequestTime")
//	private Date requestTime;
//	
//	@Column(name = "MSISDN")
//	private String msisdn;
//	
//	@Column(name = "Amount")
//	private String amount;
//	
//	@Column(name = "currency")
//	private String cur;
//	
//	@Column(name = "Charged")
//	private String charged;
//	
//	@Column(name = "ChargeTime")
//	private Date chargeTime;
//	
//	@Column(name = "CGTxnid")
//	private String cgTxnid;
//	
//	@Column(name = "CGstatusCode")
//	private String cgstatusCode;
//	
//	@Column(name = "Validity")
//	private String validity;
//	
//	@Column(name = "ServiceName")
//	private String serviceName;
//	
//	@Column(name = "Action")
//	private String action;
//	
//	@Column(name = "CPTxId")
//	private String cpTxId;
//	
//	@Column(name = "ServiceProvName")
//	private String serProvName;
//	
//	@Column(name = "TotalAmountCharged")
//	private String totalAmountCharged;
//	
//	@Column(name = "ServiceDesc")
//	private String serviceDesc;
//	
//	@Column(name = "CPCallbackStatus")
//	private String cpcallbackStatus;
//	
//	@Column(name = "Netype")
//	private String netype;
//	
//	@Column(name = "Operator")
//	private String operator;
//
//	@Column(name = "JunoStatus")
//	private String junoStatus;
//	
//	@Column(name = "PPurchaseId")
//	private String ppurchaseId;
//	
//	@Column(name = "CDRStatus")
//	private String cdrStatus;
//	
//	@Column(name = "StartTime", table = "Subscription")
//	private Date startTime;
//	
//	@Column(name = "EndTime", table = "Subscription")
//	private Date endTime;
//	
//	@Column(name = "RenewStatus", table = "Subscription")
//	private Integer renewStatus;
//	
//	@Column(name = "RetryCount", table = "Subscription")
//	private Integer retryCount;
//	
//	@Column(name = "RetryTime", table = "Subscription")
//	private Date retryTime;
//	
//	@Column(name = "RetryDays", table = "Subscription")
//	private Integer retryDays;
//	
//	@Column(name = "subPremResType", table = "Subscription")
//	private String subPremResType;
//
//	public TransactionDetails() {
//
//	}
//
//	public TransactionDetails(Integer id, String purchaseId, Date requestTime, String msisdn, String amount, String cur,
//			String charged, Date chargeTime, String cgTxnid, String cgstatusCode, String validity, String serviceName,
//			String action, String cpTxId, String serProvName, String totalAmountCharged, String serviceDesc,
//			String cpcallbackStatus, String netype, String operator, String junoStatus, String ppurchaseId,
//			Date startTime, Date endTime, Integer renewStatus, Integer retryCount, Date retryTime, Integer retryDays,
//			String subPremResType) {
//		super();
//		this.id = id;
//		this.purchaseId = purchaseId;
//		this.requestTime = requestTime;
//		this.msisdn = msisdn;
//		this.amount = amount;
//		this.cur = cur;
//		this.charged = charged;
//		this.chargeTime = chargeTime;
//		this.cgTxnid = cgTxnid;
//		this.cgstatusCode = cgstatusCode;
//		this.validity = validity;
//		this.serviceName = serviceName;
//		this.action = action;
//		this.cpTxId = cpTxId;
//		this.serProvName = serProvName;
//		this.totalAmountCharged = totalAmountCharged;
//		this.serviceDesc = serviceDesc;
//		this.cpcallbackStatus = cpcallbackStatus;
//		this.netype = netype;
//		this.operator = operator;
//		this.junoStatus = junoStatus;
//		this.ppurchaseId = ppurchaseId;
//		this.startTime = startTime;
//		this.endTime = endTime;
//		this.renewStatus = renewStatus;
//		this.retryCount = retryCount;
//		this.retryTime = retryTime;
//		this.retryDays = retryDays;
//		this.subPremResType = subPremResType;
//	}
//
//	
//}
