package com.aitrades.blockchain.web3jtrade.integration.snipe;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.tuples.generated.Tuple3;

import com.aitrades.blockchain.web3jtrade.client.DexSubGraphPriceServiceClient;
import com.aitrades.blockchain.web3jtrade.dex.contract.DexTradeContractService;
import com.aitrades.blockchain.web3jtrade.dex.contract.event.EthereumDexEventHandler;
import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.aitrades.blockchain.web3jtrade.integration.snipe.mq.SnipeOrderReSender;
import com.aitrades.blockchain.web3jtrade.oracle.gas.GasProvider;
import com.aitrades.blockchain.web3jtrade.oracle.gas.StrategyGasProvider;
import com.aitrades.blockchain.web3jtrade.repository.SnipeOrderHistoryRepository;
import com.aitrades.blockchain.web3jtrade.repository.SnipeOrderRepository;
import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;
import com.aitrades.blockchain.web3jtrade.trade.pendingTransaction.EthereumGethPendingTransactionsRetriever;
import com.aitrades.blockchain.web3jtrade.trade.pendingTransaction.EthereumParityPendingTransactionsRetriever;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Lists;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
@SuppressWarnings({"unused", "rawtypes"})
public class OrderSnipeExecuteGatewayEndpoint{
	
	private static final String CUSTOM = "CUSTOM";
	private static final String _0X000000 = "0x000000";

	@Autowired
	private Web3jServiceClientFactory web3jServiceClientFactory;

	@Autowired
	private EthereumGethPendingTransactionsRetriever gethPendingTrxsRetriever;
	
	@Autowired
	private EthereumParityPendingTransactionsRetriever parityPendingTrxsRetriever;
	
	@Resource(name="orderSubmitRabbitTemplate")
	private AmqpTemplate orderSubmitRabbitTemplate;
	
	@Autowired
	private GasProvider gasProvider;
	
	@Autowired
	private DexSubGraphPriceServiceClient subGraphPriceClient;
	
	@Resource(name="snipeTransactionRequestObjectReader")
	private ObjectReader snipeTransactionRequestObjectReader;
	
	@Autowired
	private DexTradeContractService ethereumDexTradeService;
	
	@Autowired
	private SnipeOrderRepository snipeOrderRepository;
	
	@Autowired
	private SnipeOrderHistoryRepository snipeOrderHistoryRepository;
	
	@Autowired
	private SnipeOrderReSender snipeOrderReSender;

