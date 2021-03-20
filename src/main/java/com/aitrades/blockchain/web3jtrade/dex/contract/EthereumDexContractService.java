package com.aitrades.blockchain.web3jtrade.dex.contract;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple3;

import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.StrategyGasProvider;
@SuppressWarnings({"rawtypes" })
public interface EthereumDexContractService {

    public static BigInteger MAX_UINT256 = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);

    public static final String FUNC_GETPAIR = "getPair";
    
    public static final String FUNC_GETAMOUNTOUT = "getAmountsOut";
    public static final String FUNC_GETAMOUNTIN = "getAmountsIn";
    
    public static final String FUNC_GETRESERVES = "getReserves";
    public static final String FUNC_APPROVE = "approve";
    
    public static final String FUNC_SWAPEXACTETHFORTOKENS = "swapExactETHForTokens";
    public static final String FUNC_SWAPEXACTETHFORTOKENSSUPPORTINGFEEONTRANSFERTOKENS = "swapExactETHForTokensSupportingFeeOnTransferTokens";
    
    public static final String FUNC_SWAPEXACTTOKENSFORETH = "swapExactTokensForETH";
    public static final String FUNC_SWAPEXACTTOKENSFORETHSUPPORTINGFEEONTRANSFERTOKENS = "swapExactTokensForETHSupportingFeeOnTransferTokens";
    
	public List<Type> getPair(String tokenA, String tokenB);
	
	public Tuple3<BigInteger, BigInteger, BigInteger> getReserves(String pairAddress, Credentials credentials, StrategyGasProvider customGasProvider);
	
	public String approve(Credentials credentials, String contractAddress, StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum);
	
	public BigDecimal getAmountsIn(Credentials credentials, BigDecimal inputEthers, StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum, List<String> memoryPathAddress);
	
	public String swapETHForTokens(Credentials credentials, BigInteger inputEthers, BigInteger outPutTokens, StrategyGasProvider customGasProvider,  GasModeEnum gasModeEnum,  long deadLine, List<String> memoryPathAddress, boolean hasFee);
	
	public BigDecimal getAmountsOut(Credentials credentials,BigDecimal inputTokens, StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum,  List<String> memoryPathAddress);
	
	public String swapTokenForETH(Credentials credentials, BigInteger inputTokens, BigInteger outputEthers, StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum, long deadLine, List<String> memoryPathAddress, boolean hasFee);
	
}