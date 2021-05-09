package com.aitrades.blockchain.web3jtrade.dex.uniswap;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint160;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import com.aitrades.blockchain.web3jtrade.dex.contract.DexContractServiceV3;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;
import com.google.common.collect.Lists;

import io.reactivex.schedulers.Schedulers;
@Service("uniswapVersion3")
public class UniswapV3Service implements DexContractServiceV3 {

	@Autowired
	private Web3jServiceClientFactory  web3jServiceClientFactory;
	
	@Override
	public BigInteger getAmountsIn(String route, Map<String, Object> requestMap, Credentials credentials) throws Exception {
		return null;
	}

	@Override
	public String fetchSignedTransaction(String route, Map<String, Object> requestMap, Credentials credentials) throws Exception {
		List<Type> inputparams = Arrays.asList( new Address((String)requestMap.get(TradeConstants.WNATIVE)), 
												new Address((String)requestMap.get(TradeConstants.TOADDRESS)),
												new Uint256(BigInteger.ZERO),
												new Address(credentials.getAddress()),
											    new Uint256(BigInteger.valueOf(Instant.now().plus(5, ChronoUnit.MINUTES).getEpochSecond())),
											    new Uint256((BigInteger)requestMap.get(TradeConstants.AMOUNTS_IN)),
											    new Uint256((BigInteger)requestMap.get(TradeConstants.AMOUNTS_OUT)), 
											    new Uint160(BigInteger.ZERO));
		
//		address tokenIn;
//        address tokenOut;
//        uint24 fee;
//        address recipient;
//        uint256 deadline;
//        uint256 amountIn;
//        uint256 amountOutMinimum;
//        uint160 sqrtPriceLimitX96;
		
		 final StringBuilder data = new StringBuilder();
		 data.append("0x414bf389");
	     for (Type parameter : inputparams) {
	            data.append(TypeEncoder.encode(parameter));
	        }
	     
	     System.out.println(data.toString());
		EthGetTransactionCount ethGetTransactionCount = web3jServiceClientFactory.getWeb3jMap(route).getWeb3j()
																				 .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
																				 .flowable()
																				 .subscribeOn(Schedulers.io())
																				 .blockingSingle();

		RawTransaction rawTransaction = RawTransaction.createTransaction(ethGetTransactionCount.getTransactionCount(),
																		 (BigInteger)requestMap.get(TradeConstants.GAS_PRICE), 
																		 (BigInteger)requestMap.get(TradeConstants.GAS_LIMIT),
																		 (String)requestMap.get(TradeConstants.ROUTER), 
																		 BigInteger.ZERO,
																		 data.toString());

		return Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, credentials));
	}

	@Override
	public BigInteger getAmountsOut(String route,Map<String, Object> requestMap, Credentials credentials) throws Throwable {
		return null;
	}

	@Override
	public String swapTokensForTokens(String route, Map<String, Object> requestMap, Credentials credentials) throws Exception {
		EthSendTransaction ethSendTransaction = web3jServiceClientFactory.getWeb3jMap(route).getWeb3j()
																		 .ethSendRawTransaction((String)requestMap.get(TradeConstants.SIGNED_TRANSACTION))
																		 .flowable()
																		 .subscribeOn(Schedulers.io())
																		 .blockingSingle();
		if(ethSendTransaction.hasError()) {
			throw new Exception(ethSendTransaction.getError().getMessage());
		}
		return ethSendTransaction.getTransactionHash();
	}

}
