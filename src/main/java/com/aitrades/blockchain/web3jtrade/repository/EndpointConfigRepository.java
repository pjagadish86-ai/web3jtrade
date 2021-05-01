package com.aitrades.blockchain.web3jtrade.repository;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

import com.aitrades.blockchain.web3jtrade.domain.EndpointConfig;

@Repository
public class EndpointConfigRepository {
	
	private static final String ENDPOINT_NODE_CONFIG ="endPointNodeConfig";

	@Resource(name = "endpointConfigReactiveMongoTemplate")
	private ReactiveMongoTemplate endpointConfigReactiveMongoTemplate;
	
	public List<EndpointConfig> fetchSupportedBlockchainEndpointNodeConfigUrls(){
		return endpointConfigReactiveMongoTemplate.findAll(EndpointConfig.class, ENDPOINT_NODE_CONFIG).collectList().block();
	}

	public void addSupportedBlockchainEndpointNodeConfigUrls(EndpointConfig endpointConfig) {
		endpointConfigReactiveMongoTemplate.insert(endpointConfig, ENDPOINT_NODE_CONFIG).block();
	}
}
