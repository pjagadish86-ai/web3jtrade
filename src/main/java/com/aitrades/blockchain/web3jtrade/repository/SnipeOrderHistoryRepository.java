package com.aitrades.blockchain.web3jtrade.repository;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;

import reactor.core.publisher.Mono;

@Repository
public class SnipeOrderHistoryRepository {

	@Resource(name = "snipeOrderHistoryReactiveMongoTemplate")
	public ReactiveMongoTemplate snipeOrderHistoryReactiveMongoTemplate;

	public Mono<SnipeTransactionRequest> insert(SnipeTransactionRequest snipeTransactionRequest) throws Exception {
		return snipeOrderHistoryReactiveMongoTemplate.insert(snipeTransactionRequest);
	}
}
