package com.aitrades.blockchain.web3jtrade.client;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.aitrades.blockchain.web3jtrade.domain.price.EthPrice;
import com.aitrades.blockchain.web3jtrade.domain.price.PairPrice;

import io.reactivex.Flowable;

@Service
public class DexBinanceSmartChainPriceClient implements DexSubGraphPriceClient {

	@Override
	public String getResourceUrl(String route) {
		return null;
	}

	@Override
	public BigDecimal getPriceOfTicker(String pairAddress) throws Exception {
		return null;
	}

	@Override
	public BigDecimal calculateTickerPrice(PairPrice pairPrice, EthPrice ethPrice) throws Exception {
		return null;
	}

	@Override
	public Flowable<PairPrice> getPairData(String pairAddress) throws Exception {
		return null;
	}

	@Override
	public Flowable<EthPrice> getEthPrice() throws Exception {
		return null;
	}


}