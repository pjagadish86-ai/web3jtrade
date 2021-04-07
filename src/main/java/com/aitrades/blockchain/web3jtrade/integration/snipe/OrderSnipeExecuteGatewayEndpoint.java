
package com.aitrades.blockchain.web3jtrade.integration.snipe;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.util.CollectionUtils;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;

import com.aitrades.blockchain.web3jtrade.client.DexSubGraphPriceServiceClient;
import com.aitrades.blockchain.web3jtrade.dex.contract.DexTradeContractService;
import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.aitrades.blockchain.web3jtrade.domain.TradeOverview;
import com.aitrades.blockchain.web3jtrade.integration.snipe.mq.SnipeOrderReQueue;
import com.aitrades.blockchain.web3jtrade.oracle.gas.GasProvider;
import com.aitrades.blockchain.web3jtrade.repository.SnipeOrderHistoryRepository;
import com.aitrades.blockchain.web3jtrade.repository.SnipeOrderRepository;
import com.aitrades.blockchain.web3jtrade.repository.TradeOverviewRepository;
import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;
import com.aitrades.blockchain.web3jtrade.trade.pendingTransaction.EthereumGethPendingTransactionsRetriever;
import com.aitrades.blockchain.web3jtrade.trade.pendingTransaction.EthereumParityPendingTransactionsRetriever;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
@SuppressWarnings({"unused", "rawtypes"})
public class OrderSnipeExecuteGatewayEndpoint{
	
	private static final String FAILED = "FAILED";
	private static final String SNIPE = "SNIPE";
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
	private TradeOverviewRepository tradeOverviewRepository;
	
	@Autowired
	private SnipeOrderReQueue snipeOrderReQueue;
	
	@Autowired
	private LiquidityEventOrReserversFinder liquidityEventOrReserversFinder;

