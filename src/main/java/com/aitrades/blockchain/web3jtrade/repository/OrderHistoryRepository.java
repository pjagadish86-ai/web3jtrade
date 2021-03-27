package com.aitrades.blockchain.web3jtrade.repository;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

import com.aitrades.blockchain.web3jtrade.domain.Order;
import com.mongodb.client.result.DeleteResult;

import reactor.core.publisher.Mono;

@Repository
public class OrderHistoryRepository {

	@Resource(name = "orderReactiveMongoTemplate")
	public ReactiveMongoTemplate orderReactiveMongoTemplate;

	public Mono<DeleteResult> delete(Order order) {
		return orderReactiveMongoTemplate.remove(order);
	}

	public void save(Order order) {
		orderReactiveMongoTemplate.save(order);
	}
}
