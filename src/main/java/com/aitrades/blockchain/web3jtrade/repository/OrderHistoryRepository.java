package com.aitrades.blockchain.web3jtrade.repository;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

import com.aitrades.blockchain.web3jtrade.domain.Order;

@Repository
public class OrderHistoryRepository {

	@Resource(name = "orderHistoryReactiveMongoTemplate")
	public ReactiveMongoTemplate orderHistoryReactiveMongoTemplate;

	public Order insert(Order order) throws Exception {
		return orderHistoryReactiveMongoTemplate.insert(order).block();
	}
}