	@Transformer(inputChannel = "rabbitMqSubmitOrderConsumer", outputChannel = "pairCreatedEventChannel")
	public Map<String, Object> rabbitMqSubmitOrderConsumer(byte[] message) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest  = snipeTransactionRequestObjectReader.readValue(message);
		Map<String, Object> aitradesMap = new ConcurrentHashMap<String, Object>();
		aitradesMap.put(TradeConstants.SNIPETRANSACTIONREQUEST, snipeTransactionRequest);
		return aitradesMap;
	}
	
	
	@ServiceActivator(inputChannel = "pairCreatedEventChannel", outputChannel = "getReservesEventChannel")
	public Map<String, Object> pairCreatedEventChannel(Map<String, Object> tradeOrderMap) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest)tradeOrderMap.get(TradeConstants.SNIPETRANSACTIONREQUEST);
		Optional<Type> pairAddress  = ethereumDexTradeService.getPairAddress(snipeTransactionRequest.getRoute(), snipeTransactionRequest.getToAddress(), TradeConstants.WETH_MAP.get(snipeTransactionRequest.getRoute()))
												             .parallelStream()
												             .findFirst();
		if(pairAddress.isPresent() && !StringUtils.startsWithIgnoreCase((String)pairAddress.get().getValue(), _0X000000)) {
			tradeOrderMap.put(TradeConstants.PAIR_CREATED, Boolean.TRUE);
			snipeTransactionRequest.setPairAddress((String)pairAddress.get().getValue());
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "getReservesEventChannel", outputChannel = "addLiquidityEvent")
	public Map<String, Object> getReservesEventChannel(Map<String, Object> tradeOrderMap) throws Exception{
		if(tradeOrderMap.get(TradeConstants.PAIR_CREATED) != null) {
			SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest)tradeOrderMap.get(TradeConstants.SNIPETRANSACTIONREQUEST);
			try {
				if(tradeOrderMap.get(TradeConstants.HAS_RESERVES) == null) {
					Tuple3<BigInteger, BigInteger, BigInteger> reservers = ethereumDexTradeService.getReservesOfPair(snipeTransactionRequest.getRoute(), 
																											         snipeTransactionRequest.getPairAddress(), 
																											         snipeTransactionRequest.getCredentials(), 
																											         snipeTransactionRequest.getGasPrice(), 
																											         snipeTransactionRequest.getGasLimit(),
																											         snipeTransactionRequest.getGasMode());
					if (reservers != null 
							&& reservers.component1().compareTo(BigInteger.ZERO) > 0
							&& reservers.component2().compareTo(BigInteger.ZERO) > 0) {
							tradeOrderMap.put(TradeConstants.HAS_RESERVES, Boolean.TRUE);
					}
				}
			} catch (Exception e) {
			}
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "addLiquidityEvent", outputChannel = "amountsInChannel")
	public Map<String, Object> addLiquidityEvent(Map<String, Object> tradeOrderMap) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest)tradeOrderMap.get(TradeConstants.SNIPETRANSACTIONREQUEST);
		if(tradeOrderMap.get(TradeConstants.HAS_RESERVES) == null) {
			try {
				Flowable<EthLog> flowable = EthereumDexEventHandler.mintEventFlowables(web3jServiceClientFactory.getWeb3jMap().get(snipeTransactionRequest.getRoute()).getWeb3j(), 
																					   snipeTransactionRequest.getPairAddress(), 
																					   TradeConstants.ROUTER_MAP.get(snipeTransactionRequest.getRoute()));
				flowable.subscribeOn(Schedulers.computation())
						.blockingSubscribe(resp -> {
					if(resp != null) {
						 tradeOrderMap.put(TradeConstants.HAS_LIQUIDTY_EVENT, Boolean.TRUE);
						 return;
					}
				});
			} catch (Exception e) {
			}
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "amountsInChannel", outputChannel = "swapETHForTokensChannel")
	public Map<String, Object> amountsInChannel(Map<String, Object> tradeOrderMap) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest) tradeOrderMap.get(TradeConstants.SNIPETRANSACTIONREQUEST);
		try {
			if(tradeOrderMap.get(TradeConstants.HAS_LIQUIDTY_EVENT) != null && tradeOrderMap.get(TradeConstants.HAS_RESERVES) != null ) {
				BigInteger outputTokens = ethereumDexTradeService.getAmountsIn(snipeTransactionRequest.getRoute(),
																		       snipeTransactionRequest.getCredentials(), 
																		       snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(),
																		       snipeTransactionRequest.getSlipageInDouble(),
																		       Lists.newArrayList(TradeConstants.WETH_MAP.get(snipeTransactionRequest.getRoute().toUpperCase()), snipeTransactionRequest.getToAddress()),
																		       gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasPrice()),
																		       gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasLimit()),
																		       snipeTransactionRequest.getGasMode());
				if(outputTokens != null && outputTokens.compareTo(BigInteger.ZERO) > 0 ) {
					snipeTransactionRequest.setOuputTokenValueAmounttAsBigInteger(outputTokens);
				}
			}
		} catch (Exception e) {
			if(StringUtils.containsIgnoreCase(e.getMessage(), "INSUFFICIENT_LIQUIDITY")) {
				if(StringUtils.isBlank(snipeTransactionRequest.getCreatedDateTime())) {
					snipeTransactionRequest.setCreatedDateTime(LocalDateTime.now().toString());
				}
				snipeOrderReSender.send(snipeTransactionRequest);
			}else {
				snipeTransactionRequest.setErrorMessage(e.getMessage());
				purgeMessage(snipeTransactionRequest);
			}
		} 
		
		return tradeOrderMap;	
	}
	
	@ServiceActivator(inputChannel = "swapETHForTokensChannel")
	public Map<String, Object> swapETHForTokensChannel(Map<String, Object> tradeOrderMap) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest) tradeOrderMap.get(TradeConstants.SNIPETRANSACTIONREQUEST);
		try {
			if(snipeTransactionRequest.getOuputTokenValueAmounttAsBigInteger() != null && snipeTransactionRequest.getOuputTokenValueAmounttAsBigInteger().compareTo(BigInteger.ZERO) > 0) {
				String hash = ethereumDexTradeService.swapETHForTokens(snipeTransactionRequest.getRoute(),
																	   snipeTransactionRequest.getCredentials(), 
																	   snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(),
																	   snipeTransactionRequest.getOuputTokenValueAmounttAsBigInteger(), 
																	   snipeTransactionRequest.getDeadLine(),
																	   Lists.newArrayList(TradeConstants.WETH_MAP.get(snipeTransactionRequest.getRoute().toUpperCase()), 
																			   			  snipeTransactionRequest.getToAddress()), 
																	   snipeTransactionRequest.isFeeEligible(), 
																	   gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasPrice()),
																       gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasLimit()),
																       snipeTransactionRequest.getGasMode());
				if(StringUtils.isNotBlank(hash)) {
					tradeOrderMap.put(TradeConstants.SWAP_ETH_FOR_TOKEN_HASH, true);
					snipeTransactionRequest.setSwappedHash(hash);
					snipeTransactionRequest.setSnipeStatus(TradeConstants.FILLED);
					snipeTransactionRequest.setSnipe(true);
				}
			}
		} catch (Exception e) {
			snipeTransactionRequest.setErrorMessage(e.getMessage());
			purgeMessage(snipeTransactionRequest);
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "updateOrDeleteSnipeOrderChannel")
	public Map<String, Object> updateOrDeleteSnipeOrderChannel(Map<String, Object> tradeOrderMap) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest) tradeOrderMap.get(TradeConstants.SNIPETRANSACTIONREQUEST);
		if(snipeTransactionRequest.hasSniped() && tradeOrderMap.get(TradeConstants.SWAP_ETH_FOR_TOKEN_HASH) != null) {
			purgeMessage(snipeTransactionRequest);
		}else {
			snipeOrderReSender.send(snipeTransactionRequest);
		}
		return tradeOrderMap;
	}


	private void purgeMessage(SnipeTransactionRequest snipeTransactionRequest) throws Exception {
		snipeOrderHistoryRepository.save(snipeTransactionRequest);
		snipeOrderRepository.delete(snipeTransactionRequest);
	}
	
}
