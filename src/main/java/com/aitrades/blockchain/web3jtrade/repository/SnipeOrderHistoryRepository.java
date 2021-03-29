package com.aitrades.blockchain.web3jtrade.repository;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;

@Repository
public class SnipeOrderHistoryRepository {

	@Resource(name = "snipeOrderHistoryReactiveMongoTemplate")
	public ReactiveMongoTemplate snipeOrderHistoryReactiveMongoTemplate;

	public void save(SnipeTransactionRequest snipeTransactionRequest) throws Exception {
		 snipeOrderHistoryReactiveMongoTemplate.save(snipeTransactionRequest).block();
	}
}
