package com.aitrades.blockchain.web3jtrade.integration.snipe;

import java.math.BigDecimal;
import java.math.BigInteger;
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
import com.aitrades.blockchain.web3jtrade.domain.price.PairPrice;
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
//TODO:	// May be we should find a way to send null channel if there no valid movement.
public class OrderSnipeExecuteGatewayEndpoint{
	
	@Autowired
	private Web3jServiceClientFactory web3jServiceClientFactory;
	// TODO: strict support only one front run either parity or geth. future thought
	
	@Autowired
	private EthereumGethPendingTransactionsRetriever gethPendingTrxsRetriever;
	
	@Autowired
	private EthereumParityPendingTransactionsRetriever parityPendingTrxsRetriever;
	
	@Resource(name="orderSubmitRabbitTemplate")
	private AmqpTemplate orderSubmitRabbitTemplate;
	
	@Autowired
	public StrategyGasProvider strategyGasProvider;
	
	@Autowired
	private DexSubGraphPriceServiceClient subGraphPriceClient;
	
	@Resource(name="snipeTransactionRequestObjectReader")
	private ObjectReader snipeTransactionRequestObjectReader;
	
	@Autowired
	private DexTradeContractService ethereumDexTradeService;
	
	@Autowired
	private SnipeOrderRepository snipeOrderRepository;
	
