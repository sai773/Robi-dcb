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
@Table(name = "CP_Callbacks",
indexes = {@Index(name = "RequestTime",  columnList = "RequestTime"),
           @Index(name = "idx_CallbackStatus", columnList = "CallbackStatus"),
           @Index(name = "idx_RetryCount", columnList = "RetryCount")})

public class CpCallbacks implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "Id")
	private Integer id;
	
	@Column(name = "SpId")
	private String spId;
	
	@Column(name = "PurchaseID")
	private String purchaseId;
	
	@Column(name = "RequestTime")
	private Date requestTime;
	
	@Column(name = "CPDeliveryStr")
	private String cpdeliveryStr;
	
	@Column(name = "CallbackTime")
	private Date callbackTime;
	
	@Column(name = "CallbackStatus")
	private String callbackStatus;
	
	@Column(name = "RetryCount")
	private Integer retryCount;
	
	@Column(name = "Url")
	private String url; 
	
	public CpCallbacks() {
	}

	public CpCallbacks(String purchaseId) {
		this.purchaseId = purchaseId;
	}

	public CpCallbacks(String spId, String purchaseId, Date requestTime,
			String cpdeliveryStr, Date callbackTime, String callbackStatus,Integer retryCount, String url) {
		this.spId = spId;
		this.purchaseId = purchaseId;
		this.requestTime = requestTime;
		this.cpdeliveryStr = cpdeliveryStr;
		this.callbackTime = callbackTime;
		this.callbackStatus = callbackStatus;
		this.retryCount = retryCount;
		this.url = url;
	}
}
