package com.aitrades.blockchain.web3jtrade.dex.pancake;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import com.aitrades.blockchain.web3jtrade.client.Web3jServiceClient;
import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexContract;
import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexContractService;
import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.StrategyGasProvider;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.google.common.collect.Lists;

import io.reactivex.schedulers.Schedulers;

@Service("pancake")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PancakeServiceImpl implements EthereumDexContractService {
	
	@Resource(name = "web3jBscServiceClient")
	private Web3jServiceClient web3jServiceClient;
	
	@Override
	public List<Type> getPair(String tokenA, String tokenB) throws Exception{
		final Function function = new Function(FUNC_GETPAIR, Arrays.asList(new Address(tokenA), new Address(tokenB)),
											   Arrays.asList(new TypeReference<Address>() {
											}));
		String data = FunctionEncoder.encode(function);
		Transaction transaction = Transaction.createEthCallTransaction(TradeConstants.FACTORY_MAP.get(TradeConstants.PANCAKE), 
																	   TradeConstants.FACTORY_MAP.get(TradeConstants.PANCAKE), 
																	   data);
		EthCall blockingSingle = web3jServiceClient.getWeb3j()
												   .ethCall(transaction, DefaultBlockParameterName.LATEST)
												   .flowable().blockingSingle();
		String value = blockingSingle.getValue();
		return FunctionReturnDecoder.decode(value, function.getOutputParameters());
	}

	@Override
	public Tuple3<BigInteger, BigInteger, BigInteger> getReserves(String pairAddress, Credentials credentials, StrategyGasProvider contractGasProvider) throws Exception{
		EthereumDexContract uniswapV2Pair = new EthereumDexContract(pairAddress, 
																web3jServiceClient.getWeb3j(), 
																credentials,
																contractGasProvider);
		return uniswapV2Pair.getReserves().flowable().blockingLast();
	}

	@Override
	public String approve(Credentials credentials, String contractAddress, StrategyGasProvider customGasProvider,
						  GasModeEnum gasModeEnum) throws Exception{

		final Function approveFunction = new Function(FUNC_APPROVE,
													  Lists.newArrayList(new Address(TradeConstants.ROUTER_MAP.get(TradeConstants.PANCAKE)), new Uint256(MAX_UINT256)),
													  Collections.emptyList());
		String data = FunctionEncoder.encode(approveFunction);
		
		EthGetTransactionCount ethGetTransactionCountFlowable = web3jServiceClient.getWeb3j()
																				  .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.PENDING)
																				  .flowable()
																				  .subscribeOn(Schedulers.io()).blockingSingle();

		RawTransaction rawTransaction = RawTransaction.createTransaction(ethGetTransactionCountFlowable.getTransactionCount(), 
																		 customGasProvider.getGasPricePancake(gasModeEnum), 
																		 customGasProvider.getGasLimitOfPancake(true),  
																		 contractAddress, 
																		 BigInteger.ZERO, 
																		 data);
		byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
		
		String hash = web3jServiceClient.getWeb3j()
									    .ethSendRawTransaction(Numeric.toHexString(signedMessage))
									    .flowable()
									    .blockingSingle().getTransactionHash();
		return hash;
	}

	@Override
	public BigInteger getAmountsIn(Credentials credentials, BigDecimal inputTokens, BigDecimal slipage,
								   StrategyGasProvider customGasProvider,
								   GasModeEnum gasModeEnum, List<String> memoryPathAddress) throws Exception{

		EthereumDexContract pancakeContract = new EthereumDexContract(TradeConstants.ROUTER_MAP.get(TradeConstants.PANCAKE),
																	  web3jServiceClient.getWeb3j(), 
																	  credentials, 
																	  customGasProvider);
		BigInteger amountsIn = (BigInteger) pancakeContract.getAmountsOut(Convert.toWei(inputTokens, Convert.Unit.ETHER).toBigInteger(), memoryPathAddress)
														   .flowable()
														   .blockingSingle()
														   .stream()
														   .reduce((first, second) -> second)
														   .orElse(BigInteger.ZERO);

		double slipageWithCal  = amountsIn.doubleValue() * slipage.doubleValue();
		return new BigDecimal(amountsIn.doubleValue() - slipageWithCal).setScale(0, RoundingMode.DOWN).toBigInteger();	
	}

	@Override
	public String swapETHForTokens(Credentials credentials, BigInteger inputEthers,BigInteger outputTokens ,StrategyGasProvider customGasProvider,
								   GasModeEnum gasModeEnum, long deadLine, List<String> memoryPathAddress, boolean hasFee) throws Exception {

		final Function function = new Function(FUNC_SWAPEXACTETHFORTOKENS,
											   Lists.newArrayList(new Uint256(outputTokens), 
													   			  new DynamicArray(Address.class, memoryPathAddress),
													   			  new Address(credentials.getAddress()), 
													   			  new Uint256(BigInteger.valueOf(Instant.now().plus(deadLine, ChronoUnit.MINUTES).getEpochSecond()))),
											   Collections.emptyList());

		String data = FunctionEncoder.encode(function);

		EthGetTransactionCount ethGetTransactionCount = web3jServiceClient.getWeb3j()
																		  .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
																		  .flowable()
																		  .subscribeOn(Schedulers.io()).blockingSingle();

		RawTransaction rawTransaction = RawTransaction.createTransaction(ethGetTransactionCount.getTransactionCount(),
																		 customGasProvider.getGasPricePancake(gasModeEnum), 
																		 customGasProvider.getGasLimitOfPancake(true), 
																		 TradeConstants.ROUTER_MAP.get(TradeConstants.PANCAKE), 
																		 inputEthers,
																		 data);
		byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);

		EthSendTransaction ethSendTransaction = web3jServiceClient.getWeb3j()
																  .ethSendRawTransaction(Numeric.toHexString(signedMessage)).flowable().subscribeOn(Schedulers.io())
																  .blockingSingle();
		if (ethSendTransaction.hasError()) {
			throw new Exception(ethSendTransaction.getError().getMessage());
		}
		
		return ethSendTransaction.getTransactionHash();
	}

	@Override
	public BigInteger getAmountsOut(Credentials credentials, BigDecimal inputTokens, BigDecimal slipage,
								    StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum, 
								    List<String> memoryPathAddress) throws Exception{

		EthereumDexContract pancakeContract = new EthereumDexContract(TradeConstants.ROUTER_MAP.get(TradeConstants.PANCAKE),
																	  web3jServiceClient.getWeb3j(), 
																	  credentials, 
																	  customGasProvider);
		
		BigInteger amountsIn = (BigInteger)pancakeContract.getAmountsOut(Convert.toWei(inputTokens, Convert.Unit.ETHER).toBigInteger(), memoryPathAddress)
														  .flowable()
														  .blockingSingle()
														  .stream()
														  .reduce((first, second) -> second)
														  .orElse(BigInteger.ZERO);

		double slipageWithCal  = amountsIn.doubleValue() * slipage.doubleValue();
		return new BigDecimal(amountsIn.doubleValue() - slipageWithCal).setScale(0, RoundingMode.DOWN).toBigInteger();	
	}

	@Override
	public String swapTokenForETH(Credentials credentials, BigInteger inputTokens, BigInteger outputEthers,
								  StrategyGasProvider customGasProvider, GasModeEnum gasModeEnum, long deadLine, List<String> memoryPathAddress,
								  boolean hasFee) throws Exception{

		final Function function = new Function(FUNC_SWAPEXACTTOKENSFORETH,
											   Lists.newArrayList(new Uint256(inputTokens), new Uint256(outputEthers),
														new DynamicArray(Address.class, memoryPathAddress), new Address(credentials.getAddress()),
														new Uint256(BigInteger.valueOf(Instant.now().plus(deadLine, ChronoUnit.MINUTES).getEpochSecond()))),
											   Collections.emptyList());
		
		String data = FunctionEncoder.encode(function);
		
		EthGetTransactionCount ethGetTransactionCount = web3jServiceClient.getWeb3j()
																		  .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
																		  .flowable()
																		  .subscribeOn(Schedulers.io()).blockingSingle();

		RawTransaction rawTransaction = RawTransaction.createTransaction(ethGetTransactionCount.getTransactionCount(),
																		 customGasProvider.getGasPricePancake(gasModeEnum), 
																		 customGasProvider.getGasLimitOfPancake(true),  
																		 TradeConstants.ROUTER_MAP.get(TradeConstants.PANCAKE),
																		 BigInteger.ZERO, 
								  										 data);
		
		byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
		
		EthSendTransaction ethSendTransaction = web3jServiceClient.getWeb3j()
																  .ethSendRawTransaction(Numeric.toHexString(signedMessage)).flowable().subscribeOn(Schedulers.io())
																  .blockingSingle();
		if (ethSendTransaction.hasError()) {
			throw new Exception(ethSendTransaction.getError().getMessage());
		}
		return ethSendTransaction.getTransactionHash();
	}

}
