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
@Table(name = "Subscription",
indexes = {@Index(name = "SubscriptionIndex",  columnList = "StartTime, EndTime, Msisdn")})
public class Subscription implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "Id")
	private Integer id;
	
	@Column(name = "SpId")
	private String spId;
	
	@Column(name = "PurchaseID")
	private String purchaseId;
	
	@Column(name = "Msisdn")
	private String msisdn;
	
	@Column(name = "Price")
	private String amount;
	
	@Column(name = "CPTxId")
	private String cpTxId;
	
	@Column(name = "Validity")
	private String validity;
	
	@Column(name = "Action")
	private String action;
	
	@Column(name = "Status")
	private String junoStatus;
	
	@Column(name = "StartTime")
	private Date startTime;
	
	@Column(name = "EndTime")
	private Date endTime;
	
	@Column(name = "RenewStatus")
	private Integer renewStatus;
	
	@Column(name = "ServiceProvName")
	private String serProvName;
	
	@Column(name = "ServiceName")
	private String serviceName;
	
	@Column(name = "RetryCount")
	private Integer retryCount;
	
	@Column(name = "RetryTime")
	private Date retryTime;
	
	@Column(name = "ChargeStatus")
	private String charged;
	
	@Column(name = "RetryDays")
	private Integer retryDays;
	
	@Column(name = "SmsCount")
	private String smsCount;
	
	@Column(name = "prodDescription")
	private String cgstatusDesc;
	
	@Column(name = "LoginId")
	private String dctLoginId;

	public Subscription() {
	}

	public Subscription(String purchaseId, String msisdn) {
		this.purchaseId = purchaseId;
		this.msisdn = msisdn;
	}

	public Subscription(Integer id, String spId, String purchaseId, String msisdn, String amount, String cpTxId,
			String validity, String action, String junoStatus, Date startTime, Date endTime, Integer renewStatus,
			String serProvName, String serviceName, Integer retryCount, Date retryTime, String charged,
			Integer retryDays, String smsCount, String cgstatusDesc, String dctloginID) {
		super();
		this.id = id;
		this.spId = spId;
		this.purchaseId = purchaseId;
		this.msisdn = msisdn;
		this.amount = amount;
		this.cpTxId = cpTxId;
		this.validity = validity;
		this.action = action;
		this.junoStatus = junoStatus;
		this.startTime = startTime;
		this.endTime = endTime;
		this.renewStatus = renewStatus;
		this.serProvName = serProvName;
		this.serviceName = serviceName;
		this.retryCount = retryCount;
		this.retryTime = retryTime;
		this.charged = charged;
		this.retryDays = retryDays;
		this.smsCount = smsCount;
		this.cgstatusDesc = cgstatusDesc;
		this.dctLoginId = dctloginID;
	}	
}
