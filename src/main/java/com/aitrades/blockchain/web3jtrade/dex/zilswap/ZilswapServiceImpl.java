package com.aitrades.blockchain.web3jtrade.dex.zilswap;

import java.math.BigInteger;
import java.util.List;

import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple3;

import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.oracle.gas.StrategyGasProvider;
@Service("zilswap")
@SuppressWarnings({ "rawtypes" })
public class ZilswapServiceImpl  implements ZilService {

	
	@Override
	public List<Type> getPair(String tokenA, String tokenB) throws Exception {
		return null;
	}

	@Override
	public Tuple3<BigInteger, BigInteger, BigInteger> getReserves(String pairAddress, Credentials credentials,
																  StrategyGasProvider customGasProvider) throws Exception {
		return null;
	}

	@Override
	public BigInteger getAmountsIn(Credentials credentials, BigInteger inputEthers, Double slipage,
									StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum, List<String> memoryPathAddress)
									throws Exception {
		return null;
	}

	@Override
	public String swapZILForTokens(Credentials credentials, BigInteger inputEthers, BigInteger outPutTokens,
									StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum, long deadLine,
									List<String> memoryPathAddress, boolean hasFee, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
		return null;
	}

	@Override
	public BigInteger getAmountsOut(Credentials credentials, BigInteger inputTokens, Double slipage,
			StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum, List<String> memoryPathAddress)
			throws Throwable {
		return null;
	}

	@Override
	public String swapTokenForZIL(Credentials credentials, BigInteger inputTokens, BigInteger outputEthers,
			StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum, long deadLine,
			List<String> memoryPathAddress, boolean hasFee, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
		return null;
	}

}
