package com.aitrades.blockchain.web3jtrade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.mongodb.reactivestreams.client.MongoClient;

@Configuration
public class OrderMongoConfig extends AbstractReactiveMongoConfiguration {
	
	@Autowired
	public MongoClient mongoClient;
	
	@Override
	protected String getDatabaseName() {
		return "order";
	}

	@Bean(name = "orderReactiveMongoTemplate")
	public ReactiveMongoTemplate orderReactiveMongoTemplate(MongoClient mongoClient) {
		return new ReactiveMongoTemplate(mongoClient, getDatabaseName());
	}

	
}