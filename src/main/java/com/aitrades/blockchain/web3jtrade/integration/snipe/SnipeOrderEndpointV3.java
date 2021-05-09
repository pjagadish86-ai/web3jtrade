package com.aitrades.blockchain.web3jtrade.integration.snipe;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;

import com.aitrades.blockchain.web3jtrade.client.DexNativePriceOracleClient;
import com.aitrades.blockchain.web3jtrade.dex.contract.DexContractServiceV3;
import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
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

import io.reactivex.schedulers.Schedulers;

@SuppressWarnings({"unused"})
@Service("snipeOrderEndpointV3")
public class SnipeOrderEndpointV3{
	
	private static final String RUNDLL32_URL_DLL_FILE_PROTOCOL_HANDLER = "rundll32 url.dll,FileProtocolHandler ";
	private static final String ETHERSCAN = "https://etherscan.io/tx/";
	private static final String BSC_SCAN = "https://bscscan.com/tx/";
	private static final String FAILED = "FAILED";
	private static final String SNIPE = "SNIPE";
	private static final String _0X000000 = "0x000000";

	@Autowired
	private Web3jServiceClientFactory web3jServiceClientFactory;

	@Resource(name="orderSubmitRabbitTemplate")
	private AmqpTemplate orderSubmitRabbitTemplate;
	
	@Autowired
	private GasProvider gasProvider;
	
	@Resource(name="snipeTransactionRequestObjectReader")
	private ObjectReader snipeTransactionRequestObjectReader;
	
	@Autowired
	private DexContractServiceV3 dexContractServiceV3;
	
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
	private DexContractStaticCodeValuesService dexContractStaticCodeValuesService;
	
	@Autowired
	private LiquidityEventFinder liquidityEventFinder;
	
	private static final String ZERO_X = "0x";
	private static final String MINT = "Mint";
	
	@Transformer(inputChannel = "snipeV3Transform", outputChannel = "snipeV3SwapChannel")
	public SnipeTransactionRequest snipeV3Transform(byte[] message) throws Exception{
		return snipeTransactionRequestObjectReader.readValue(message);
	}
	
