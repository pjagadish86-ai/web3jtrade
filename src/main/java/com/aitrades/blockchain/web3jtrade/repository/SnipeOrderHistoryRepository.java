package com.aitrades.blockchain.web3jtrade.repository;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;
import com.mongodb.client.result.DeleteResult;

import reactor.core.publisher.Mono;

@Repository
public class SnipeOrderHistoryRepository {

	@Resource(name = "snipeOrderHistoryReactiveMongoTemplate")
	public ReactiveMongoTemplate snipeOrderHistoryReactiveMongoTemplate;

	public Mono<DeleteResult> delete(SnipeTransactionRequest transactionRequest) {
		return snipeOrderHistoryReactiveMongoTemplate.remove(transactionRequest);
	}

	public void insert(SnipeTransactionRequest snipeTransactionRequest) {
		snipeOrderHistoryReactiveMongoTemplate.insert(snipeTransactionRequest);
	}
}
