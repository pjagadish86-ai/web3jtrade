package com.aitrades.blockchain.web3jtrade.client;

import java.math.BigDecimal;

import com.aitrades.blockchain.web3jtrade.domain.price.EthPrice;
import com.aitrades.blockchain.web3jtrade.domain.price.PairPrice;

public interface DexSubGraphPriceClient {
	
	public String getResourceUrl(String route);
	
	public BigDecimal getPriceOfTicker(String pairAddress) throws Exception;
	
	public BigDecimal calculateTickerPrice(PairPrice pairPrice, EthPrice ethPrice) throws Exception;
	
}
