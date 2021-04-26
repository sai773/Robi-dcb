package com.juno.database.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "Purchases",
indexes = {@Index(name = "MSISDN_IDX",  columnList = "MSISDN"),
        @Index(name = "CP_TxId_IDX", columnList = "CPTxId"),
        @Index(name = "RequestTime", columnList = "RequestTime"),
        @Index(name = "idx_JunoStatus", columnList = "JunoStatus")})
public class Purchases implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "SpId")
	private String spId;
	
   	@Column(name = "PurchaseID")
	private String purchaseId;
	
	@Column(name = "RequestTime")
	private Date requestTime;
	
	@Column(name = "MSISDN")
	private String msisdn;
	
	@Column(name = "Amount")
	private String amount;
	
	@Column(name = "cur")
	private String cur;
	
	@Column(name = "Charged")
	private String charged;
	
	@Column(name = "ChargeTime")
	private Date chargeTime;
	
	@Column(name = "CGTxnid")
	private String cgTxnid;
	
	@Column(name = "CGstatusCode")
	private String cgstatusCode;
	
	@Column(name = "Validity")
	private String validity;
	
	@Column(name = "Action")
	private String action;
	
	@Column(name = "ServiceName")
	private String serviceName;
	
	@Column(name = "CPTxId")
	private String cpTxId;
	
	@Column(name = "ServiceProvName")
	private String serviceProvName;
	
	@Column(name = "TotalAmountCharged")
	private String totalAmountCharged;
	
	@Column(name = "CGStatusDesc")
	private String cgstatusDesc;
	
	@Column(name = "CPCallbackStatus")
	private String cpcallbackStatus;
	
	@Column(name = "Netype")
	private String netype;
	
	@Column(name = "Operator")
	private String operator;

	@Column(name = "JunoStatus")
	private String junoStatus;
	
	@Column(name = "CallbackStatus")
	private String callbackStatus;
	
	@Column(name = "CallbackTime")
	private String callbackTime;
	
	@Column(name = "ServiceId")
	private String serviceId;
	
	@Column(name = "PPurchaseId")
	private String ppurchaseId;
	
	@Column(name = "Browser")
	private String browser;
	
	@Column(name = "SourceApp")
	private String sourceApp;
	
	@Column(name = "AdSource")
	private String adsource;
	
	public Purchases() {
	}

	public Purchases(String purchaseId) {
		this.purchaseId = purchaseId;
	}

	public Purchases(Integer id, String spId, String purchaseId, Date requestTime, String msisdn, String amount,
			String cur, String charged, Date chargeTime, String cgTxnid, String cgstatusCode, String validity,
			String action, String serviceName, String cpTxId, String serviceProvName, String totalAmountCharged,
			String cgstatusDesc, String cpcallbackStatus, String netype, String operator, String junoStatus,
			String callbackStatus, String callbackTime, String serviceId, String ppurchaseId,
			String bua, String xreqWith, String referer) {
		super();
		this.id = id;
		this.spId = spId;
		this.purchaseId = purchaseId;
		this.requestTime = requestTime;
		this.msisdn = msisdn;
		this.amount = amount;
		this.cur = cur;
		this.charged = charged;
		this.chargeTime = chargeTime;
		this.cgTxnid = cgTxnid;
		this.cgstatusCode = cgstatusCode;
		this.validity = validity;
		this.action = action;
		this.serviceName = serviceName;
		this.cpTxId = cpTxId;
		this.serviceProvName = serviceProvName;
		this.totalAmountCharged = totalAmountCharged;
		this.cgstatusDesc = cgstatusDesc;
		this.cpcallbackStatus = cpcallbackStatus;
		this.netype = netype;
		this.operator = operator;
		this.junoStatus = junoStatus;
		this.callbackStatus = callbackStatus;
		this.callbackTime = callbackTime;
		this.serviceId = serviceId;
		this.ppurchaseId = ppurchaseId;
		this.sourceApp = xreqWith;
		this.browser = bua;
		this.adsource = referer;
	}
}
