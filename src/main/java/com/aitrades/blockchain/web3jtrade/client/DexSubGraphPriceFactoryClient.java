package com.aitrades.blockchain.web3jtrade.client;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;

@Service
public class DexSubGraphPriceFactoryClient {
	
	@Autowired
	private DexNativePriceOracleClient priceOracleClient;
	
	@Autowired
	private Map<String, DexSubGraphPriceClient> typesMap;

	@PostConstruct
	public void init() {
		typesMap.put(TradeConstants.UNISWAP, priceOracleClient);
		typesMap.put(TradeConstants.SUSHI, priceOracleClient);
		typesMap.put(TradeConstants.PANCAKE, priceOracleClient);
	}
	
	public DexSubGraphPriceClient getRoute(String condition) {
		return typesMap.get(condition);
	}

}