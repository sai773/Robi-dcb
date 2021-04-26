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
@Table(name = "LoginDetails")
public class LoginDetails implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id")
	private Integer id;

	@Column(name = "SpId")
	private String spId;

	@Column(name = "Username")
	private String lusr;

	@Column(name = "Password")
	private String lpwd;

	public LoginDetails() {
	}

	public LoginDetails(String spId, String user, String pwd) {
		this.spId = spId;
		this.lusr = user;
		this.lpwd = pwd;
	}
}
