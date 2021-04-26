package com.juno.redisRepo;

public interface TokenRepo {
	String getAccessTokenKeyinRedis(String spid);
	void setAccessTokenKeyinRedis(String spid, String token, long expires);
	void setDuplicateCheck(String spid, String cptxnid, String msisdn);
	String getDuplicateCheck(String cptxnid);
}
