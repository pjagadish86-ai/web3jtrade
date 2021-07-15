
package com.aitrades.blockchain.web3jtrade.integration.snipe;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;

import com.aitrades.blockchain.web3jtrade.client.DexNativePriceOracleClient;
import com.aitrades.blockchain.web3jtrade.dex.contract.DexTradeContractService;
import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.Reserves;
import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.aitrades.blockchain.web3jtrade.domain.TradeOverview;
import com.aitrades.blockchain.web3jtrade.integration.snipe.mq.SnipeOrderReQueue;
import com.aitrades.blockchain.web3jtrade.oracle.gas.GasProvider;
import com.aitrades.blockchain.web3jtrade.repository.SnipeOrderHistoryRepository;
import com.aitrades.blockchain.web3jtrade.repository.SnipeOrderRepository;
import com.aitrades.blockchain.web3jtrade.repository.TradeOverviewRepository;
import com.aitrades.blockchain.web3jtrade.service.DexContractStaticCodeValuesService;
import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
@SuppressWarnings({"unused", "rawtypes"})
public class SnipeExeEndpointV1{
	
	private static final String RUNDLL32_URL_DLL_FILE_PROTOCOL_HANDLER = "rundll32 url.dll,FileProtocolHandler ";
	private static final String FAILED = "FAILED";
	private static final String SNIPE = "SNIPE";
	private static final String _0X000000 = "0x000000";

	@Autowired
	private Web3jServiceClientFactory web3jServiceClientFactory;

	@Resource(name="orderSubmitRabbitTemplate")
	private AmqpTemplate orderSubmitRabbitTemplate;
	
	@Autowired
	private GasProvider gasProvider;
	
	@Autowired
	private DexNativePriceOracleClient dexNativePriceOracleClient2;
	
	@Resource(name="snipeTransactionRequestObjectReader")
	private ObjectReader snipeTransactionRequestObjectReader;
	
	@Autowired
	private DexTradeContractService ethereumDexTradeService;
	
	@Autowired
	private SnipeOrderRepository snipeOrderRepository;
	
	@Autowired
	private SnipeOrderHistoryRepository snipeOrderHistoryRepository;
	
	@Autowired 
	private TradeOverviewRepository tradeOverviewRepository;
	
	@Autowired
	private SnipeOrderReQueue snipeOrderReQueue;
	
	@Autowired
	private DexNativePriceOracleClient dexNativePriceOracleClient;
	
	@Autowired
	private LiquidityEventOrReserversFinder liquidityEventOrReserversFinder;
	
	@Autowired
	private DexContractStaticCodeValuesService dexContractStaticCodeValuesService;
	
	@Autowired
	private LiquidityEventFinder liquidityEventFinder;
	
	private static final String ZERO_X = "0x";
	private static final String MINT = "Mint";
	
	private Set<String> contractAddress = new HashSet<>();
	
	@Transformer(inputChannel = "snipeOrderMQReciever", outputChannel = "snipeSwapChannel")
	public SnipeTransactionRequest snipeOrderMQReciever(byte[] message) throws Exception{
		return snipeTransactionRequestObjectReader.readValue(message);
	}
	
