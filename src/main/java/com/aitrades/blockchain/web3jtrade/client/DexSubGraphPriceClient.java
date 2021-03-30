package com.aitrades.blockchain.web3jtrade.client;

import java.math.BigDecimal;

import com.aitrades.blockchain.web3jtrade.domain.price.EthPrice;
import com.aitrades.blockchain.web3jtrade.domain.price.PairPrice;

import io.reactivex.Flowable;

public interface DexSubGraphPriceClient {
	
	public String getResourceUrl(String route);
	
	public BigDecimal getPriceOfTicker(String pairAddress) throws Exception;
	
	public BigDecimal calculateTickerPrice(PairPrice pairPrice, EthPrice ethPrice) throws Exception;
	
	public Flowable<PairPrice> getPairData(final String pairAddress) throws Exception;
	
	public Flowable<EthPrice> getEthPrice() throws Exception;
	
}
