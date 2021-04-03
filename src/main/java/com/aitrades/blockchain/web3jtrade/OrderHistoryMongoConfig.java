package com.aitrades.blockchain.web3jtrade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.mongodb.reactivestreams.client.MongoClient;

@Configuration
public class OrderHistoryMongoConfig extends AbstractReactiveMongoConfiguration {
	
	private static final String ORDER_HISTORY = "orderHistory";
	@Autowired
	public MongoClient mongoClient;
	
	@Override
	protected String getDatabaseName() {
		return ORDER_HISTORY;
	}

	@Bean(name = "orderHistoryReactiveMongoTemplate")
	public ReactiveMongoTemplate orderHistoryReactiveMongoTemplate(MongoClient mongoClient) {
		return new ReactiveMongoTemplate(mongoClient, ORDER_HISTORY);
	}

	
}