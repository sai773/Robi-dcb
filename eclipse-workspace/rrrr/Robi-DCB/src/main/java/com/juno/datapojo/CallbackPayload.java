package com.juno.datapojo;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallbackPayload implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String ServiceName;
	private String Price;
	private String Msisdn;
	private String Cptxnid;
	private String Action;
	private String ChargingMode;
	private String Optxnid;
	private String Status;
	private String OpStatus;	
}
