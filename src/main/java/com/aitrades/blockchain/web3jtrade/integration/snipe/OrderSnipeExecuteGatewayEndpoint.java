package com.aitrades.blockchain.web3jtrade.integration.snipe;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.tuples.generated.Tuple3;

import com.aitrades.blockchain.web3jtrade.client.DexSubGraphPriceFactoryClient;
import com.aitrades.blockchain.web3jtrade.client.Web3jServiceClient;
import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexTradeContractService;
import com.aitrades.blockchain.web3jtrade.dex.contract.event.EthereumDexContractEventService;
import com.aitrades.blockchain.web3jtrade.dex.contract.event.EthereumDexContractEventService.AddLiquidityEventResponse;
import com.aitrades.blockchain.web3jtrade.domain.StrategyGasProvider;
import com.aitrades.blockchain.web3jtrade.domain.TransactionRequest;
import com.aitrades.blockchain.web3jtrade.trade.snipe.EthereumGethPendingTransactionsRetriever;
import com.aitrades.blockchain.web3jtrade.trade.snipe.EthereumParityPendingTransactionsRetriever;
import com.jsoniter.JsonIterator;

import io.reactivex.Flowable;
@SuppressWarnings({"unused", "rawtypes"})
//TODO:	// May be we should find a way to send null channel if there no valid movement.
public class OrderSnipeExecuteGatewayEndpoint{
	
	private static final String PAIR_CREATED = "PAIR_CREATED";
	private static final String HAS_RESERVES = "HAS_RESERVES";
	private static final String HAS_LIQUIDTY_EVENT = "HAS_LIQUIDTY_EVENT";
	
	@Resource(name="web3jServiceClient")
	private Web3jServiceClient web3jServiceClient;
	// TODO: strict support only one front run either parity or geth.
	private static final String TRANSACTION_REQUEST = "TRANSACTION_REQUEST";
	private static final String ORDER_DECISION = "ORDER_DECISION";
	
	@Autowired
	private SnipeService snipeService;
	
	@Autowired
	private EthereumGethPendingTransactionsRetriever gethPendingTrxsRetriever;
	
	@Autowired
	private EthereumParityPendingTransactionsRetriever parityPendingTrxsRetriever;
	
	@Resource(name="orderSubmitRabbitTemplate")
	private AmqpTemplate orderSubmitRabbitTemplate;
	
	@Autowired
	public StrategyGasProvider strategyGasProvider;
	
	@Autowired
	private DexSubGraphPriceFactoryClient graphPriceFactoryClient;
	
	@Autowired
	private EthereumDexTradeContractService tradeContractService;

	@Transformer(inputChannel = "rabbitMqSubmitOrderConsumer", outputChannel = "pairCreatedEventChannel")
	public Map<String, Object> rabbitMqSubmitOrderConsumer(byte[] message){
		String orderstr = new String(message);
		TransactionRequest transactionRequest  = JsonIterator.deserialize(orderstr, TransactionRequest.class);
		Map<String, Object> aitradesMap = new ConcurrentHashMap<String, Object>();
		aitradesMap.put(TRANSACTION_REQUEST, transactionRequest);
		return aitradesMap;
	}
	
	@ServiceActivator(inputChannel = "pairCreatedEventChannel", outputChannel = "getReservesEventChannel")
	public Map<String, Object> pairCreatedEventChannel(Map<String, Object> tradeOrderMap){
		TransactionRequest transactionRequest = (TransactionRequest)tradeOrderMap.get(TRANSACTION_REQUEST);
		Optional<Type> pairAddress  = tradeContractService.getPairAddress(transactionRequest.getRoute(), transactionRequest.getFromAddress(), transactionRequest.getToAddress())
												          .parallelStream()
												          .findFirst();
		if(pairAddress.isPresent()) {
			tradeOrderMap.put(PAIR_CREATED, Boolean.TRUE);
			transactionRequest.setPairAddress((String)pairAddress.get().getValue());
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "getReservesEventChannel", outputChannel = "addLiquidityEvent")
	public Map<String, Object> getReservesEventChannel(Map<String, Object> tradeOrderMap){
		if(tradeOrderMap.get(HAS_RESERVES) == null) {
			TransactionRequest transactionRequest = (TransactionRequest)tradeOrderMap.get(TRANSACTION_REQUEST);
			Tuple3<BigInteger, BigInteger, BigInteger> response = tradeContractService.getReservesOfPair(transactionRequest.getRoute(), transactionRequest.getPairAddress(), transactionRequest.getCredentials(), strategyGasProvider);
			if(response.component1().compareTo(BigInteger.ZERO) > 0 && response.component2().compareTo(BigInteger.ZERO) > 0) {
				tradeOrderMap.put(HAS_RESERVES, Boolean.TRUE);
			}
		
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "addLiquidityEvent", outputChannel = "orderSubmitSnipeChannel")
	public Map<String, Object> addLiquidityEvent(Map<String, Object> tradeOrderMap){
		if(tradeOrderMap.get(HAS_LIQUIDTY_EVENT) == null) {
			TransactionRequest transactionRequest = (TransactionRequest)tradeOrderMap.get(TRANSACTION_REQUEST);
			EthereumDexContractEventService ethereumDexContractEventService = EthereumDexContractEventService.load(transactionRequest.getToAddress(), web3jServiceClient.getWeb3j(), transactionRequest.getCredentials(), strategyGasProvider);
			Flowable<AddLiquidityEventResponse> addLiquidityEventResponseFlowable = ethereumDexContractEventService.addLiquidityEventFlowable(DefaultBlockParameterName.PENDING, DefaultBlockParameterName.PENDING);
			AddLiquidityEventResponse response = addLiquidityEventResponseFlowable.blockingSingle();
			if(response != null) {
				tradeOrderMap.put(HAS_LIQUIDTY_EVENT, Boolean.TRUE);
			}
		// save to db? 
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "orderSubmitSnipeChannel")
	public Map<String, Object> orderSubmitRabbitMqBuyOrSellChannel(Map<String, Object> tradeOrderMap){
		if(tradeOrderMap.get(HAS_LIQUIDTY_EVENT) != null) {
			snipeService.snipe(tradeOrderMap);// may be this is bad we should delegate this request to (buy or sell) integration endpoint.
		}
		return tradeOrderMap;
	}

}
