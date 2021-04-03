package com.aitrades.blockchain.web3jtrade.client;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DexSubGraphPriceFactoryClient {

	private static final String SUSHI = "SUSHI";

	private static final String UNISWAP = "UNISWAP";

	@Autowired
	private DexSubGraphPriceServiceClient dexSubGraphPriceServiceClient;
	
	@Autowired
	private Map<String, DexSubGraphPriceServiceClient> typesMap;

	@PostConstruct
	public void init() {
		typesMap.put(UNISWAP, dexSubGraphPriceServiceClient);
		typesMap.put(SUSHI, dexSubGraphPriceServiceClient);
	}
	
	public DexSubGraphPriceServiceClient getRoute(String condition) {
		return typesMap.get(condition);
	}

}