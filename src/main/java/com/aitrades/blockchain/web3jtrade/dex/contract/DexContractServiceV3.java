package com.aitrades.blockchain.web3jtrade.dex.contract;

import java.math.BigInteger;
import java.util.Map;

import org.web3j.crypto.Credentials;
public interface DexContractServiceV3 {

    public static BigInteger MAX_UINT256 = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);

    public static final String FUNC_GETAMOUNTOUT = "getAmountsOut";
    public static final String FUNC_GETAMOUNTIN = "getAmountsIn";

    // V3
    public static final String FUNC_EXACTINPUTSINGLE = "exactInputSingle";
    
	public BigInteger getAmountsIn(String route, Map<String, Object> requestMap, Credentials credentials) throws Exception;

	public String fetchSignedTransaction(String route, Map<String, Object> requestMap, Credentials credentials) throws Exception;
	
	public BigInteger getAmountsOut(String route, Map<String, Object> requestMap, Credentials credentials) throws Throwable;
	
	public String swapTokensForTokens(String route, Map<String, Object> requestMap, Credentials credentials) throws Exception;

}