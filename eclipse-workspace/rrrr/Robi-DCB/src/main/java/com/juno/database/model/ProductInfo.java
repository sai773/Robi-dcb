package com.juno.database.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ProductInfo")
public class ProductInfo implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SlNo")
	private Integer slNo;
	
	@Column(name = "ServiceName")
	private String serviceName;
	
	@Column(name = "SpId")
	private String spId;
	
	@Column(name = "ServiceProvName")
	private String serviceProvName;
	
	@Column(name = "Amount")
	private String amount;
	
	@Column(name = "Validity")
	private String validity;
	
	@Column(name = "unsubUrl")
	private String unSubUrl;
	
	@Column(name = "SmsShortCode")
	private String smsShortCode;
	
	@Column(name = "PreSmsNotify")
	private Integer preSmsNotify;
	
	@Column(name = "RnwSms")
	private Integer rnwSms;
	
	public ProductInfo() {
	}

	public ProductInfo(String amount) {
		this.amount = amount;
	}

	public ProductInfo(Integer slNo, String serviceName, String spId, String serviceProvName, String amount,
			String validity, String unSubUrl, String smsShrtCode, Integer preSms, Integer rnwsms) {
		super();
		this.slNo = slNo;
		this.serviceName = serviceName;
		this.spId = spId;
		this.serviceProvName = serviceProvName;
		this.amount = amount;
		this.validity = validity;
		this.unSubUrl = unSubUrl;
		this.smsShortCode = smsShrtCode;
		this.preSmsNotify = preSms;
		this.rnwSms = rnwsms;
	}
}
