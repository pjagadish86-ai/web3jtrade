package com.aitrades.blockchain.web3jtrade.repository;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

import com.aitrades.blockchain.web3jtrade.domain.EndpointConfig;

@Repository
public class LiquidityAvailableRepo {

	private static final String LIQUIDITY_AVAL_CONFIG = "liquidityAvailableConfig";
	
	
	@Resource(name = "liquidityAvailableConfigReactiveMongoTemplate")
	private ReactiveMongoTemplate liquidityAvailableConfigReactiveMongoTemplate;
	
	public List<EndpointConfig> fetchLiquidityAvailableRepo(){
		return liquidityAvailableConfigReactiveMongoTemplate.findAll(EndpointConfig.class, LIQUIDITY_AVAL_CONFIG).collectList().block();
	}

	
}
