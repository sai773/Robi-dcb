package com.juno.redisRepo;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import com.juno.datapojo.TokenResponse;

@Repository
public class TokenRepoImpl implements TokenRepo {
	
	private RedisTemplate<String, String> redisTemplate;
	private ValueOperations<String, String> valueOperation;
	
	public TokenRepoImpl() {
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Autowired
    public TokenRepoImpl(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
	
	@PostConstruct
    public void init() {
        valueOperation = redisTemplate.opsForValue();
    }

	@Override
	public void setAccessTokenKeyinRedis(String spid, String token, long expires_in) {
		valueOperation.set(spid, token);
		valueOperation.getOperations().expire(spid, expires_in, TimeUnit.SECONDS);
	}

	@Override
	public String getAccessTokenKeyinRedis(String spid) {
			return valueOperation.get(spid);
	}

	@Override
	public void setDuplicateCheck(String spid, String cptxnid, String msisdn) {
		valueOperation.set(cptxnid, spid + msisdn);
		valueOperation.getOperations().expire(cptxnid, 5, TimeUnit.MINUTES);
	}

	@Override
	public String getDuplicateCheck(String cptxnid) {
		return valueOperation.get(cptxnid);
	}
}