	@Autowired
	public SnipeOrderHistoryRepository snipeOrderHistoryRepository;

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
		if(pairAddress.isPresent() && StringUtils.startsWithIgnoreCase((String)pairAddress.get().getValue(), "0x000000")) {
			pairAddress  = ethereumDexTradeService.getPairAddress(snipeTransactionRequest.getRoute(), snipeTransactionRequest.getToAddress(), TradeConstants.ETH)
									             .parallelStream()
									             .findFirst();
		}
		if(pairAddress.isPresent() && !StringUtils.startsWithIgnoreCase((String)pairAddress.get().getValue(), "0x000000")) {
			tradeOrderMap.put(TradeConstants.PAIR_CREATED, Boolean.TRUE);
			snipeTransactionRequest.setPairAddress((String)pairAddress.get().getValue());
		}
		return tradeOrderMap;
	}
	
	//TODO: combine pair data, reserves and addliquidty single call
	@ServiceActivator(inputChannel = "getReservesEventChannel", outputChannel = "addLiquidityEvent")
	public Map<String, Object> getReservesEventChannel(Map<String, Object> tradeOrderMap) throws Exception{
		
		if(tradeOrderMap.get(TradeConstants.PAIR_CREATED) != null) {
			
			SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest)tradeOrderMap.get(TradeConstants.SNIPETRANSACTIONREQUEST);
//			Flowable<PairPrice> pairData = subGraphPriceClient.getPairData(snipeTransactionRequest.getPairAddress());
//				
//				pairData.subscribeOn(Schedulers.io())
//						.blockingSubscribe(resp -> {
//				if(resp != null && resp.getData() != null && resp.getData().getPair() != null && resp.getData().getPair().getReserve0AsBigDecimal().compareTo(BigDecimal.ZERO) > 0 && resp.getData().getPair().getReserve1AsBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
//					 tradeOrderMap.put(TradeConstants.HAS_RESERVES, Boolean.TRUE);
//					 return;
//				}
//			});
			
			if(tradeOrderMap.get(TradeConstants.HAS_RESERVES) == null) {
				Tuple3<BigInteger, BigInteger, BigInteger> reservers = ethereumDexTradeService.getReservesOfPair(snipeTransactionRequest.getRoute(), snipeTransactionRequest.getPairAddress(), snipeTransactionRequest.getCredentials(), snipeTransactionRequest.getGasPrice(), snipeTransactionRequest.getGasLimit());
				if (reservers != null) {
					if (reservers.component1().compareTo(BigInteger.ZERO) > 0
							&& reservers.component2().compareTo(BigInteger.ZERO) > 0) {
						tradeOrderMap.put(TradeConstants.HAS_RESERVES, Boolean.TRUE);
					} 
				}
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
			if(tradeOrderMap.get(TradeConstants.HAS_LIQUIDTY_EVENT) != null || tradeOrderMap.get(TradeConstants.HAS_RESERVES) != null ) {
				BigInteger gasPrice = null;
				BigInteger gasLimit = null;
				if(snipeTransactionRequest.getGasMode().equalsIgnoreCase("CUSTOM")) {
					gasPrice = snipeTransactionRequest.getGasPrice();
					gasLimit = snipeTransactionRequest.getGasLimit();
				}else {
					gasPrice = strategyGasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()));
					gasLimit = strategyGasProvider.getGasLimit(snipeTransactionRequest.getRoute());
				}
				
				BigInteger outputTokens = ethereumDexTradeService.getAmountsIn(snipeTransactionRequest.getRoute(),
																		       snipeTransactionRequest.getCredentials(), 
																		       snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(),
																		       snipeTransactionRequest.getSlipageInDouble(),
																		       Lists.newArrayList(TradeConstants.WETH_MAP.get(snipeTransactionRequest.getRoute().toUpperCase()), snipeTransactionRequest.getToAddress()),
																		       gasPrice,
																		       gasLimit);
				if(outputTokens != null && outputTokens.compareTo(BigInteger.ZERO) > 0 ) {
					snipeTransactionRequest.setOuputTokenValueAmounttAsBigInteger(outputTokens);
				}
			}
		} catch (Exception e) {
			purgeMessage(snipeTransactionRequest);
			e.printStackTrace();
		}
		return tradeOrderMap;	
	}
	
	@ServiceActivator(inputChannel = "swapETHForTokensChannel")
	public Map<String, Object> swapETHForTokensChannel(Map<String, Object> tradeOrderMap) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest) tradeOrderMap.get(TradeConstants.SNIPETRANSACTIONREQUEST);
		try {
			if(snipeTransactionRequest.getOuputTokenValueAmounttAsBigInteger() != null && snipeTransactionRequest.getOuputTokenValueAmounttAsBigInteger().compareTo(BigInteger.ZERO) > 0) {
				BigInteger gasPrice = null;
				BigInteger gasLimit = null;
				if(snipeTransactionRequest.getGasMode().equalsIgnoreCase("CUSTOM")) {
					gasPrice = snipeTransactionRequest.getGasPrice();
					gasLimit = snipeTransactionRequest.getGasLimit();
				}else {
					gasPrice = strategyGasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()));
					gasLimit = strategyGasProvider.getGasLimit(snipeTransactionRequest.getRoute());
				}
				String hash = ethereumDexTradeService.swapETHForTokens(snipeTransactionRequest.getRoute(),
																	   snipeTransactionRequest.getCredentials(), 
																	   snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(),
																	   snipeTransactionRequest.getOuputTokenValueAmounttAsBigInteger(), 
																	   snipeTransactionRequest.getDeadLine(),
																	   Lists.newArrayList(TradeConstants.WETH_MAP.get(snipeTransactionRequest.getRoute().toUpperCase()), 
																			   			  snipeTransactionRequest.getToAddress()), 
																	   false, 
																	   gasPrice, 
																	   gasLimit);
				if(StringUtils.isNotBlank(hash)) {
					tradeOrderMap.put(TradeConstants.SWAP_ETH_FOR_TOKEN_HASH, true);
					snipeTransactionRequest.setSwappedHash(hash);
					snipeTransactionRequest.setSnipeStatus(TradeConstants.FILLED);
					snipeTransactionRequest.setSnipe(true);
				
				}
			}
		} catch (Exception e) {
			purgeMessage(snipeTransactionRequest);
			e.printStackTrace();
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "updateOrDeleteSnipeOrderChannel")
	public Map<String, Object> updateOrDeleteSnipeOrderChannel(Map<String, Object> tradeOrderMap) throws Exception{
		SnipeTransactionRequest snipeTransactionRequest = (SnipeTransactionRequest) tradeOrderMap.get(TradeConstants.SNIPETRANSACTIONREQUEST);
		if(snipeTransactionRequest.hasSniped() && tradeOrderMap.get(TradeConstants.SWAP_ETH_FOR_TOKEN_HASH) != null) {
			purgeMessage(snipeTransactionRequest);
		}else {
			snipeOrderRepository.updateAvail(snipeTransactionRequest);
		}
		return tradeOrderMap;
	}


	private void purgeMessage(SnipeTransactionRequest snipeTransactionRequest) throws Exception {
		snipeOrderHistoryRepository.save(snipeTransactionRequest);
		snipeOrderRepository.delete(snipeTransactionRequest);
	}
	
}
