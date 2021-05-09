package com.aitrades.blockchain.web3jtrade.dex.contract;

import java.math.BigInteger;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;

@Component
public class DexTradeContractServiceV3 {

	@Autowired
	private DexContractServiceFactory factory;
	
	public BigInteger getAmountsIn(String route, Map<String, Object> requestMap, Credentials credentials) throws Exception{
		return factory.getV3Instance(route).getAmountsIn(route, requestMap, credentials);
	}

	public String fetchSignedTransaction(String route, Map<String, Object> requestMap, Credentials credentials) throws Exception{
		return factory.getV3Instance(route).fetchSignedTransaction(route, requestMap, credentials);
	}
	
	public BigInteger getAmountsOut(String route, Map<String, Object> requestMap, Credentials credentials) throws Throwable{
		return factory.getV3Instance(route).getAmountsOut(route, requestMap, credentials);
	}
	
	public String swapTokensForTokens(String route, Map<String, Object> requestMap, Credentials credentials) throws Exception{
		return factory.getV3Instance(route).swapTokensForTokens(route, requestMap, credentials);
	}
	
}