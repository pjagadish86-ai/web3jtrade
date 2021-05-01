package com.aitrades.blockchain.web3jtrade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.mongodb.reactivestreams.client.MongoClient;
@Configuration
public class EndpointMongConfig extends AbstractReactiveMongoConfiguration {
	
	private static final String ENDPOINT_CONFIG = "endpointConfig";
	
	@Autowired
	public MongoClient mongoClient;
	
	@Override
	protected String getDatabaseName() {
		return ENDPOINT_CONFIG;
	}

	@Bean(name = "endpointConfigReactiveMongoTemplate")
	public ReactiveMongoTemplate dexCntrStcCdsReactiveMongoTemplate(MongoClient mongoClient) {
		return new ReactiveMongoTemplate(mongoClient, getDatabaseName());
	}

	
}