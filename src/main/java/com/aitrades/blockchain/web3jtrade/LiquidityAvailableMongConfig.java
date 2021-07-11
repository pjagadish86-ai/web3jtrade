package com.aitrades.blockchain.web3jtrade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.mongodb.reactivestreams.client.MongoClient;
@Configuration
public class LiquidityAvailableMongConfig extends AbstractReactiveMongoConfiguration {
	
	private static final String LIQUIDITY_AVAL_CONFIG = "liquidityAvailableConfig";
	
	@Autowired
	public MongoClient mongoClient;
	
	@Override
	protected String getDatabaseName() {
		return LIQUIDITY_AVAL_CONFIG;
	}

	@Bean(name = "liquidityAvailableConfigReactiveMongoTemplate")
	public ReactiveMongoTemplate liquidityAvailableConfigReactiveMongoTemplate(MongoClient mongoClient) {
		return new ReactiveMongoTemplate(mongoClient, getDatabaseName());
	}

	
}