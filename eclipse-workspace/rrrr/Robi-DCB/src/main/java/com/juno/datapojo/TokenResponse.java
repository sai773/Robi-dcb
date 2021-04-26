package com.juno.datapojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponse {
	String scope;
	String token_type;
	long expires_in;
	String refresh_token;
	String access_token;
	
	public TokenResponse(String tscope, String type, long expire, String rtoken, String atoken){
		scope = tscope;
		token_type = type;
		expires_in = expire;
		refresh_token = rtoken;
		access_token = atoken;
	}
}