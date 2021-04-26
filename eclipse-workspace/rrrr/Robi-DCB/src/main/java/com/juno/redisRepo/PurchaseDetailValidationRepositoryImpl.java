package com.juno.redisRepo;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import com.juno.datapojo.PurchaseRequestDetail;

@Repository
public class PurchaseDetailValidationRepositoryImpl implements PurchaseDetailValidationRepository {
	
	private RedisTemplate<String, PurchaseRequestDetail> redisTemplate;
	private ValueOperations<String, PurchaseRequestDetail> valueOperation;
	
	private RedisTemplate<String, Integer> sessionTemplate;
	private ValueOperations<String, Integer> valueIntOperation;
	
	public PurchaseDetailValidationRepositoryImpl() {
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Autowired
    public PurchaseDetailValidationRepositoryImpl(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.sessionTemplate = redisTemplate;
    }
	
	@PostConstruct
    public void init() {
        valueOperation = redisTemplate.opsForValue();
        valueIntOperation = sessionTemplate.opsForValue();
    }

	@Override
	public void setPurchaseRequestDetail(String purchaseId,
		PurchaseRequestDetail purchaseRequestDetail) {
		valueOperation.set("R"+purchaseId, purchaseRequestDetail);
		valueOperation.getOperations().expire("R"+purchaseId, 1, TimeUnit.HOURS);	
	}

	@Override
	public PurchaseRequestDetail getPurchaseRequestDetail(String purchaseId) {
		return valueOperation.get("R"+purchaseId);
	}

	@Override
	public void deletePurchaseRequestDetail(String purchaseId) {
		valueOperation.getOperations().delete("R"+purchaseId);
	}
	
	//Added for Session Timeout Timer Changes
	@Override
	public void setCGSessionTimer(Integer timer){
		valueIntOperation.set("rCgTimer", timer);
	}
	
	@Override
	public String getCGSessionTimer(){
		return String.valueOf(valueIntOperation.get("rCgTimer"));
	}
	
	@Override
	public void deleteCGSessionTimer(){
		valueIntOperation.getOperations().delete("rCgTimer");
	}
}
