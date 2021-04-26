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
@Table(name = "CP_Credentials")
public class CpCredentials implements java.io.Serializable {
		
		private static final long serialVersionUID = 1L;
		
		@Id
	    @GeneratedValue(strategy = GenerationType.AUTO)
		@Column(name = "Id")
		private Integer id;
		
		@Column(name = "SpId")
		private String spId;
		
		@Column(name = "UserName")
		private String usrName;
		
		@Column(name = "Password")
		private String pwd;
		
		@Column(name = "ConsumerKey")
		private String consumerKey;
		
		@Column(name = "ConsumerSecret")
		private String consumerSecret;
		
		@Column(name = "ASsecretKey")
		private String asSecretKey;

		public CpCredentials() {
		}

		public CpCredentials(String spId, String user, String passwd, String key, String secret, String asKey) {
			this.spId = spId;
			this.usrName = user;
			this.pwd = passwd;
			this.consumerKey = key;
			this.consumerSecret = secret;
			this.asSecretKey = asKey;
		}
}
