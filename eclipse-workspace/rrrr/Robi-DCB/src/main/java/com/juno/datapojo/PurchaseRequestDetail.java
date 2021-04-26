package com.juno.datapojo;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class PurchaseRequestDetail implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String requestTime;
	private String price;
	private String serimgurl;
	private String msisdn;
	private String currency;
	private String validity;
	private String purchaseId;
	private String cptxnid;
	private String serProvName;
	private String action;
	private String serName;
	private String serDesc;
	private String spid;
	private String netType;
	private String totalAmountCharged;
	private String endTime;
	private String imgType;
	private String acpt;
	private String bua;
	private String mip;
	private String bua2;
	private String ip2;
	private String platform;
	private String scrnSize;
	private String heResp;
	private String ashieldResp;
	private String consentTime;
	private String smsShortCode;
	private String referer; //added on 07/04/2020 @Swe
	private String xReqWithRef;
	private String Iframe;
	private String playStoreApp; 
	private String devOsName;
	private String browserName;
	private String deviceModel;

}
