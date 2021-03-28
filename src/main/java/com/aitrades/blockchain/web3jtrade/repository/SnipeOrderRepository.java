package com.aitrades.blockchain.web3jtrade.repository;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;
import com.mongodb.client.result.DeleteResult;

import reactor.core.publisher.Mono;

@Repository
public class SnipeOrderRepository {

	@Resource(name = "snipeOrderReactiveMongoTemplate")
	public ReactiveMongoTemplate snipeOrderReactiveMongoTemplate;
	
	public Mono<DeleteResult> delete(SnipeTransactionRequest transactionRequest) {
		return snipeOrderReactiveMongoTemplate.remove(transactionRequest);
	}

	public void save(SnipeTransactionRequest snipeTransactionRequest) {
		snipeOrderReactiveMongoTemplate.save(snipeTransactionRequest);
	}
}