	@ServiceActivator(inputChannel = "snipeSwapChannel")
	public SnipeTransactionRequest snipeSwapChannel(SnipeTransactionRequest snipeTransactionRequest) throws Exception{
		
		boolean hasDataBeenTouched = false;
		Credentials credentials = snipeTransactionRequest.getCredentials();
		
		String dexWrapContractAddress = dexContractStaticCodeValuesService.getDexContractAddress(snipeTransactionRequest.getRoute(), TradeConstants.WNATIVE);
		String dexRouterContractAddress = dexContractStaticCodeValuesService.getDexContractAddress(snipeTransactionRequest.getRoute(), TradeConstants.ROUTER);
		String dexWrappedUsdContractAddress = dexContractStaticCodeValuesService.getDexContractAddress(snipeTransactionRequest.getRoute(), TradeConstants.WUSD);
		
		Address wnativeAddress = new Address(dexWrapContractAddress);
		Address toAddress = new Address(snipeTransactionRequest.getToAddress());
		BigInteger gasPrice = gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasPrice());
		BigInteger gasLimit = gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasLimit());
		
		final String hexRouterAddress = ZERO_X + TypeEncoder.encode(new Address(dexRouterContractAddress.substring(2)));
		
		List<Address> swapMemoryPath = Lists.newArrayList(wnativeAddress, toAddress);
		if(snipeTransactionRequest.isUSDPair()) { //new Address(TradeConstants.BUSD)
			swapMemoryPath = Lists.newArrayList(wnativeAddress, 
												new Address(dexWrappedUsdContractAddress), 
												toAddress);
		}
		// this is dangerous as your nonce may not in sync, please do pull off before any external execution
		String signedTransactionFinal = snipeTransactionRequest.getSignedTransaction();
		if(snipeTransactionRequest.getExpectedOutPutToken() != null 
				&& StringUtils.isBlank(signedTransactionFinal)) {
			 signedTransactionFinal = ethereumDexTradeService.fetchSignedTransaction(snipeTransactionRequest.getRoute(),
																					   credentials,
																					   snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(),
																					   snipeTransactionRequest.getExpectedOutPutToken(),
																					   snipeTransactionRequest.getDeadLine(),
																					   swapMemoryPath,
																					   Boolean.FALSE,
																					   gasPrice,
																					   gasLimit,
																					   snipeTransactionRequest.getGasMode());
		}
		boolean liquidityCheckEnabled = true;
	//	snipeTransactionRequest.setPairAddress("0x01Ac73c0B91289C21E12Ff44841A3C6b8aCDEA03");
		if(StringUtils.isBlank(snipeTransactionRequest.getPairAddress())) {
			String pairAddress = getPairAddress(snipeTransactionRequest, dexWrapContractAddress); 
			if(StringUtils.isNotBlank(pairAddress)) {
				snipeTransactionRequest.setPairAddress(pairAddress);
				System.err.println("Pair found");
			}else {
				Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
				return null;
			}
		}
		Web3j web3j = web3jServiceClientFactory.getWeb3jMap(snipeTransactionRequest.getRoute()).getWeb3j();
		Flowable<EthSendTransaction> ethSendTransaction  = web3j.ethSendRawTransaction(signedTransactionFinal)
				  .flowable().subscribeOn(Schedulers.trampoline());
