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
@Table(name = "AppRefererDetails")
public class AppRefererDetails implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "SlNo")
	private Integer slNo;
	
	@Column(name = "Package")
	private String appPackage;
	
	@Column(name = "Status")
	private String appStatus;
	
	public AppRefererDetails() {
	}
	
	public AppRefererDetails(Integer slNo, String appPack, String appStat) {
		super();
		this.slNo = slNo;
		this.appPackage = appPack;
		this.appStatus = appStat;
	}
	
	/*public AppRefererDetails(String appPack, String appStat){
		this.appPackage = appPack;
		this.appStatus = appStat;
	}*/
}
