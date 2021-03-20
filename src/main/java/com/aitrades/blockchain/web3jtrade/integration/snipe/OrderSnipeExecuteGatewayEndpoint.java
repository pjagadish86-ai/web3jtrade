package com.aitrades.blockchain.web3jtrade.integration.snipe;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import com.aitrades.blockchain.web3jtrade.client.DexSubGraphPriceFactoryClient;
import com.aitrades.blockchain.web3jtrade.client.Web3jServiceClient;
import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexTradeContractService;
import com.aitrades.blockchain.web3jtrade.dex.contract.event.EthereumDexContractEventService;
import com.aitrades.blockchain.web3jtrade.dex.contract.event.EthereumDexContractEventService.AddLiquidityEventResponse;
import com.aitrades.blockchain.web3jtrade.domain.Order;
import com.aitrades.blockchain.web3jtrade.trade.snipe.EthereumGethPendingTransactionsRetriever;
import com.aitrades.blockchain.web3jtrade.trade.snipe.EthereumParityPendingTransactionsRetriever;

import io.reactivex.Flowable;
@SuppressWarnings("unused")
public class OrderSnipeExecuteGatewayEndpoint {
	
	@Resource(name="web3jServiceClient")
	private Web3jServiceClient web3jServiceClient;
	// TODO: strict support only one snipe either parity or geth.
	
	@Autowired
	private EthereumGethPendingTransactionsRetriever gethPendingTrxsRetriever;
	
	@Autowired
	private EthereumParityPendingTransactionsRetriever parityPendingTrxsRetriever;
	
	@Resource(name="orderSubmitRabbitTemplate")
	private AmqpTemplate orderSubmitRabbitTemplate;
	
	@Resource(name="orderSubmitRabbitTemplate")
	private DexSubGraphPriceFactoryClient graphPriceFactoryClient;
	
	@Autowired
	private EthereumDexTradeContractService tradeContractService;

	// for any service of below method try this idea:
	//1. Check if graphql returns any data if not check for the events
	//2. if graphql return data then dont do any thing.
	//3. if graphql doesnt return any data call the events.  this lot times.
	
	
	@ServiceActivator(inputChannel = "pairCreatedEventChannel", outputChannel = "getReservesEventChannel")
	public Map<String, Object> pairCreatedEventChannel(Map<String, Object> tradeOrderMap){
		Order orders =  (Order)tradeOrderMap.get("order");
		BigInteger gas = null;
		String contractAddress = null;
		String route = null;
		String tokenA= "Either weth or eth";
		String tokenB = "snipe contract address";
		Type pairAddress  = tradeContractService.getPairAddress(route, tokenA, tokenB).parallelStream().findFirst().get();
		//save into order object
		
		tradeOrderMap.put("SNIPE", null);
		//??? if the time interval doesnt satisfy then do send the data back to snipe and retry until a max of two mins. or do poll?
		return tradeOrderMap;
	}
	

	@ServiceActivator(inputChannel = "getReservesEventChannel", outputChannel = "addLiquidityEvent")
	public Map<String, Object> getReservesEventChannel(Map<String, Object> tradeOrderMap){
		BigInteger gas = null;
		String contractAddress = null;
		String route = null;
		BigInteger pendingQuotedGas  = null;//parityPendingTrxsRetriever.pendingTransactionsFrontRunnerFilter(route, false, false, gas, contractAddress);
		tradeOrderMap.put("SNIPE", pendingQuotedGas);
		// if the time interval doesnt satisfy then do send the data back to snipe and retry until a max of two mins.
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "addLiquidityEvent", outputChannel = "orderSubmitRabbitMqBuyOrSellChannel")
	public Map<String, Object> addLiquidityEvent(Map<String, Object> tradeOrderMap){
		BigInteger gas = null;
		String contractAddress = null;
		String route = null;
		BigInteger pendingQuotedGas  = null;// parityPendingTrxsRetriever.pendingTransactionsFrontRunnerFilter(route, false, false, gas, contractAddress);
		tradeOrderMap.put("SNIPE", pendingQuotedGas);
		Credentials credentials = null;
		ContractGasProvider contractGasProvider = new DefaultGasProvider();
		EthereumDexContractEventService ethereumDexContractEventService = EthereumDexContractEventService.load(contractAddress, web3jServiceClient.getWeb3j(), credentials, contractGasProvider);
		Flowable<AddLiquidityEventResponse> addLiquidityEventResponseFlowable = ethereumDexContractEventService.addLiquidityEventFlowable(DefaultBlockParameterName.PENDING, DefaultBlockParameterName.PENDING);
		// if the time interval doesn't satisfy then do send the data back to snipe and retry until a max of two mins.
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "orderSubmitRabbitMqBuyOrSellChannel")
	public  Map<String, Object> orderSubmitRabbitMqBuyOrSellChannel(Map<String, Object> tradeOrderMap){
		return tradeOrderMap;
	}
	
}