//		//This is dangerous as we need to verify before hand a block number;
		BigInteger fromBlockNbr = web3j.ethBlockNumber()
				  .flowable()
				  .subscribeOn(Schedulers.io())
				  .blockingFirst()
				  .getBlockNumber().subtract(new BigInteger("8"));
		String hash = null;
		if(CollectionUtils.isNotEmpty(snipeTransactionRequest.getAdditionalProperties()) && StringUtils.isNotBlank(snipeTransactionRequest.getAdditionalProperties().get(0).getPropVal())){
			String functionName = snipeTransactionRequest.getAdditionalProperties().get(0).getPropVal();
			boolean isTradingEnabled = false;
			while(!isTradingEnabled) {
				isTradingEnabled = getIsTradingEnabledFunc(snipeTransactionRequest.getRoute(), functionName, snipeTransactionRequest.getToAddress());
				
				try {
					TradingEnabledEventChecker checker = TradingEnabledEventChecker.load(snipeTransactionRequest.getToAddress(), 
							web3jServiceClientFactory.getWeb3jMap(snipeTransactionRequest.getRoute()).getWeb3j(),
							credentials, gasPrice, gasLimit);
					
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				
				
				Uninterruptibles.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);
				System.err.println("Trading not enabled");
			}
			
			if(isTradingEnabled) {
				try {
					
					hash = ethSendTransaction.blockingSingle().getTransactionHash();
					if(StringUtils.isNotBlank(hash)) {
						
						String url = TradeConstants.SCAN_API_URL.get(snipeTransactionRequest.getRoute())+hash;
						System.out.println("URL"+ url);
						Runtime rt = Runtime.getRuntime();
					    rt.exec(RUNDLL32_URL_DLL_FILE_PROTOCOL_HANDLER + url);
						snipeTransactionRequest.setSwappedHash(hash);
						snipeTransactionRequest.setSnipeStatus(TradeConstants.FILLED);
						snipeTransactionRequest.setSnipe(true);
						snipeTransactionRequest.getAuditInformation().setUpdatedDateTime(Instant.now().toString());
						snipeTransactionRequest.setSignedTransaction(null);
						tradeOverviewRepository.save(mapRequestToTradeOverView(snipeTransactionRequest));
						purgeMessage(snipeTransactionRequest);
					}
				} catch (Exception e) {
					snipeTransactionRequest.setErrorMessage(e.getMessage());
					purgeMessage(snipeTransactionRequest);
				}
			}
			
		}else{
			
		boolean birthCheck = false;
		
		while (liquidityCheckEnabled) {
//			BigInteger fromBlockNbr1 = web3j.ethBlockNumber()
//					  .flowable()
//					  .subscribeOn(Schedulers.io())
//					  .blockingFirst()
//					  .getBlockNumber().subtract(new BigInteger("5"));
				
//				DefaultBlockParameter fromBlockNbr = null;
//				if(birthCheck) {
//					fromBlockNbr = DefaultBlockParameterName.EARLIEST;
//					birthCheck = false;
//					System.out.println("BirthCheck passed!!!");
//				}else {
//					fromBlockNbr = new DefaultBlockParameterNumber(blockNumber.subtract(new BigInteger("5")));
//				}
				//System.out.println("maunal BlockNbr -> "+ fromBlockNbr);
				//System.out.println("automatic BlockNbr -> "+ fromBlockNbr1);
				EthLog ethLog = liquidityEventFinder.hasLiquidityEventV2(snipeTransactionRequest.getRoute(), 
																		new DefaultBlockParameterNumber(fromBlockNbr), 
																	   DefaultBlockParameterName.LATEST,
																	   hexRouterAddress, 
																	   snipeTransactionRequest.getPairAddress());
				Boolean hasLogs = ethLog != null && ethLog.getError() == null && CollectionUtils.isNotEmpty(ethLog.getLogs());
				if(hasLogs) {
					liquidityCheckEnabled = Boolean.FALSE;
					try {
						
						hash = ethSendTransaction.blockingSingle().getTransactionHash();
						if(StringUtils.isNotBlank(hash)) {
							
							String url = TradeConstants.SCAN_API_URL.get(snipeTransactionRequest.getRoute())+hash;
							System.out.println("URL"+ url);
							Runtime rt = Runtime.getRuntime();
						    rt.exec(RUNDLL32_URL_DLL_FILE_PROTOCOL_HANDLER + url);
							snipeTransactionRequest.setSwappedHash(hash);
							snipeTransactionRequest.setSnipeStatus(TradeConstants.FILLED);
							snipeTransactionRequest.setSnipe(true);
							snipeTransactionRequest.getAuditInformation().setUpdatedDateTime(Instant.now().toString());
							snipeTransactionRequest.setSignedTransaction(null);
							tradeOverviewRepository.save(mapRequestToTradeOverView(snipeTransactionRequest));
							purgeMessage(snipeTransactionRequest);
						}
					} catch (Exception e) {
						snipeTransactionRequest.setErrorMessage(e.getMessage());
						purgeMessage(snipeTransactionRequest);
					}
				}else {
					Uninterruptibles.sleepUninterruptibly(2500, TimeUnit.MILLISECONDS);
					System.err.println("No Liquidity found");
					liquidityCheckEnabled = Boolean.TRUE;
					fromBlockNbr = fromBlockNbr.add(BigInteger.ONE);
				}
		}	}
		
		System.err.println(" ***Liquidity found ** ");
		
		if(StringUtils.isBlank(hash))  {
			BigInteger outputTokens = snipeTransactionRequest.getExpectedOutPutToken() == null 
														?  getAmountsIn(credentials, snipeTransactionRequest, Lists.newArrayList(toAddress, wnativeAddress), gasPrice, gasLimit) 
																: snipeTransactionRequest.getExpectedOutPutToken();
			if(outputTokens == null) {
				return null;
			}
			synchronizedBlock(snipeTransactionRequest, credentials, swapMemoryPath, gasPrice, gasLimit, outputTokens); 
		}
		return null;
	}

	private void  synchronizedBlock(SnipeTransactionRequest snipeTransactionRequest, Credentials credentials,
			List<Address> swapMemoryPath, BigInteger gasPrice, BigInteger gasLimit, BigInteger outputTokens)
			throws Exception {
		String hash = null;
		try {
				   hash = ethereumDexTradeService.swapETHForTokens(snipeTransactionRequest.getRoute(),
																   credentials,
																   snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(),
																   outputTokens,
																   snipeTransactionRequest.getDeadLine(),
																   swapMemoryPath,
																   snipeTransactionRequest.isFeeEligible(),
																   gasPrice,
																   gasLimit,
																   snipeTransactionRequest.getGasMode());	
			if (StringUtils.isNotBlank(hash)) {
				String url = TradeConstants.SCAN_API_URL.get(snipeTransactionRequest.getRoute())+hash;
				System.out.println( url);
				Runtime rt = Runtime.getRuntime();
			    rt.exec(RUNDLL32_URL_DLL_FILE_PROTOCOL_HANDLER + url);
				snipeTransactionRequest.setSwappedHash(hash);
				snipeTransactionRequest.setSnipeStatus(TradeConstants.FILLED);
				snipeTransactionRequest.setSnipe(true);
				snipeTransactionRequest.getAuditInformation().setUpdatedDateTime(Instant.now().toString());
				tradeOverviewRepository.save(mapRequestToTradeOverView(snipeTransactionRequest));
				purgeMessage(snipeTransactionRequest);
			}
		} catch (Exception e) {
			snipeTransactionRequest.setErrorMessage(e.getMessage());
			purgeMessage(snipeTransactionRequest);
		}
	}

	private BigInteger getAmountsIn(Credentials credentials, SnipeTransactionRequest snipeTransactionRequest, List<Address> amountsInMemoryPath, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
		BigInteger outputTokens = null;
		try {
			outputTokens = ethereumDexTradeService.getAmountsIn(snipeTransactionRequest.getRoute(),
																credentials,
															    snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(),
															    snipeTransactionRequest.getSlipageInDouble(),
															    amountsInMemoryPath,
															    gasPrice,
															    gasLimit,
															    snipeTransactionRequest.getGasMode(),
															    snipeTransactionRequest.getToAddressDecimals());
		} catch (Exception e) {
			System.err.println(" getAmountsIn error "+ e.getMessage());
			if (StringUtils.containsIgnoreCase(e.getMessage(), TradeConstants.INSUFFICIENT_LIQUIDITY) || StringUtils.containsIgnoreCase(e.getMessage(), TradeConstants.DS_MATH_SUB_UNDERFLOW)) {
				System.err.println(" getAmountsIn error insufficient liquidity "+ e.getMessage());
			} else {
				System.err.println("error in getAmountsIn purging order-> "+ e.getMessage());
				snipeTransactionRequest.setErrorMessage(e.getMessage());
				purgeMessage(snipeTransactionRequest);
				throw e;
			}
		}

		if (outputTokens != null && outputTokens.compareTo(BigInteger.ZERO) > 0) {
			return outputTokens;
		} else {
			Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
			snipeOrderReQueue.send(snipeTransactionRequest);
			return null;
		}
	}

	private Tuple3<BigInteger, BigInteger, BigInteger> getReserves(SnipeTransactionRequest snipeTransactionRequest, Credentials credentials) 	throws Exception {
		Tuple3<BigInteger, BigInteger, BigInteger> reserves = null;
		try {
			reserves = liquidityEventOrReserversFinder.fetchReserves(snipeTransactionRequest.getRoute(), 
															         snipeTransactionRequest.getPairAddress(), 
															         credentials,
															         snipeTransactionRequest.getGasPrice(), 
															         snipeTransactionRequest.getGasLimit(), 
															         snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(), 
															         snipeTransactionRequest.getGasMode());
		} catch (Exception e) {
			System.err.println(" getReserves error "+ e.getMessage());
		}
		
		if(reserves != null && preChecksForSnipe(snipeTransactionRequest, reserves)) {
			return reserves;
		}else {
			System.err.println(snipeTransactionRequest.getId()+ " Not Listed / No Reserves");
			Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
			snipeOrderReQueue.send(snipeTransactionRequest);
			return null;
		}
	}
	
	
	
	private String getPairAddress(SnipeTransactionRequest snipeTransactionRequest, String dexContractAddress) throws Exception {
		String pairAddress = getPairAddressFrmExchange(snipeTransactionRequest, dexContractAddress);
		if(StringUtils.isNotBlank(pairAddress)) {
			snipeTransactionRequest.setPairAddress(pairAddress);
			return pairAddress;
		}else {
			System.err.println(snipeTransactionRequest.getId() + " Not Listed / No Pair");
			Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
			snipeOrderReQueue.send(snipeTransactionRequest);
			return null;
		}
	}

	private String getPairAddressFrmExchange(SnipeTransactionRequest snipeTransactionRequest, String dexServiceContractAddress) throws Exception {
		Type pairAddress = null; 
		try {
			pairAddress = ethereumDexTradeService.getPairAddress(snipeTransactionRequest.getRoute(), 
																 snipeTransactionRequest.getToAddress(),  
																 dexServiceContractAddress)
					   							 .get(0);
			if(pairAddress != null && StringUtils.startsWithIgnoreCase((String)pairAddress.getValue(), _0X000000) && StringUtils.equalsIgnoreCase(snipeTransactionRequest.getRoute(), TradeConstants.PANCAKE)) {
				pairAddress = ethereumDexTradeService.getPairAddress(snipeTransactionRequest.getRoute(), 
																	 snipeTransactionRequest.getToAddress(), 
																	 TradeConstants.BUSD)
													 .get(0);
				snipeTransactionRequest.setUSDPair(true);
			}
		} catch (Exception e) {
			System.err.println(" getPairAddressFrmExchange error "+ e.getMessage());
			pairAddress  = ethereumDexTradeService.getPairAddress(snipeTransactionRequest.getRoute(), 
																  snipeTransactionRequest.getToAddress(), 
																  dexServiceContractAddress)
		             							  .parallelStream()
		             							  .findFirst()
		             							  .get();
		}
		if(pairAddress != null && !StringUtils.startsWithIgnoreCase((String)pairAddress.getValue(), _0X000000)) {
			return (String)pairAddress.getValue();
		}
		return null;
	}

	
		public Boolean getIsTradingEnabledFunc(String route, String functionName, String contractAddress) throws Exception{
			final Function function = new Function(
					functionName.trim(), 
	                Arrays.<Type>asList(), 
	                Collections.<TypeReference<?>>emptyList());
			EthCall ethCall = web3jServiceClientFactory.getWeb3jMap(route).getWeb3j()
											    .ethCall(Transaction.createEthCallTransaction(contractAddress, 
											    			contractAddress,
														 FunctionEncoder.encode(function)),
											    		DefaultBlockParameterName.LATEST)
											    .flowable()
											    .blockingSingle();
			if(ethCall.hasError()) {
				throw new Exception(ethCall.getError().getMessage());
			}
			try {
				List<Type> decode = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
				return (Boolean) decode.get(0).getValue();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return false;
		}


	private boolean preChecksForSnipe(SnipeTransactionRequest snipeTransactionRequest,
									  Tuple3<BigInteger, BigInteger, BigInteger> reserves) {
		boolean hasReserves = reserves != null  && reserves.component1().compareTo(BigInteger.ZERO) > 0
												&& reserves.component2().compareTo(BigInteger.ZERO) > 0;
		
		boolean hasLiquidityQuantity = snipeTransactionRequest.getLiquidityQuantity() != null;
		
		if(hasReserves && hasLiquidityQuantity) {
			if(reserves.component2().compareTo(snipeTransactionRequest.getLiquidityQuantity()) >= 0) {
				return true;
			}else {
				return false;
			}
		}
		return hasReserves;
	}
	
	private Reserves mapReserves(Tuple3<BigInteger, BigInteger, BigInteger> reserves) {
		Reserves reserves2 = new Reserves();
		reserves2.setReserve0(reserves.component1());
		reserves2.setReserve1(reserves.component2());
		return reserves2;
	}


	private void purgeMessage(SnipeTransactionRequest snipeTransactionRequest) throws Exception {
		snipeOrderHistoryRepository.save(snipeTransactionRequest);
		snipeOrderRepository.delete(snipeTransactionRequest);
	}

	private TradeOverview mapRequestToTradeOverView(SnipeTransactionRequest request) throws Exception {
		TradeOverview overview = new TradeOverview();
		overview.setApprovedHash(request.getApprovedHash());
		overview.setSwappedHash(request.getSwappedHash());
		overview.setErrorMessage(request.getErrorMessage());
		overview.setId(request.getId());
		overview.setOrderDesc(SNIPE);
		overview.setOrderSide(null);
		overview.setOrderState(StringUtils.isNotBlank(request.getErrorMessage())? FAILED: request.getSnipeStatus());
		overview.setOrderType(SNIPE);
		overview.setRoute(request.getRoute());
		try {
			overview.setExecutedPrice(dexNativePriceOracleClient.tokenPrice(request.getPairAddress(), request.getRoute(), request.getCredentials()));
		} catch (Exception e) {
			overview.setErrorMessage(request.getErrorMessage()+" your fucking code sucks!!!");
		}
		return overview;
	}
	
}
