package com.aitrades.blockchain.web3jtrade.dex.pancake;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.response.NoOpProcessor;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.utils.Numeric;

import com.aitrades.blockchain.web3jtrade.client.Web3jServiceClient;
import com.aitrades.blockchain.web3jtrade.dex.contract.DexContractService;
import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexContract;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;

import io.reactivex.schedulers.Schedulers;

@Service("pancake")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PancakeServiceImpl implements DexContractService {

	private static final Uint256 DEAD_LINE = new Uint256(BigInteger.valueOf(Instant.now().plus(600, ChronoUnit.SECONDS).getEpochSecond()));

	private static final String CUSTOM = "CUSTOM";
	
	@Resource(name = "web3jBscServiceClient")
	private Web3jServiceClient web3jServiceClient;
	
	@Resource(name = "pollingTransactionReceiptProcessor")
	private PollingTransactionReceiptProcessor pollingTransactionReceiptProcessor;
	
	@Resource(name= "noOpProcessor")
	private NoOpProcessor noOpProcessor;

	@Override
	public List<Type> getPair(String tokenA, String tokenB) throws Exception{
		
		final Function function = new Function(FUNC_GETPAIR, Arrays.asList(new Address(tokenA), new Address(tokenB)),
											   Arrays.asList(new TypeReference<Address>() {
											}));
		EthCall ethCall = web3jServiceClient.getWeb3j()
										    .ethCall(Transaction.createEthCallTransaction(TradeConstants.PANCAKE_FACTORY_ADDRESS, 
													 TradeConstants.PANCAKE_FACTORY_ADDRESS, 
													 FunctionEncoder.encode(function)),
										    		DefaultBlockParameterName.LATEST)
										    .flowable()
										    .blockingSingle();
		if(ethCall.hasError()) {
			throw new Exception(ethCall.getError().getMessage());
		}
		return FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
	}

	@Override
	public Tuple3<BigInteger, BigInteger, BigInteger> getReserves(String pairAddress, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String gasMode)  throws Exception{
		return new EthereumDexContract(pairAddress,
									   web3jServiceClient.getWeb3j(), 
									   credentials,
									   gasPrice,
									   gasLimit).getReserves()
											    .flowable()
									            .subscribeOn(Schedulers.io())
									            .blockingSingle();
	}


	@Override
	public BigInteger getAmountsIn(Credentials credentials, BigInteger inputEthers, Double slipage,
								   List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit , String gasMode) throws Exception {
		List amountsOuts = new EthereumDexContract(TradeConstants.PANCAKE_ROUTER_ADDRESS,
												  web3jServiceClient.getWeb3j(), 
											      credentials, 
											      gasPrice, 
											      gasLimit)
									   .getAmountsIn(inputEthers, memoryPathAddress)
									  .flowable()
									  .blockingSingle();
		BigInteger amountsOut = (BigInteger)amountsOuts.get(0);
		double slipageWithCal  = amountsOut.doubleValue() * slipage;
		BigDecimal setScale = new BigDecimal(amountsOut.doubleValue() - slipageWithCal).setScale(0, RoundingMode.DOWN);
		return setScale.toBigInteger();
	}

	@Override
	public String swapExactTokensForTokens(Credentials credentials, BigInteger amountIn, BigInteger amountOutMin, 
								   long deadLine, List<Address> memoryPathAddress, boolean hasFee, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception{
		EthSendTransaction ethSendTransaction = new FastRawTransactionManager(web3jServiceClient.getWeb3j(), 
																			   credentials,
																			   noOpProcessor)
															.sendTransaction(gasPrice, 
																			 gasLimit, 
																			 TradeConstants.PANCAKE_ROUTER_ADDRESS, 
																			 FunctionEncoder.encode(new Function(hasFee ? FUNC_SWAPEXACTTOKENSFORTOKENSSUPPORTINGFEEONTRANSFERTOKENS : FUNC_SWAPEXACTTOKENSFORTOKENS,
																					 										  Arrays.asList(new Uint256(amountIn), new Uint256(amountOutMin),
																												   			  new DynamicArray(Address.class, memoryPathAddress),
																												   			  new Address(credentials.getAddress()), 
																												   			  DEAD_LINE),
																								   Collections.emptyList())), 
																			 BigInteger.ZERO);
		if(ethSendTransaction.hasError()) {
			throw new Exception(ethSendTransaction.getError().getMessage());
		}
		return ethSendTransaction.getTransactionHash();
	}

	private List<Address> getAddress(List<String> path) {
		List<Address> addresses = new ArrayList<>();
		for(String addr : path) {
			addresses.add(new Address(addr));
		}
		return addresses;
	}
	
	@Override
	public BigInteger getAmountsOut(Credentials credentials, BigInteger inputTokens, Double slipage, 
								    List<String> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Throwable {

		EthereumDexContract dexContract = new EthereumDexContract(TradeConstants.ROUTER_MAP.get(TradeConstants.PANCAKE),
																 web3jServiceClient.getWeb3j(), 
																 credentials, 
																 gasPrice, 
																 gasLimit);
		
		List amountsIn = dexContract.getAmountsOut(inputTokens, memoryPathAddress)
													   .flowable()
													   .blockingSingle();
		
		BigInteger amountsOut = (BigInteger)amountsIn.get(1);	
		double slipageWithCal  = amountsOut.doubleValue() * slipage;
		return BigDecimal.valueOf(amountsOut.doubleValue() - slipageWithCal).setScale(0, RoundingMode.DOWN).toBigInteger();
	}

	@Override
	public String swapTokenForETH(Credentials credentials, BigInteger inputTokens, BigInteger outputEthers,
								  long deadLine, List<String> memoryPathAddress,  boolean hasFee, 
								  BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception {

		final Function function = new Function(hasFee ? FUNC_SWAPEXACTTOKENSFORETHSUPPORTINGFEEONTRANSFERTOKENS : FUNC_SWAPEXACTTOKENSFORETH,
																  Arrays.asList(new Uint256(inputTokens), 
													   			  new Uint256(outputEthers),
																  new DynamicArray(Address.class, getAddress(memoryPathAddress)), 
																  new Address(credentials.getAddress()),
																  new Uint256(BigInteger.valueOf(Instant.now().plus(deadLine, ChronoUnit.SECONDS).getEpochSecond()))),
											   Collections.emptyList());
		String data = FunctionEncoder.encode(function);
		BigInteger gasLmt = StringUtils.equalsIgnoreCase(gasMode, CUSTOM) ? gasLimit : BigInteger.valueOf(21000l).add(BigInteger.valueOf(68l)
																														.multiply(BigInteger.valueOf(data.getBytes().length)));
	
		EthGetTransactionCount ethGetTransactionCount = web3jServiceClient.getWeb3j()
																		  .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
																		  .flowable()
																		  .subscribeOn(Schedulers.io()).blockingSingle();

		RawTransaction rawTransaction = RawTransaction.createTransaction(ethGetTransactionCount.getTransactionCount(),
																		 gasPrice, 
																		 gasLmt, 
																		 TradeConstants.ROUTER_MAP.get(TradeConstants.PANCAKE),
																		 BigInteger.ZERO, 
																		 data);
		
		EthSendTransaction ethSendTransaction = web3jServiceClient.getWeb3j()
																  .ethSendRawTransaction(Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, credentials)))
																  .flowable()
																  .subscribeOn(Schedulers.io())
																  .blockingSingle();
		if(ethSendTransaction.hasError()) {
			throw new Exception(ethSendTransaction.getError().getMessage());
		}
		return ethSendTransaction.getTransactionHash();
	}

	@Override
	public TransactionReceipt deposit(BigInteger weiValue, Credentials credentials, BigInteger inputEthers,
			Double slipage, List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) {
		EthereumDexContract ethereumDexContract = new EthereumDexContract(TradeConstants.ROUTER_MAP.get(TradeConstants.PANCAKE),
											   web3jServiceClient.getWeb3j(), 
											   credentials,
											   gasPrice,
											   gasLimit);
		TransactionReceipt blockingSingle = null;
		try {
			blockingSingle = ethereumDexContract.deposit(inputEthers).flowable().subscribeOn(Schedulers.io()).blockingSingle();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("rever reason-> "+blockingSingle.getRevertReason());
		return blockingSingle;
	}

	@Override
	public TransactionReceipt withDraw(BigInteger weiValue, Credentials credentials, BigInteger inputEthers,
			Double slipage, List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) {
		return null;
	}

	@Override
	public TransactionReceipt transfer(String pairAddress, BigInteger wad, Credentials credentials, BigInteger inputEthers,
			Double slipage, List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) {
		return new EthereumDexContract(TradeConstants.WETH_MAP.get(TradeConstants.PANCAKE),
				   web3jServiceClient.getWeb3j(), 
				   credentials,
				   gasPrice,
				   gasLimit).transfer(pairAddress, inputEthers).flowable().subscribeOn(Schedulers.io()).blockingSingle();
	}


}
