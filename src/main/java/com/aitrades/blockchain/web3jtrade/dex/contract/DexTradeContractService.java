package com.aitrades.blockchain.web3jtrade.dex.contract;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple3;

@Component
@SuppressWarnings("rawtypes")
public class DexTradeContractService {

	@Autowired
	private DexContractServiceFactory factory;
	
	public List<Type> getPairAddress(String route, String tokenA, String tokenB) throws Exception{
		return factory.getInstance(route).getPair(tokenA, tokenB);
	};
	
	public Tuple3<BigInteger, BigInteger, BigInteger> getReservesOfPair(String route, String pairAddress, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) throws Exception{
		return factory.getInstance(route).getReserves(pairAddress, credentials, gasPrice, gasLimit);
	};
	
	public BigInteger getAmountsIn(String route, Credentials credentials, BigInteger inputEthers, Double slipage,
								   List<String> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit) throws Exception{
		return factory.getInstance(route).getAmountsIn(credentials, inputEthers, slipage, memoryPathAddress, gasPrice, gasLimit);
	};
	
	public String swapETHForTokens(String route, Credentials credentials, BigInteger inputEthers,BigInteger outputTokens,
								   long deadLine, List<String> memoryPathAddress, boolean hasFee, BigInteger gasPrice, BigInteger gasLimit) throws Exception{
		return factory.getInstance(route).swapETHForTokens(credentials, inputEthers, outputTokens, deadLine, memoryPathAddress, hasFee, gasPrice, gasLimit);
	};
	
	public BigInteger getAmountsOut(String route, Credentials credentials,BigInteger inputTokens,Double slipage, 
									List<String> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit) throws Throwable{
		return factory.getInstance(route).getAmountsOut(credentials, inputTokens, slipage, memoryPathAddress, gasPrice, gasLimit);
	};
	
	public String swapTokenForETH(String route, Credentials credentials, BigInteger inputTokens, BigInteger outputEthers, long deadLine, List<String> memoryPathAddress, boolean hasFee, BigInteger gasPrice, BigInteger gasLimit) throws Exception{
		return factory.getInstance(route).swapTokenForETH(credentials, inputTokens, outputEthers, deadLine, memoryPathAddress, hasFee, gasPrice, gasLimit);
	};
	
}