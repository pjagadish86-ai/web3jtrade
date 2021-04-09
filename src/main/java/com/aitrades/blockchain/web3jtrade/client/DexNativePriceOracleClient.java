package com.aitrades.blockchain.web3jtrade.client;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple3;

import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexContract;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import  com.aitrades.blockchain.web3jtrade.domain.price.Cryptonator;
import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.ImmutableMap;

@Service
public class DexNativePriceOracleClient implements DexSubGraphPriceClient {

	private static final String BNB_USD_PRICE = "https://api.cryptonator.com/api/ticker/bnb-usd";
	
	private static final String ETH_USD_PRICE = "https://api.cryptonator.com/api/ticker/eth-usd";

	
	private static final Map<String, String> BLOCKCHAIN_NATIVE_PRICE_ORACLE = ImmutableMap.of(TradeConstants.UNISWAP, ETH_USD_PRICE, TradeConstants.SUSHI, ETH_USD_PRICE, TradeConstants.PANCAKE, BNB_USD_PRICE);

	@Resource(name="bscPriceHttpClient")
	public CloseableHttpClient closeableHttpClient;
	
	@Resource(name="cryptonatorObjectReader")
	private ObjectReader cryptonatorObjectReader;
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private Web3jServiceClientFactory web3jServiceClientFactory;

	private Tuple3<BigInteger, BigInteger, BigInteger> getReserves(String pairAddress, String route,  Credentials credentials) {
		EthereumDexContract dexContract = new EthereumDexContract(pairAddress, 
																  web3jServiceClientFactory.getWeb3jMap().get(route).getWeb3j(), 
															      credentials);
		try {
			return dexContract.getReserves()
							  .sendAsync().get();
		} catch (Exception e) {
		}
		return null;
	}
	
	@Override
	public Cryptonator nativeCoinPrice(String route) throws Exception {
		return web3jServiceClientFactory.getWeb3jMap().get(route).getRestTemplate().getForEntity(BLOCKCHAIN_NATIVE_PRICE_ORACLE.get(route), Cryptonator.class).getBody();
	}
	
	@Cacheable(cacheNames = "routePrice")
    public String getPrice(String route) throws Exception  {
		if(cacheManager.getCache("routePrices").get(route) != null) {
            return cacheManager.getCache("routePrice").get(route).get().toString();
        }
        return nativeCoinPrice(route).getTicker().getPrice();
    }
	

	@Override
	public BigDecimal tokenPrice(String pairAddress, String route, Credentials credentials) throws Exception {
		Tuple3<BigInteger, BigInteger, BigInteger> reserves  =  getReserves(pairAddress, route, credentials);
		if(reserves == null) {
			return null;
		}
		String price = nativeCoinPrice(route).getTicker().getPrice();
		if(StringUtils.isBlank(price)) {
			return null;
		}
		
		Double priceOFToken = (Double.valueOf(1)/ (reserves.component2().divide(reserves.component1())).doubleValue()) * Double.valueOf(price);
		return BigDecimal.valueOf(priceOFToken);
	}

}