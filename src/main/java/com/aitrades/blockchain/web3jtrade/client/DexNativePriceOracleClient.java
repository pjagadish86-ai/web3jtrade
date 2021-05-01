package com.aitrades.blockchain.web3jtrade.client;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple3;

import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexContract;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import  com.aitrades.blockchain.web3jtrade.domain.price.Cryptonator;
import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableMap;

@Service
public class DexNativePriceOracleClient implements DexSubGraphPriceClient {
	
	private static com.github.benmanes.caffeine.cache.Cache<String, Cryptonator> tokenCache;
	
	private static final String BNB_USD_PRICE = "https://api.cryptonator.com/api/ticker/bnb-usd";
	
	private static final String ETH_USD_PRICE = "https://api.cryptonator.com/api/ticker/eth-usd";

	
	private static final Map<String, String> BLOCKCHAIN_NATIVE_PRICE_ORACLE = ImmutableMap.of(TradeConstants.UNISWAP, ETH_USD_PRICE, TradeConstants.SUSHI, ETH_USD_PRICE, TradeConstants.PANCAKE, BNB_USD_PRICE);

	@Resource(name="bscPriceHttpClient")
	public CloseableHttpClient closeableHttpClient;
	
	@Resource(name="cryptonatorObjectReader")
	private ObjectReader cryptonatorObjectReader;
	
	@Autowired
	private Web3jServiceClientFactory web3jServiceClientFactory;
	
	@Autowired
    private DexNativePriceOracleClient() {
        tokenCache = Caffeine.newBuilder()
                             .expireAfterWrite(3, TimeUnit.MINUTES)
                             .build();
    }

    public Cryptonator getNtvPrice(String route){
        return tokenCache.get(route, rout -> {
			try {
				return this.nativeCoinPrice(rout);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
    }

	private Tuple3<BigInteger, BigInteger, BigInteger> getReserves(String pairAddress, String route,  Credentials credentials) throws Exception {
		EthereumDexContract dexContract = new EthereumDexContract(pairAddress, 
																  web3jServiceClientFactory.getWeb3jMap(route).getWeb3j(), 
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
		return web3jServiceClientFactory.getWeb3jMap(route).getRestTemplate().getForEntity(BLOCKCHAIN_NATIVE_PRICE_ORACLE.get(route), Cryptonator.class).getBody();
	}
	
    public String getPrice(String route) throws Exception  {
        return getNtvPrice(route).getTicker().getPrice();
    }

    public BigDecimal getPriceBigDec(String route) throws Exception  {
        return new BigDecimal(getNtvPrice(route).getTicker().getPrice()).setScale(2, RoundingMode.DOWN);
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
		
		BigInteger divide = reserves.component1().divide(reserves.component2());
		Double priceOFToken = (Double.valueOf(1)/ divide.doubleValue()) * Double.valueOf(price);
		
		try {
			return BigDecimal.valueOf(priceOFToken);
		} catch (Exception e) {
			BigInteger divide1 = reserves.component2().divide(reserves.component1());
			Double priceOFToken1 = (Double.valueOf(1)/ divide1.doubleValue()) * Double.valueOf(price);
			return BigDecimal.valueOf(priceOFToken1);
		}
	}

}