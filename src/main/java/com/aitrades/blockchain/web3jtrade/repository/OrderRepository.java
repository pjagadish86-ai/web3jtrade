package com.aitrades.blockchain.web3jtrade.repository;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import com.aitrades.blockchain.web3jtrade.domain.Order;
import com.mongodb.client.result.DeleteResult;

import reactor.core.publisher.Mono;

@Repository
public class OrderRepository {

	@Resource(name = "orderHistoryReactiveMongoTemplate")
	public ReactiveMongoTemplate orderHistoryReactiveMongoTemplate;
	
	@Async
	public Mono<DeleteResult> delete(Order order) {
		return orderHistoryReactiveMongoTemplate.remove(order);
	}

	public void save(Order order) {
		orderHistoryReactiveMongoTemplate.save(order);
	}
}
