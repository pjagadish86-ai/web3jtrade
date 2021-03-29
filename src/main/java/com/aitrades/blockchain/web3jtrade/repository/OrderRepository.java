package com.aitrades.blockchain.web3jtrade.repository;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
	
	public void updateLock(Order order) {
		Query query = new Query();
        query.addCriteria(Criteria.where("id").is(order.getId()));
        Update update = new Update();
        update.set("read", "LOCK");
		orderReactiveMongoTemplate.updateFirst(query, update, Order.class).block();
	}
	
	public void updateAvail(Order order) {
		Query query = new Query();
        query.addCriteria(Criteria.where("id").is(order.getId()));
        Update update = new Update();
        update.set("read", "AVAL");
     //   update.set("counter", order.getCounter()+1); TODO: Come back
		orderReactiveMongoTemplate.updateFirst(query, update, Order.class).block();
	}
}
