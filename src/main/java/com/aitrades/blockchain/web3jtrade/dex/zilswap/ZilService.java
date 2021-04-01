package com.aitrades.blockchain.web3jtrade.dex.zilswap;

import java.math.BigInteger;
import java.util.List;

import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple3;

import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.oracle.gas.StrategyGasProvider;

public interface ZilService {

	public List<Type> getPair(String tokenA, String tokenB) throws Exception;
	
	public Tuple3<BigInteger, BigInteger, BigInteger> getReserves(String pairAddress, Credentials credentials, StrategyGasProvider customGasProvider) throws Exception;
	
	public BigInteger getAmountsIn(Credentials credentials, BigInteger inputEthers, Double slipage, StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum, List<String> memoryPathAddress) throws Exception;
	
	public String swapZILForTokens(Credentials credentials, BigInteger inputEthers, BigInteger outPutTokens, StrategyGasProvider customGasProvider,  GasModeEnum gasModeEnum,  long deadLine, List<String> memoryPathAddress, boolean hasFee, BigInteger gasPrice, BigInteger gasLimit) throws Exception;
	
	public BigInteger getAmountsOut(Credentials credentials,BigInteger inputTokens, Double slipage, StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum,  List<String> memoryPathAddress) throws Throwable;
	
	public String swapTokenForZIL(Credentials credentials, BigInteger inputTokens, BigInteger outputEthers, StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum, long deadLine, List<String> memoryPathAddress, boolean hasFee, BigInteger gasPrice, BigInteger gasLimit) throws Exception;

	
}
