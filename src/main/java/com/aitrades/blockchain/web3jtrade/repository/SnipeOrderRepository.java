package com.aitrades.blockchain.web3jtrade.repository;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
	
	public void updateLock(SnipeTransactionRequest snipeTransactionRequest) {
		Query query = new Query();
        query.addCriteria(Criteria.where("id").is(snipeTransactionRequest.getId()));
        Update update = new Update();
        update.set("read", "LOCK");
        snipeOrderReactiveMongoTemplate.updateFirst(query, update, SnipeTransactionRequest.class).block();
	}
	
	public void updateAvail(SnipeTransactionRequest snipeTransactionRequest) {
		Query query = new Query();
        query.addCriteria(Criteria.where("id").is(snipeTransactionRequest.getId()));
        Update update = new Update();
        update.set("read", "AVAL");
     //   update.set("counter", order.getCounter()+1); TODO: Come back
        snipeOrderReactiveMongoTemplate.updateFirst(query, update, SnipeTransactionRequest.class).block();
	}
}