	@Transformer(inputChannel = "rabbitMqSubmitOrderConsumer", outputChannel = "pairCreatedEventChannel")
	public Map<String, Object> rabbitMqSubmitOrderConsumer(byte[] message) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest  = snipeTransactionRequestObjectReader.readValue(message);
		Map<String, Object> aitradesMap = new ConcurrentHashMap<>();
		aitradesMap.put(TradeConstants.SNIPETRANSACTIONREQUEST, snipeTransactionRequest);
		aitradesMap.put(TradeConstants.CREDENTIALS, snipeTransactionRequest.getCredentials());
		return aitradesMap;
	}
	
	@ServiceActivator(inputChannel = "pairCreatedEventChannel", outputChannel = "liquidityEventOrReservesFinderChannel")
	public Map<String, Object> pairCreatedEventChannel(Map<String, Object> tradeOrderMap) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest)tradeOrderMap.get(TradeConstants.SNIPETRANSACTIONREQUEST);
		Optional<Type> pairAddress  = ethereumDexTradeService.getPairAddress(snipeTransactionRequest.getRoute(), snipeTransactionRequest.getToAddress(), TradeConstants.WETH_MAP.get(snipeTransactionRequest.getRoute()))
												             .parallelStream()
												             .findFirst();
		if(pairAddress.isPresent() && !StringUtils.startsWithIgnoreCase((String)pairAddress.get().getValue(), _0X000000)) {
			tradeOrderMap.put(TradeConstants.PAIR_CREATED, Boolean.TRUE);
			snipeTransactionRequest.setPairAddress((String)pairAddress.get().getValue());
			return tradeOrderMap;
		}else {
			snipeOrderReQueue.send(snipeTransactionRequest);
		}
		return null;
	}
	
	@ServiceActivator(inputChannel = "liquidityEventOrReservesFinderChannel", outputChannel = "amountsInChannel")
	public Map<String, Object> liquidityEventOrReservesFinderChannel(Map<String, Object> tradeOrderMap) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest)tradeOrderMap.get(TradeConstants.SNIPETRANSACTIONREQUEST);
		boolean hasLiquidityOrReserves  = liquidityEventOrReserversFinder.hasReservesMetWithInputAmount(snipeTransactionRequest.getRoute(), 
																								        snipeTransactionRequest.getPairAddress(), 
																								        (Credentials)tradeOrderMap.get(TradeConstants.CREDENTIALS),
																								        snipeTransactionRequest.getGasPrice(), 
																								        snipeTransactionRequest.getGasLimit(), 
																								        snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(), snipeTransactionRequest.getGasMode());
		if(hasLiquidityOrReserves) {
			tradeOrderMap.put(TradeConstants.HAS_LIQUIDTY_EVENT_OR_HAS_RESERVES, hasLiquidityOrReserves);
			return tradeOrderMap;
		}else {
			snipeOrderReQueue.send(snipeTransactionRequest);
		}
		return null;
	}
	
	@ServiceActivator(inputChannel = "amountsInChannel", outputChannel = "swapETHForTokensChannel")
	public Map<String, Object> amountsInChannel(Map<String, Object> tradeOrderMap) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest) tradeOrderMap .get(TradeConstants.SNIPETRANSACTIONREQUEST);
		try {
			if (tradeOrderMap.get(TradeConstants.HAS_LIQUIDTY_EVENT_OR_HAS_RESERVES) != null) {
				BigInteger outputTokens = ethereumDexTradeService.getAmountsIn(snipeTransactionRequest.getRoute(),
																			   snipeTransactionRequest.getCredentials(),
																			   snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(),
																			   snipeTransactionRequest.getSlipageInDouble(),
																			   Lists.newArrayList(TradeConstants.WETH_MAP.get(snipeTransactionRequest.getRoute().toUpperCase()),
																															   snipeTransactionRequest.getToAddress()),
																			   gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasPrice()),
																			   gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasLimit()),
																			   snipeTransactionRequest.getGasMode());
				
				if (outputTokens != null && outputTokens.compareTo(BigInteger.ZERO) > 0) {
					snipeTransactionRequest.setOuputTokenValueAmounttAsBigInteger(outputTokens);
					return tradeOrderMap;
				}
			}else {
				snipeOrderReQueue.send(snipeTransactionRequest);
			}
		} catch (Exception e) {
			if (StringUtils.containsIgnoreCase(e.getMessage(), TradeConstants.INSUFFICIENT_LIQUIDITY) 
					|| StringUtils.containsIgnoreCase(e.getMessage(), TradeConstants.DS_MATH_SUB_UNDERFLOW)) {
				snipeOrderReQueue.send(snipeTransactionRequest);
			} else {
				snipeTransactionRequest.setErrorMessage(e.getMessage());
				purgeMessage(snipeTransactionRequest);
			}
		} 
		return null;
	}
	
	@ServiceActivator(inputChannel = "swapETHForTokensChannel")
	public Map<String, Object> swapETHForTokensChannel(Map<String, Object> tradeOrderMap) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest) tradeOrderMap .get(TradeConstants.SNIPETRANSACTIONREQUEST);
		try {
			if (snipeTransactionRequest.getOuputTokenValueAmounttAsBigInteger() != null && snipeTransactionRequest.getOuputTokenValueAmounttAsBigInteger().compareTo(BigInteger.ZERO) > 0) {
				String hash = ethereumDexTradeService.swapETHForTokens(snipeTransactionRequest.getRoute(),
																		snipeTransactionRequest.getCredentials(),
																		snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(),
																		snipeTransactionRequest.getOuputTokenValueAmounttAsBigInteger(),
																		snipeTransactionRequest.getDeadLine(),
																		Lists.newArrayList(TradeConstants.WETH_MAP.get(snipeTransactionRequest.getRoute().toUpperCase()),
																						   snipeTransactionRequest.getToAddress()),
																		snipeTransactionRequest.isFeeEligible(),
																		gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()),
																		snipeTransactionRequest.getGasPrice()),
																		gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()),
																		snipeTransactionRequest.getGasLimit()),
																		snipeTransactionRequest.getGasMode());
				if (StringUtils.isNotBlank(hash)) {
					tradeOrderMap.put(TradeConstants.SWAP_ETH_FOR_TOKEN_HASH, true);
					snipeTransactionRequest.setSwappedHash(hash);
					snipeTransactionRequest.setSnipeStatus(TradeConstants.FILLED);
					snipeTransactionRequest.setSnipe(true);
					return tradeOrderMap;
				}
			}
		} catch (Exception e) {
			snipeTransactionRequest.setErrorMessage(e.getMessage());
			purgeMessage(snipeTransactionRequest);
		} 
	
		return null;
	}
	
	@ServiceActivator(inputChannel = "updateOrDeleteSnipeOrderChannel")
	public Map<String, Object> updateOrDeleteSnipeOrderChannel(Map<String, Object> tradeOrderMap) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest) tradeOrderMap.get(TradeConstants.SNIPETRANSACTIONREQUEST);
		if (snipeTransactionRequest.hasSniped()	&& tradeOrderMap.get(TradeConstants.SWAP_ETH_FOR_TOKEN_HASH) != null) {
			purgeMessage(snipeTransactionRequest);
		} else {
			snipeOrderReQueue.send(snipeTransactionRequest);
		}
		return null;
	}

	private void purgeMessage(SnipeTransactionRequest snipeTransactionRequest) throws Exception {
		tradeOverviewRepository.save(mapRequestToTradeOverView(snipeTransactionRequest));
		snipeOrderHistoryRepository.save(snipeTransactionRequest);
		snipeOrderRepository.delete(snipeTransactionRequest);
	}

	private TradeOverview mapRequestToTradeOverView(SnipeTransactionRequest request) {
		TradeOverview overview = new TradeOverview();
		overview.setApprovedHash(request.getApprovedHash());
		overview.setSwappedHash(request.getSwappedHash());
		overview.setErrorMessage(request.getErrorMessage());
		overview.setId(request.getId());
		overview.setOrderDesc(SNIPE);
		overview.setOrderSide(null);
		overview.setOrderState(StringUtils.isNotBlank(request.getErrorMessage())? FAILED: request.getSnipeStatus());
		overview.setOrderType(SNIPE);
		return overview;
	}
	
}
