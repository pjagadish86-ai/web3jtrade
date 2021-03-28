package com.aitrades.blockchain.web3jtrade.repository;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

import com.aitrades.blockchain.web3jtrade.domain.Order;
import com.mongodb.client.result.DeleteResult;

@Repository
public class OrderRepository {

	@Resource(name = "orderReactiveMongoTemplate")
	public ReactiveMongoTemplate orderReactiveMongoTemplate;
	
	public void delete(Order order) {
		DeleteResult count  = orderReactiveMongoTemplate.remove(order).block();
		if(count.getDeletedCount() == 0) {
			System.out.println("not deleted");
		}else {
			System.out.println("deleted");
		}
	}

	public void save(Order order) {
		orderReactiveMongoTemplate.save(order);
	}
}
