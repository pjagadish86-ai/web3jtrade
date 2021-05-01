package com.aitrades.blockchain.web3jtrade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.mongodb.reactivestreams.client.MongoClient;
@Configuration
public class DexContractStaticCodeValueMongoConfig extends AbstractReactiveMongoConfiguration {
	
	private static final String DEX_CONTRACT_STATIC_CODE_VALUES = "dexContractStaticCodeValue";
	
	@Autowired
	public MongoClient mongoClient;
	
	@Override
	protected String getDatabaseName() {
		return DEX_CONTRACT_STATIC_CODE_VALUES;
	}

	@Bean(name = "dexCntrStcCdsReactiveMongoTemplate")
	public ReactiveMongoTemplate dexCntrStcCdsReactiveMongoTemplate(MongoClient mongoClient) {
		return new ReactiveMongoTemplate(mongoClient, getDatabaseName());
	}

	
}