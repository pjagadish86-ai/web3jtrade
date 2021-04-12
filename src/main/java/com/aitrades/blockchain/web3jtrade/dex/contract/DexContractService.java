package com.aitrades.blockchain.web3jtrade.dex.contract;

import java.math.BigInteger;
import java.util.List;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple3;
@SuppressWarnings({"rawtypes" })
public interface DexContractService {

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
    
	public List<Type> getPair(String tokenA, String tokenB) throws Exception;
	
	public Tuple3<BigInteger, BigInteger, BigInteger> getReserves(String pairAddress, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception;
	
	public BigInteger getAmountsIn(Credentials credentials, BigInteger inputEthers, Double slipage, List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception;

	public String swapETHForTokens(Credentials credentials, BigInteger inputEthers, BigInteger outPutTokens, long deadLine, List<Address> memoryPathAddress, boolean hasFee, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception;
	
	public BigInteger getAmountsOut(Credentials credentials,BigInteger inputTokens, Double slipage, List<String> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Throwable;
	
	public String swapTokenForETH(Credentials credentials, BigInteger inputTokens, BigInteger outputEthers, long deadLine, List<String> memoryPathAddress, boolean hasFee, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception;

}