	@ServiceActivator(inputChannel = "snipeV3SwapChannel")
	public SnipeTransactionRequest snipeV3SwapChannel(SnipeTransactionRequest snipeTransactionRequest) throws Exception{
		
		Credentials credentials = snipeTransactionRequest.getCredentials();
		
		String dexWrapContractAddress = dexContractStaticCodeValuesService.getDexContractAddress(snipeTransactionRequest.getRoute(), TradeConstants.WNATIVE);
		String dexRouterContractAddress = dexContractStaticCodeValuesService.getDexContractAddress(snipeTransactionRequest.getRoute(), TradeConstants.ROUTER);
		String dexFactoryContractAddress = dexContractStaticCodeValuesService.getDexContractAddress(snipeTransactionRequest.getRoute(), TradeConstants.FACTORY);

		Address wnativeAddress = new Address(dexWrapContractAddress);
		Address toAddress = new Address(snipeTransactionRequest.getToAddress());
		List<Address> swapMemoryPath = Lists.newArrayList(wnativeAddress, toAddress);
		
		BigInteger gasPrice = gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasPrice());
		BigInteger gasLimit = gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasLimit());
		
		final String hexRouterAddress = ZERO_X + TypeEncoder.encode(new Address(dexRouterContractAddress.substring(2)));
		final String hexToAddress = ZERO_X + TypeEncoder.encode(new Address(snipeTransactionRequest.getToAddress().substring(2)));
		final String hexWnativeAddress = ZERO_X + TypeEncoder.encode(new Address(dexWrapContractAddress.substring(2)));

		
		Map<String, Object> requestMap = new ConcurrentHashMap<>();
		requestMap.put(TradeConstants.WNATIVE, dexWrapContractAddress);
		requestMap.put(TradeConstants.TOADDRESS, snipeTransactionRequest.getToAddress());
		requestMap.put(TradeConstants.AMOUNTS_IN, snipeTransactionRequest.getInputTokenValueAmountAsBigInteger());
		requestMap.put(TradeConstants.AMOUNTS_OUT, snipeTransactionRequest.getExpectedOutPutToken());
		requestMap.put(TradeConstants.ROUTER, dexRouterContractAddress);
		requestMap.put(TradeConstants.GAS_PRICE, gasPrice);
		requestMap.put(TradeConstants.GAS_LIMIT, gasLimit);
		requestMap.put(TradeConstants.SNIPE, snipeTransactionRequest);
		
		String signedTransaction = dexContractServiceV3.fetchSignedTransaction(snipeTransactionRequest.getRoute(), requestMap, credentials);
		
		boolean hasLiquidity = false;
		while (!hasLiquidity) {
				Web3j web3j = web3jServiceClientFactory.getWeb3jMap(snipeTransactionRequest.getRoute()).getWeb3j();
//				BigInteger blockNumber = web3j.ethBlockNumber()
//											.flowable()
//											.subscribeOn(Schedulers.io())
//											.blockingLast()
//											.getBlockNumber().subtract(BigInteger.valueOf(400l));
			
				BigInteger blockNumber = BigInteger.valueOf(12381212);
				BigInteger toblockNumber = BigInteger.valueOf(12381215);
				System.out.println("from blck nbr-> "+ blockNumber);
				//String route, DefaultBlockParameter fromBlockNbr, DefaultBlockParameter toBlockNbr, String factoryAddress, String toAddress, String wnativeAddress
				EthLog ethLog = liquidityEventFinder.hasLiquidityEventV3(snipeTransactionRequest.getRoute(), 
																	   new DefaultBlockParameterNumber(blockNumber), 
																	   new DefaultBlockParameterNumber(toblockNumber),
																	   dexFactoryContractAddress, 
																	   hexToAddress, 
																	   hexWnativeAddress);
				hasLiquidity = ethLog != null && ethLog.getError() == null && CollectionUtils.isNotEmpty(ethLog.getLogs());
				if(ethLog != null && ethLog.getError() == null && CollectionUtils.isNotEmpty(ethLog.getLogs())) {
					hasLiquidity = Boolean.TRUE;
					for(LogResult log : ethLog.getResult()) {
						Log x = (Log)log.get();
						System.out.println("NBR # "+ x.getBlockNumber() + "  IND # "+ x.getTransactionIndex() + "         TRANSCTION HASH # " +x.getTransactionHash());
					}
				}else {
					System.err.println("No Liquidity found");
					Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
				}
		}
		
		System.err.println(" ***Liquidity found ** ");
		
		
		try {
			EthSendTransaction ethSendTransaction = web3jServiceClientFactory.getWeb3jMap(snipeTransactionRequest.getRoute()).getWeb3j()
																		     .ethSendRawTransaction(signedTransaction)
																			 .flowable()
																			 .subscribeOn(Schedulers.io())
																			 .blockingSingle();
			if(ethSendTransaction.hasError()) {
				System.err.println(ethSendTransaction.getError().getMessage());
				throw new Exception(ethSendTransaction.getError().getMessage());
			}
			
			if(StringUtils.isNotBlank(ethSendTransaction.getTransactionHash())) {
				String url = StringUtils.equalsIgnoreCase(snipeTransactionRequest.getRoute(), TradeConstants.PANCAKE) ? BSC_SCAN + ethSendTransaction.getTransactionHash() : ETHERSCAN+ethSendTransaction.getTransactionHash();
				System.out.println("URL"+ url);
				Runtime rt = Runtime.getRuntime();
			    rt.exec(RUNDLL32_URL_DLL_FILE_PROTOCOL_HANDLER + url);
				snipeTransactionRequest.setSwappedHash(ethSendTransaction.getTransactionHash());
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
		return null;
	}

	private void purgeMessage(SnipeTransactionRequest snipeTransactionRequest) throws Exception {
		//snipeOrderHistoryRepository.save(snipeTransactionRequest);
		//snipeOrderRepository.delete(snipeTransactionRequest);
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
