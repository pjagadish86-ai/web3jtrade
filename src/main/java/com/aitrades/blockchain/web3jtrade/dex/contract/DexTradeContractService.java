package com.aitrades.blockchain.web3jtrade.dex.contract;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;

@Component
@SuppressWarnings("rawtypes")
public class DexTradeContractService {

	@Autowired
	private DexContractServiceFactory factory;
	
	public List<Type> getPairAddress(String route, String tokenA, String tokenB) throws Exception{
		return factory.getInstance(route).getPair(route,tokenA, tokenB);
	};
	
	public Tuple3<BigInteger, BigInteger, BigInteger> getReservesOfPair(String route, String pairAddress, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception{
		return factory.getInstance(route).getReserves(route,pairAddress, credentials, gasPrice, gasLimit, gasMode);
	};
	
	public BigInteger getAmountsIn(String route, Credentials credentials, BigInteger inputEthers, Double slipage,
								   List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode,  String decimals) throws Exception{
		return factory.getInstance(route).getAmountsIn(route, credentials, inputEthers, slipage, memoryPathAddress, gasPrice, gasLimit, gasMode, decimals);
	};
	
	public String swapETHForTokens(String route, Credentials credentials, BigInteger inputEthers,BigInteger outputTokens,
								   long deadLine, List<Address> memoryPathAddress, boolean hasFee, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception{
		return factory.getInstance(route).swapExactTokensForTokens(route,credentials, inputEthers, outputTokens, deadLine, memoryPathAddress, hasFee, gasPrice, gasLimit, gasMode);
	};
	
	public BigInteger getAmountsOut(String route, Credentials credentials,BigInteger inputTokens,Double slipage, 
									List<String> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Throwable{
		return factory.getInstance(route).getAmountsOut(route, credentials, inputTokens, slipage, memoryPathAddress, gasPrice, gasLimit, gasMode);
	};
	
	public String swapTokenForETH(String route, Credentials credentials, BigInteger inputTokens, BigInteger outputEthers, long deadLine, List<String> memoryPathAddress, boolean hasFee, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception{
		return factory.getInstance(route).swapTokenForETH(route, credentials, inputTokens, outputEthers, deadLine, memoryPathAddress, hasFee, gasPrice, gasLimit, gasMode);
	};
	
	public TransactionReceipt deposit(String route, BigInteger weiValue, Credentials credentials, BigInteger inputEthers, Double slipage,
			   List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit , String gasMode) throws Exception {
		return factory.getInstance(route).deposit(route, weiValue, credentials, inputEthers, slipage, memoryPathAddress, gasPrice, gasLimit, gasMode);
	};
	
	public TransactionReceipt withDraw(String route, BigInteger weiValue, Credentials credentials, BigInteger inputEthers, Double slipage,
			   List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit , String gasMode) {
		return factory.getInstance(route).withDraw(route, inputEthers, credentials, inputEthers, slipage, memoryPathAddress, gasPrice, gasLimit, null);
	};
	
	public TransactionReceipt transfer(String route, String pairAddress, BigInteger wad, Credentials credentials, BigInteger inputEthers, Double slipage,
			   List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit , String gasMode) throws Exception {
		return factory.getInstance(route).transfer(route, pairAddress, inputEthers, credentials, inputEthers, slipage, memoryPathAddress, gasPrice, gasLimit, gasMode);
	};
	
}