package com.aitrades.blockchain.web3jtrade.repository;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;
import com.mongodb.client.result.DeleteResult;

@Repository
public class SnipeOrderRepository {

	private static final String LOCK = "LOCK";
	private static final String AVAL = "AVAL";
	private static final String ID = "id";
	private static final String READ = "read";
	@Resource(name = "snipeOrderReactiveMongoTemplate")
	public ReactiveMongoTemplate snipeOrderReactiveMongoTemplate;
	
	public void delete(SnipeTransactionRequest transactionRequest) {
		DeleteResult count  = snipeOrderReactiveMongoTemplate.remove(transactionRequest).block();
		if(count != null && count.getDeletedCount() == 0) {
			System.out.println("not snipe deleted");
		}else {
			System.out.println("deleted");
		}
	}

	public void save(SnipeTransactionRequest snipeTransactionRequest) {
		snipeOrderReactiveMongoTemplate.save(snipeTransactionRequest).block();
	}
	
	public void updateLock(SnipeTransactionRequest snipeTransactionRequest) {
		Query query = new Query();
        query.addCriteria(Criteria.where(ID).is(snipeTransactionRequest.getId()));
        Update update = new Update();
        update.set(READ, LOCK);
        snipeOrderReactiveMongoTemplate.updateFirst(query, update, SnipeTransactionRequest.class).block();
	}
	
	public void updateAvail(SnipeTransactionRequest snipeTransactionRequest) {
		Query query = new Query();
        query.addCriteria(Criteria.where(ID).is(snipeTransactionRequest.getId()));
        Update update = new Update();
        update.set(READ, AVAL);
        snipeOrderReactiveMongoTemplate.updateFirst(query, update, SnipeTransactionRequest.class).block();
	}
}
