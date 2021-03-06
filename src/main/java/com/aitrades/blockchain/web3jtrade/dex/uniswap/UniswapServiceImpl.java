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

import org.apache.commons.lang.StringUtils;
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
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import com.aitrades.blockchain.web3jtrade.dex.contract.DexContractService;
import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexContract;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.aitrades.blockchain.web3jtrade.service.DexContractStaticCodeValuesService;
import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;

import io.reactivex.schedulers.Schedulers;

@Service("uniswap")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class UniswapServiceImpl implements DexContractService {

	
//	Function: exactInputSingle((address,address,uint24,address,uint256,uint256,uint256,uint160))
//	#	Name	Type	Data
//	0	params.tokenIn	address	0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2
//	0	params.tokenOut	address	0x6B175474E89094C44Da98b954EedeAC495271d0F
//	0	params.recipient	address	0x7B74B57c89A73145Fe1915f45d8c23682fF78341
//	0	params.deadline	uint256	1620387189
//	0	params.amountIn	uint256	100000000000000
//	0	params.amountOutMinimum	uint256	314631682153778871
//	0	params.sqrtPriceLimitX96	uint160	0

	private static final List<TypeReference<?>> GET_AMOUNTS_IN_OUTPUT = Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {});

	@Autowired
	private Web3jServiceClientFactory  web3jServiceClientFactory;
	
	@Autowired
	private DexContractStaticCodeValuesService dexContractStaticCodeValuesService;
 
 
	@Override
	public List<Type> getPair(String route, String tokenA, String tokenB) throws Exception{
		final Function function = new Function(FUNC_GETPAIR, Arrays.asList(new Address(tokenA), new Address(tokenB)),
											   Arrays.asList(new TypeReference<Address>() {
											}));
		Transaction transaction = Transaction.createEthCallTransaction(dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.FACTORY), 
																	dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.FACTORY), 
																	  FunctionEncoder.encode(function));
		EthCall ethCall = web3jServiceClientFactory.getWeb3jMap(route).getWeb3j()
										    .ethCall(transaction, DefaultBlockParameterName.LATEST)
										    .flowable()
										    .blockingSingle();
		if(ethCall.hasError()) {
			throw new Exception(ethCall.getError().getMessage());
		}
		return FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
	}

	@Override
	public Tuple3<BigInteger, BigInteger, BigInteger> getReserves(String route, String pairAddress, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception{
		return new EthereumDexContract(pairAddress, web3jServiceClientFactory.getWeb3jMap(route).getWeb3j(), 
															      credentials,
															      gasPrice,
															      gasLimit).getReserves()
																		  .flowable()
																          .subscribeOn(Schedulers.io())
																          .blockingSingle();
	}


	@Override
	public BigInteger getAmountsIn(String route, Credentials credentials, BigInteger inputEthers, Double slipage,
								   List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode, String decimals) throws Exception {
		final Function function = new Function(FUNC_GETAMOUNTIN, 
								                Arrays.<Type>asList(new Uint256(inputEthers), 
								                new DynamicArray<Address>(Address.class, memoryPathAddress)), 
								                GET_AMOUNTS_IN_OUTPUT);
		final String data = FunctionEncoder.encode(function);
		EthCall resp =  web3jServiceClientFactory.getWeb3jMap(route).getWeb3j().ethCall(Transaction.createEthCallTransaction(memoryPathAddress.get(0).getValue(), 
				dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.ROUTER ), 
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
								   		   long deadLine, List<Address> memoryPathAddress, boolean hasFee, 
								   		   BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception{
		EthSendTransaction ethSendTransaction = new FastRawTransactionManager(web3jServiceClientFactory.getWeb3jMap(route).getWeb3j(), 
																		      credentials,
																		      new NoOpProcessor(web3jServiceClientFactory.getWeb3jMap(route).getWeb3j()))
												.sendTransaction(gasPrice, 
																 gasLimit, 
																 dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.ROUTER ), 
																 FunctionEncoder.encode(new Function(hasFee ? FUNC_SWAPEXACTTOKENSFORTOKENSSUPPORTINGFEEONTRANSFERTOKENS : FUNC_SWAPEXACTETHFORTOKENS,
																		 										  Arrays.asList(new Uint256(amountIn), new Uint256(amountOutMin), 
																									   			  new DynamicArray(Address.class, memoryPathAddress),
																									   			  new Address(credentials.getAddress()), 
																									   			  new Uint256(BigInteger.valueOf(Instant.now().plus(5, ChronoUnit.MINUTES).getEpochSecond()))),
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
	public BigInteger getAmountsOut(String route, Credentials credentials, BigInteger inputTokens, Double slipage, 
								    List<String> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Throwable {

		EthereumDexContract dexContract = new EthereumDexContract(dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.ROUTER ),
																  web3jServiceClientFactory.getWeb3jMap(route).getWeb3j(), 
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

		final Function function = new Function(hasFee ? FUNC_SWAPEXACTTOKENSFORETHSUPPORTINGFEEONTRANSFERTOKENS : FUNC_SWAPEXACTTOKENSFORETH,
																  Arrays.asList(new Uint256(inputTokens), 
													   			  new Uint256(outputEthers),
																  new DynamicArray(Address.class, getAddress(memoryPathAddress)), 
																  new Address(credentials.getAddress()),
																  new Uint256(BigInteger.valueOf(Instant.now().plus(5, ChronoUnit.MINUTES).getEpochSecond()))),
											   Collections.emptyList());

		String encode = FunctionEncoder.encode(function);
		BigInteger gasLmt = StringUtils.equalsIgnoreCase(gasMode, TradeConstants.CUSTOM) ? gasLimit : BigInteger.valueOf(21000l).add(BigInteger.valueOf(68l)
																														.multiply(BigInteger.valueOf(encode.getBytes().length)));
		
		EthGetTransactionCount ethGetTransactionCount = web3jServiceClientFactory.getWeb3jMap(route).getWeb3j()
																		  .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
																		  .flowable()
																		  .subscribeOn(Schedulers.io())
																		  .blockingSingle();

		RawTransaction rawTransaction = RawTransaction.createTransaction(ethGetTransactionCount.getTransactionCount(),
																		 gasPrice, 
																		 gasLmt, 
																		 dexContractStaticCodeValuesService.getDexContractAddress(route, TradeConstants.ROUTER ),
																		 BigInteger.ZERO, 
																		 encode);
		
		String hexString = Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, credentials));
		EthSendTransaction ethSendTransaction = web3jServiceClientFactory.getWeb3jMap(route).getWeb3j()
																  .ethSendRawTransaction(hexString)
																  .flowable()
																  .subscribeOn(Schedulers.io())
																  .blockingSingle();
		if(ethSendTransaction.hasError()) {
			throw new Exception(ethSendTransaction.getError().getMessage());
		}
		return ethSendTransaction.getTransactionHash();
	}

	@Override
	public TransactionReceipt deposit(String route, BigInteger weiValue, Credentials credentials, BigInteger inputEthers,
			Double slipage, List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransactionReceipt withDraw(String route, BigInteger weiValue, Credentials credentials, BigInteger inputEthers,
			Double slipage, List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransactionReceipt transfer(String route, String dst, BigInteger wad, Credentials credentials, BigInteger inputEthers,
			Double slipage, List<Address> memoryPathAddress, BigInteger gasPrice, BigInteger gasLimit, String gasMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String fetchSignedTransaction(String route, Credentials credentials, BigInteger inputEthers,
			BigInteger outPutTokens, long deadLine, List<Address> memoryPathAddress, boolean hasFee,
			BigInteger gasPrice, BigInteger gasLimit, String gasMode) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
