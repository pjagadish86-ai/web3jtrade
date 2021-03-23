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
import com.aitrades.blockchain.web3jtrade.trade.pendingTransaction.EthereumGethPendingTransactionsRetriever;
import com.aitrades.blockchain.web3jtrade.trade.pendingTransaction.EthereumParityPendingTransactionsRetriever;
import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;
import com.jsoniter.JsonIterator;

import io.reactivex.Flowable;
@SuppressWarnings({"unused", "rawtypes"})
//TODO:	// May be we should find a way to send null channel if there no valid movement.
public class OrderSnipeExecuteGatewayEndpoint{
	
	private static final String PAIR_CREATED = "PAIR_CREATED";
	private static final String HAS_RESERVES = "HAS_RESERVES";
	private static final String HAS_LIQUIDTY_EVENT = "HAS_LIQUIDTY_EVENT";
	private static final String OUTPUT_TOKENS = "OUTPUT_TOKENS";
	
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
	private EthereumDexTradeContractService ethereumDexTradeService;

	@Transformer(inputChannel = "rabbitMqSubmitOrderConsumer", outputChannel = "pairCreatedEventChannel")
	public Map<String, Object> rabbitMqSubmitOrderConsumer(byte[] message){
		String orderstr = new String(message);
		SnipeTransactionRequest transactionRequest  = JsonIterator.deserialize(orderstr, SnipeTransactionRequest.class);
		Map<String, Object> aitradesMap = new ConcurrentHashMap<String, Object>();
		aitradesMap.put(TRANSACTION_REQUEST, transactionRequest);
		return aitradesMap;
	}
	
	@ServiceActivator(inputChannel = "pairCreatedEventChannel", outputChannel = "getReservesEventChannel")
	public Map<String, Object> pairCreatedEventChannel(Map<String, Object> tradeOrderMap){
		SnipeTransactionRequest transactionRequest = (SnipeTransactionRequest)tradeOrderMap.get(TRANSACTION_REQUEST);
		Optional<Type> pairAddress  = ethereumDexTradeService.getPairAddress(transactionRequest.getRoute(), transactionRequest.getFromAddress(), transactionRequest.getToAddress())
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
			SnipeTransactionRequest transactionRequest = (SnipeTransactionRequest)tradeOrderMap.get(TRANSACTION_REQUEST);
			Tuple3<BigInteger, BigInteger, BigInteger> response = ethereumDexTradeService.getReservesOfPair(transactionRequest.getRoute(), transactionRequest.getPairAddress(), transactionRequest.getCredentials(), strategyGasProvider);
			if(response.component1().compareTo(BigInteger.ZERO) > 0 && response.component2().compareTo(BigInteger.ZERO) > 0) {
				tradeOrderMap.put(HAS_RESERVES, Boolean.TRUE);
			}
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "addLiquidityEvent", outputChannel = "approveChannel")
	public Map<String, Object> addLiquidityEvent(Map<String, Object> tradeOrderMap){
		if(tradeOrderMap.get(HAS_LIQUIDTY_EVENT) == null) {
			SnipeTransactionRequest SnipeTransactionRequest = (SnipeTransactionRequest)tradeOrderMap.get(TRANSACTION_REQUEST);
			EthereumDexContractEventService ethereumDexContractEventService = EthereumDexContractEventService.load(SnipeTransactionRequest.getToAddress(), web3jServiceClient.getWeb3j(), SnipeTransactionRequest.getCredentials(), strategyGasProvider);
			Flowable<AddLiquidityEventResponse> addLiquidityEventResponseFlowable = ethereumDexContractEventService.addLiquidityEventFlowable(DefaultBlockParameterName.PENDING, DefaultBlockParameterName.PENDING);
			AddLiquidityEventResponse response = addLiquidityEventResponseFlowable.blockingSingle();
			if(response != null) {
				tradeOrderMap.put(HAS_LIQUIDTY_EVENT, Boolean.TRUE);
			}
		// save to db? 
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "approveChannel", outputChannel = "amountsInChannel")
	public Map<String, Object> approveChannel(Map<String, Object> tradeOrderMap){
		
		TransactionRequest transactionRequest = (TransactionRequest) tradeOrderMap.get(TRANSACTION_REQUEST);
		
		String hash = ethereumDexTradeService.approve(transactionRequest.getRoute(), 
									   				  transactionRequest.getCredentials(),
													  transactionRequest.getToAddress(), 
													  strategyGasProvider,
													  GasModeEnum.fromValue(transactionRequest.getGasMode()));
		return tradeOrderMap;
		
	}
	
	@ServiceActivator(inputChannel = "amountsInChannel", outputChannel = "swapETHForTokensChannel")
	public Map<String, Object> amountsInChannel(Map<String, Object> tradeOrderMap){
		TransactionRequest transactionRequest = (TransactionRequest) tradeOrderMap.get(TRANSACTION_REQUEST);
		BigInteger outputTokens = ethereumDexTradeService.getAmountsIn(transactionRequest.getRoute(),
																       transactionRequest.getCredentials(), 
																       transactionRequest.getInputTokenValueAmountAsBigDecimal(),
																       transactionRequest.getSlipage(),
																       strategyGasProvider, 
																       GasModeEnum.fromValue(transactionRequest.getGasMode()),
																       transactionRequest.getMemoryPath());
		tradeOrderMap.put(OUTPUT_TOKENS, outputTokens);
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "swapETHForTokensChannel")
	public Map<String, Object> swapETHForTokensChannel(Map<String, Object> tradeOrderMap){
		TransactionRequest transactionRequest = (TransactionRequest) tradeOrderMap.get(TRANSACTION_REQUEST);
		BigInteger outputTokens = (BigInteger)tradeOrderMap.get(OUTPUT_TOKENS);
		String hash = ethereumDexTradeService.swapETHForTokens(transactionRequest.getRoute(),
															   transactionRequest.getCredentials(), 
															   transactionRequest.getInputTokenValueAmountAsBigInteger(),
															   outputTokens, 
															   strategyGasProvider, 
															   GasModeEnum.fromValue(transactionRequest.getGasMode()), 
															   1234211,
															   transactionRequest.getMemoryPath(), 
															   false);
		return tradeOrderMap;
	}
	
}
