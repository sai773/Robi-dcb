package com.juno.database.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.juno.database.model.ProductInfo;

@Repository
public interface SubscriptionInfoRepo extends CrudRepository<ProductInfo, String>{
	
}
