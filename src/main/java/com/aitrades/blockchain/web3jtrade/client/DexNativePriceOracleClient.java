package com.aitrades.blockchain.web3jtrade.client;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple3;

import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexContract;
import  com.aitrades.blockchain.web3jtrade.domain.price.Cryptonator;
import com.aitrades.blockchain.web3jtrade.service.DexContractStaticCodeValuesService;
import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.reactivex.schedulers.Schedulers;

@Service
public class DexNativePriceOracleClient implements DexSubGraphPriceClient {
	
	private static final String PRICE_ORACLE_URL_PREFIX = "https://api.cryptonator.com/api/ticker/";
	private static final String PRICE_ORACLE_URL_SUFFIX = "-usd";

	private static com.github.benmanes.caffeine.cache.Cache<String, Cryptonator> tokenPairPriceCache;

	@Autowired
	private Web3jServiceClientFactory web3jServiceClientFactory;
	
	@Autowired
	private DexContractStaticCodeValuesService dexContractStaticCodeValuesService;
	
	@Autowired
    private DexNativePriceOracleClient() {
        tokenPairPriceCache = Caffeine.newBuilder()
                             .expireAfterWrite(3, TimeUnit.MINUTES)
                             .build();
    }

    public Cryptonator getNtvPrice(String route){
        return tokenPairPriceCache.get(route, rout -> {
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
							  .flowable()
							  .subscribeOn(Schedulers.io())
							  .blockingSingle();
		} catch (Exception e) {
		}
		return null;
	}
	
	@Override
	public Cryptonator nativeCoinPrice(String route) throws Exception {
		return web3jServiceClientFactory.getWeb3jMap(route).getRestTemplate()
														   .getForEntity(buildPriceOracleUrl(route), Cryptonator.class)
														   .getBody();
	}
	
	
	private String buildPriceOracleUrl(String route) {
		return PRICE_ORACLE_URL_PREFIX + dexContractStaticCodeValuesService.fetchNativeCoinTicker(route)+ PRICE_ORACLE_URL_SUFFIX;
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