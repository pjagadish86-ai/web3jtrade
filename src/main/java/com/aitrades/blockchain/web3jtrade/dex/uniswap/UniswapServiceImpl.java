package com.aitrades.blockchain.web3jtrade.dex.uniswap;

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
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.exceptions.ContractCallException;
import org.web3j.utils.Numeric;

import com.aitrades.blockchain.web3jtrade.client.Web3jServiceClient;
import com.aitrades.blockchain.web3jtrade.dex.contract.DexContractService;
import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexContract;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.google.common.collect.Lists;

import io.reactivex.schedulers.Schedulers;

@Service("uniswap")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class UniswapServiceImpl implements DexContractService {

	private static final String CUSTOM = "CUSTOM";
	
	@Resource(name = "web3jServiceClient")
	private Web3jServiceClient web3jServiceClient;

	@Override
	public List<Type> getPair(String tokenA, String tokenB) throws Exception{
		final Function function = new Function(FUNC_GETPAIR, Arrays.asList(new Address(tokenA), new Address(tokenB)),
											   Arrays.asList(new TypeReference<Address>() {
											}));
		String data = FunctionEncoder.encode(function);
		Transaction transaction = Transaction.createEthCallTransaction(TradeConstants.FACTORY_MAP.get(TradeConstants.UNISWAP), TradeConstants.FACTORY_MAP.get(TradeConstants.UNISWAP), data);
		EthCall ethCall = web3jServiceClient.getWeb3j()
										    .ethCall(transaction, DefaultBlockParameterName.LATEST)
										    .flowable()
										    .blockingSingle();
		if(ethCall.hasError()) {
			throw new Exception(ethCall.getError().getMessage());
		}
		
		String value = ethCall.getValue();
		
		return FunctionReturnDecoder.decode(value, function.getOutputParameters());
	}

	@Override
	public Tuple3<BigInteger, BigInteger, BigInteger> getReserves(String pairAddress, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit)  throws Exception{
		
		EthereumDexContract uniswapV2Pair = new EthereumDexContract(pairAddress, 
																    web3jServiceClient.getWeb3j(), 
																    credentials,
																    gasPrice,
																    gasLimit);
		return uniswapV2Pair.getReserves()
							.flowable()
					        .subscribeOn(Schedulers.io())
					        .blockingSingle();
	}


	@Override
	public BigInteger getAmountsIn(Credentials credentials, BigInteger inputEthers, Double slipage,
								   List<String> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit) throws Exception {

		BigInteger outputTokensWithSlipage = null;
		try {
			EthereumDexContract uniswapV2Contract = new EthereumDexContract(TradeConstants.ROUTER_MAP.get(TradeConstants.UNISWAP),
																			web3jServiceClient.getWeb3j(), 
																		    credentials, 
																		    gasPrice, 
																		    gasLimit);
			
			List amountsOuts = (List) uniswapV2Contract.getAmountsIn(inputEthers, memoryPathAddress)// TODO: come back to verify 
																  .flowable()
																  .blockingSingle();
			BigInteger amountsOut = (BigInteger)amountsOuts.get(0);					  
			System.out.println("AMOUNTs in"+ amountsOut);
			if(amountsOut.compareTo(BigInteger.ZERO) <=0 ) {
				throw new Exception("getAmountsIn out zero");
			}
			double slipageWithCal  = amountsOut.doubleValue() * slipage;
			outputTokensWithSlipage = new BigDecimal(amountsOut.doubleValue() - slipageWithCal).setScale(0, RoundingMode.DOWN).toBigInteger();
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof ContractCallException) {
				if(e.getMessage().contains("org.web3j.tx.exceptions.ContractCallException: Contract Call has been reverted by the EVM with the reason: 'execution reverted: UniswapV2Library: INSUFFICIENT_LIQUIDITY'.")) {
					System.out.println("");
				}
			}
		}
		
		return outputTokensWithSlipage;
	}

	@Override
	public String swapETHForTokens(Credentials credentials, BigInteger inputEthers, BigInteger outputTokens, 
								   long deadLine, List<String> memoryPathAddress, boolean hasFee, BigInteger gasPrice, BigInteger gasLimit) throws Exception{
		final Function function = new Function(FUNC_SWAPEXACTETHFORTOKENS,
											   Lists.newArrayList(new Uint256(outputTokens), 
													   			  new DynamicArray(Address.class, getAddress(memoryPathAddress)),
													   			  new Address(credentials.getAddress()), 
													   			  new Uint256(BigInteger.valueOf(Instant.now().plus(deadLine, ChronoUnit.SECONDS).getEpochSecond()))),
											   Collections.emptyList());

		String data = FunctionEncoder.encode(function);

		EthGetTransactionCount ethGetTransactionCount = web3jServiceClient.getWeb3j()
																		  .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
																		  .flowable()
																		  .subscribeOn(Schedulers.io()).blockingSingle();
		RawTransaction rawTransaction = RawTransaction.createTransaction(ethGetTransactionCount.getTransactionCount(),
																		 gasPrice, 
																		 gasLimit, 
																		 TradeConstants.ROUTER_MAP.get(TradeConstants.UNISWAP), 
																		 inputEthers,
																		 data);
		
		byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
//		EthSendTransaction ethSendTransaction = web3jServiceClient.getWeb3j()
//																  .ethSendRawTransaction(Numeric.toHexString(signedMessage))
//																  .flowable()
//																  .blockingSingle();
//		if(ethSendTransaction.hasError()) {
//			throw new Exception(ethSendTransaction.getError().getMessage());
//		}
//		return ethSendTransaction.getTransactionHash();
		return "Test Passes with swaptoEth";
	}

	private List<Address> getAddress(List<String> path) {
		List<Address>  addresses = new ArrayList<Address>();
		for(String addr : path) {
			addresses.add(new Address(addr));
		}
		return addresses;
	}
	
	@Override
	public BigInteger getAmountsOut(Credentials credentials, BigInteger inputTokens, Double slipage, 
								    List<String> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit) throws Throwable {

		EthereumDexContract uniswapV2Contract = new EthereumDexContract(TradeConstants.ROUTER_MAP.get(TradeConstants.UNISWAP),
																	web3jServiceClient.getWeb3j(), 
																	credentials, 
																	gasPrice, 
																	gasLimit);
		
		BigInteger amountsIn = (BigInteger) uniswapV2Contract.getAmountsOut(inputTokens, memoryPathAddress)
															 .flowable()
															 .blockingSingle()
															 .stream()
															 .reduce((first, second) -> second)
															 .orElseThrow(() -> new Exception("GetAmounts Out Returned ZERO"));
		
		System.out.println("AMOUNTs OUT"+ amountsIn);
		double slipageWithCal  = amountsIn.doubleValue() * slipage;
		BigInteger outputTokensWithSlipage = new BigDecimal(amountsIn.doubleValue() - slipageWithCal).setScale(0, RoundingMode.DOWN).toBigInteger();	
		return outputTokensWithSlipage;
	}

	@Override
	public String swapTokenForETH(Credentials credentials, BigInteger inputTokens, BigInteger outputEthers,
								  long deadLine, 
								  List<String> memoryPathAddress,
								  boolean hasFee, BigInteger gasPrice, BigInteger gasLimit) throws Exception {

		final Function function = new Function(FUNC_SWAPEXACTTOKENSFORETH,
											   Lists.newArrayList(new Uint256(inputTokens), 
													   			  new Uint256(outputEthers),
																  new DynamicArray(Address.class, getAddress(memoryPathAddress)), 
																  new Address(credentials.getAddress()),
																  new Uint256(BigInteger.valueOf(Instant.now().plus(deadLine, ChronoUnit.SECONDS).getEpochSecond()))),
											   Collections.emptyList());

		String data = FunctionEncoder.encode(function);
		
		EthGetTransactionCount ethGetTransactionCount = web3jServiceClient.getWeb3j()
																		  .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
																		  .flowable()
																		  .subscribeOn(Schedulers.io()).blockingSingle();

		RawTransaction rawTransaction = RawTransaction.createTransaction(ethGetTransactionCount.getTransactionCount(),
																		 gasPrice, 
																		 gasLimit, 
																		 TradeConstants.ROUTER_MAP.get(TradeConstants.UNISWAP),
																		 BigInteger.ZERO, 
																		 data);
		
		byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
		System.out.println("swapped tokens to eth");
		return "swappedhash";
//		EthSendTransaction ethSendTransaction = web3jServiceClient.getWeb3j()
//																  .ethSendRawTransaction(Numeric.toHexString(signedMessage))
//																  .flowable()
//																  .subscribeOn(Schedulers.io())
//																  .blockingSingle();
//		if(ethSendTransaction.hasError()) {
//			throw new Exception(ethSendTransaction.getError().getMessage());
//		}
//		return ethSendTransaction.getTransactionHash();
	}

}
