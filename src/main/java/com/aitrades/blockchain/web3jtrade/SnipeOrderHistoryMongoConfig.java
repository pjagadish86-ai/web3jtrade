package com.aitrades.blockchain.web3jtrade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.mongodb.reactivestreams.client.MongoClient;

@Configuration
public class SnipeOrderHistoryMongoConfig extends AbstractReactiveMongoConfiguration {
	
	@Autowired
	public MongoClient mongoClient;
	
	@Override
	protected String getDatabaseName() {
		return "snipeOrder";
	}

	@Bean(name = "snipeOrderHistoryReactiveMongoTemplate")
	public ReactiveMongoTemplate snipeOrderHistoryReactiveMongoTemplate(MongoClient mongoClient) {
		return new ReactiveMongoTemplate(mongoClient, getDatabaseName());
	}

	
}