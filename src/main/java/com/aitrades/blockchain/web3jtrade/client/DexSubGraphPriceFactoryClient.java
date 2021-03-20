package com.aitrades.blockchain.web3jtrade.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DexSubGraphPriceFactoryClient {

	@Autowired
	private Map<String, DexSubGraphPriceServiceClient> typesMap;

	public DexSubGraphPriceServiceClient getRoute(String condition) {
		return typesMap.get(condition);
	}

}