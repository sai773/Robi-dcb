package com.juno.database.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "CP_Details")
public class CpDetails implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "Id")
	private Integer id;
	
	@Column(name = "SpId")
	private String spId;
	
	@Column(name = "CallbackURL")
	private String callbackUrl;
	
	@Column(name = "RedirectURL")
	private String redirectUrl;
	
	@Column(name = "PDRedirectURL")
	private String pdRedirectUrl;
	
	@Column(name = "InfoCallbackURL")
	private String infoCallbackUrl;

	public CpDetails() {
	}

	public CpDetails(String spId, String callbackUrl, String redirectUrl, String pdRedirectUrl, String infoCallbackUrl) {
		this.spId = spId;
		this.callbackUrl = callbackUrl;
		this.redirectUrl = redirectUrl;
		this.pdRedirectUrl = pdRedirectUrl;
		this.infoCallbackUrl = infoCallbackUrl;
	}
}
