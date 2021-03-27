package com.aitrades.blockchain.web3jtrade.integration.buy;

import java.math.BigInteger;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;

import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexTradeContractService;
import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.StrategyGasProvider;
import com.aitrades.blockchain.web3jtrade.domain.TransactionRequest;

public class OrderBuyExecuteGatewayEndpoint {

	private static final String OUTPUT_TOKENS = "OUTPUT_TOKENS";
	private static final String TRANSACTION_REQUEST = "TRANSACTION_REQUEST";
	
	@Autowired
	public StrategyGasProvider strategyGasProvider;
	
	@Autowired
	protected EthereumDexTradeContractService ethereumDexTradeService;
	
	@ServiceActivator(inputChannel = "approveChannel", outputChannel = "amountsInChannel")
	public Map<String, Object> approveChannel(Map<String, Object> tradeOrderMap) throws Exception{
		
		TransactionRequest transactionRequest = (TransactionRequest) tradeOrderMap.get(TRANSACTION_REQUEST);
		
		String hash = ethereumDexTradeService.approve(transactionRequest.getRoute(), 
									   				  transactionRequest.getCredentials(),
													  transactionRequest.getToAddress(), 
													  strategyGasProvider,
													  GasModeEnum.fromValue(transactionRequest.getGasMode()));
		return tradeOrderMap;
		
	}
	
	@ServiceActivator(inputChannel = "amountsInChannel", outputChannel = "swapETHForTokensChannel")
	public Map<String, Object> amountsInChannel(Map<String, Object> tradeOrderMap) throws Exception{
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
	public Map<String, Object> swapETHForTokensChannel(Map<String, Object> tradeOrderMap) throws Exception{
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
