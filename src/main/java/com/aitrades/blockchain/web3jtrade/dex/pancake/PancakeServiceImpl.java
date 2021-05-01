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

import org.springframework.beans.factory.annotation.Autowired;
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
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.response.NoOpProcessor;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.utils.Convert;

import com.aitrades.blockchain.web3jtrade.client.Web3jServiceClient;
import com.aitrades.blockchain.web3jtrade.dex.contract.DexContractService;
import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexContract;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.aitrades.blockchain.web3jtrade.oracle.gas.GasProvider;
import com.aitrades.blockchain.web3jtrade.service.DexContractStaticCodeValuesService;

import io.reactivex.schedulers.Schedulers;

@Service("pancake")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PancakeServiceImpl implements DexContractService {

	private static final List<TypeReference<?>> GET_AMTS_IN_OUT_PARAMS = Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {});

	private static final Uint256 DEAD_LINE = new Uint256(BigInteger.valueOf(Instant.now().plus(600, ChronoUnit.SECONDS).getEpochSecond()));

	@Resource(name = "web3jBscServiceClient")
	private Web3jServiceClient web3jServiceClient;
	
	@Resource(name = "pollingTransactionReceiptProcessor")
	private PollingTransactionReceiptProcessor pollingTransactionReceiptProcessor;
	
	@Resource(name= "noOpProcessor")
	private NoOpProcessor noOpProcessor;
	
	@Autowired
	private GasProvider gasProvider;
	
 	@Autowired
	private DexContractStaticCodeValuesService dexContractStaticCodeValuesService;
 
 	
	@Override
	public List<Type> getPair(String route, String tokenA, String tokenB) throws Exception{
		
		final Function function = new Function(FUNC_GETPAIR, Arrays.asList(new Address(tokenA), new Address(tokenB)),
											   Arrays.asList(new TypeReference<Address>() {
											}));
		EthCall ethCall = web3jServiceClient.getWeb3j()
										    .ethCall(Transaction.createEthCallTransaction(dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.FACTORY), 
										    		 dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.FACTORY),
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
	public Tuple3<BigInteger, BigInteger, BigInteger> getReserves(String route, String pairAddress, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String gasMode)  throws Exception{
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
	public BigInteger getAmountsIn(String route, Credentials credentials, BigInteger inputEthers, Double slipage,
								   List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit , String gasMode, String decimals) throws Exception {
		final Function function = new Function(FUNC_GETAMOUNTIN, 
							                   Arrays.<Type>asList(new Uint256(inputEthers), 
							                   new DynamicArray<Address>(Address.class, memoryPathAddress)), 
							                   GET_AMTS_IN_OUT_PARAMS);
		String data = FunctionEncoder.encode(function);
		EthCall resp =  web3jServiceClient.getWeb3j().ethCall(Transaction.createEthCallTransaction(memoryPathAddress.get(0).getValue(), 
				dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.ROUTER), 
																									data), 
													          DefaultBlockParameterName.LATEST)
										              .flowable()
										              .subscribeOn(Schedulers.io())
										              .blockingSingle();
		
		try {
			final List<Type> response  = FunctionReturnDecoder.decode(resp.getValue(), function.getOutputParameters());
			final BigInteger amountsOut = (BigInteger)(((DynamicArray<Type>)response.get(0)).getValue().get(0).getValue());
			final double slipageWithCal  = amountsOut.doubleValue() * slipage;
			//return new BigDecimal(amountsOut.doubleValue() - slipageWithCal).setScale(0, RoundingMode.DOWN).toBigInteger();
			return Convert.toWei(Convert.fromWei(new BigDecimal(amountsOut.doubleValue() - slipageWithCal).setScale(0, RoundingMode.DOWN), 
											     Convert.Unit.fromString(TradeConstants.DECIMAL_MAP.get(decimals))).setScale(0, RoundingMode.DOWN), 
								 Convert.Unit.fromString(TradeConstants.DECIMAL_MAP.get(decimals))).setScale(0, RoundingMode.DOWN)
					      .toBigInteger();
		} catch (Exception e) {
			throw new Exception("INSUFFICIENT_LIQUIDITY");
		} 
	}

	@Override
	public String swapExactTokensForTokens(String route, Credentials credentials, BigInteger amountIn, BigInteger amountOutMin, 
								   long deadLine, List<Address> memoryPathAddress, boolean hasFee, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception{
		final String data = FunctionEncoder.encode(new Function(hasFee ? FUNC_SWAPEXACTTOKENSFORTOKENSSUPPORTINGFEEONTRANSFERTOKENS : FUNC_SWAPEXACTTOKENSFORTOKENS,
					 										    Arrays.asList(new Uint256(amountIn), new Uint256(amountOutMin),
					 										    			  new DynamicArray(Address.class, memoryPathAddress),
					 										    			  new Address(credentials.getAddress()), 
					 										    			  DEAD_LINE),
														        Collections.emptyList()));
		EthSendTransaction ethSendTransaction = new FastRawTransactionManager(web3jServiceClient.getWeb3j(), 
																			   credentials,
																			   noOpProcessor)
															.sendTransaction(gasPrice, 
																			 gasLimit, 
																			 dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.ROUTER), 
																			 data, 
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
	public BigInteger getAmountsOut(String route, Credentials credentials, BigInteger inputTokens, Double slipage, 
								    List<String> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Throwable {

		EthereumDexContract dexContract = new EthereumDexContract(dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.ROUTER),
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
	public String swapTokenForETH(String route, Credentials credentials, BigInteger inputTokens, BigInteger outputEthers,
								  long deadLine, List<String> memoryPathAddress,  boolean hasFee, 
								  BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception {

		final Function function = new Function(hasFee ? FUNC_SWAPEXACTTOKENSFORETHSUPPORTINGFEEONTRANSFERTOKENS : FUNC_SWAPEXACTTOKENSFORTOKENS,
																  Arrays.asList(new Uint256(inputTokens), 
													   			  new Uint256(outputEthers),
																  new DynamicArray(Address.class, getAddress(memoryPathAddress)), 
																  new Address(credentials.getAddress()),
																  new Uint256(BigInteger.valueOf(Instant.now().plus(deadLine, ChronoUnit.SECONDS).getEpochSecond()))),
											   Collections.emptyList());
		String data = FunctionEncoder.encode(function);
		EthSendTransaction ethSendTransaction = new FastRawTransactionManager(web3jServiceClient.getWeb3j(), 
																		      credentials,
																		      noOpProcessor)
															.sendTransaction(gasPrice, 
																			 gasProvider.gasLimitPancake(credentials.getAddress(), data, TradeConstants.PANCAKE), 
																			 dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.ROUTER), 
																			 data, 
																			 BigInteger.ZERO);
		if(ethSendTransaction.hasError()) {
			throw new Exception(ethSendTransaction.getError().getMessage());
		}
		return ethSendTransaction.getTransactionHash();
	}

	@Override
	public TransactionReceipt deposit(String route, BigInteger weiValue, Credentials credentials, BigInteger inputEthers,
			Double slipage, List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) {
		EthereumDexContract ethereumDexContract = new EthereumDexContract(dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.ROUTER),
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
	public TransactionReceipt withDraw(String route, BigInteger weiValue, Credentials credentials, BigInteger inputEthers,
			Double slipage, List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) {
		return null;
	}

	@Override
	public TransactionReceipt transfer(String route, String pairAddress, BigInteger wad, Credentials credentials, BigInteger inputEthers,
			Double slipage, List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) {
		return new EthereumDexContract(dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.WNATIVE),
				   web3jServiceClient.getWeb3j(), 
				   credentials,
				   gasPrice,
				   gasLimit).transfer(pairAddress, inputEthers).flowable().subscribeOn(Schedulers.io()).blockingSingle();
	}


}
