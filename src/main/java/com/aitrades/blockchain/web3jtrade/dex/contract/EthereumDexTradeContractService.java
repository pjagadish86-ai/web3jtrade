package com.aitrades.blockchain.web3jtrade.dex.contract;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple3;

import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.StrategyGasProvider;

@Component
@SuppressWarnings("rawtypes")
public class EthereumDexTradeContractService {

	@Autowired
	private EthereumDexContractServiceFactory factory;
	
	public List<Type> getPairAddress(String route, String tokenA, String tokenB) throws Exception{
		return factory.getInstance(route).getPair(tokenA, tokenB);
	};
	
	public Tuple3<BigInteger, BigInteger, BigInteger> getReservesOfPair(String route, String pairAddress, Credentials credentials, StrategyGasProvider customGasProvider) throws Exception{
		return factory.getInstance(route).getReserves(pairAddress, credentials, customGasProvider);
	};
	
	public String approve(String route, Credentials credentials, String contractAddress, StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum) throws Exception{
		return factory.getInstance(route).approve(credentials, contractAddress, customGasProvider, gasModeEnum);
	};
	
	public BigInteger getAmountsIn(String route, Credentials credentials, BigDecimal inputEthers, BigDecimal slipage, StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum, 
								   List<String> memoryPathAddress) throws Exception{
		return factory.getInstance(route).getAmountsIn(credentials, inputEthers, slipage, customGasProvider, gasModeEnum, memoryPathAddress);
	};
	
	public String swapETHForTokens(String route, Credentials credentials, BigInteger inputEthers,BigInteger outputTokens, StrategyGasProvider customGasProvider,  GasModeEnum gasModeEnum,  
								   long deadLine, List<String> memoryPathAddress, boolean hasFee) throws Exception{
		return factory.getInstance(route).swapETHForTokens(credentials, inputEthers, outputTokens, customGasProvider, gasModeEnum, deadLine, memoryPathAddress, hasFee);
	};
	
	public BigInteger getAmountsOut(String route, Credentials credentials,BigDecimal inputTokens,BigDecimal slipage, StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum,
									List<String> memoryPathAddress) throws Exception{
		return factory.getInstance(route).getAmountsOut(credentials, inputTokens, slipage, customGasProvider, gasModeEnum, memoryPathAddress);
	};
	
	public String swapTokenForETH(String route, Credentials credentials, BigInteger inputTokens, BigInteger outputEthers, StrategyGasProvider customGasProvider, 
							      GasModeEnum gasModeEnum, long deadLine, List<String> memoryPathAddress, boolean hasFee) throws Exception{
		return factory.getInstance(route).swapTokenForETH(credentials, inputTokens, outputEthers, customGasProvider, gasModeEnum, deadLine, memoryPathAddress, hasFee);
	};
	
}