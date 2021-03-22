package com.aitrades.blockchain.web3jtrade.integration.sell;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.web3j.crypto.Credentials;

import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexTradeContractService;
import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.StrategyGasProvider;
import com.aitrades.blockchain.web3jtrade.domain.TransactionRequest;

public class OrderSellExecuteGatewayEndpoint {
	
	private static final String INPUT_TOKENS = "INPUT_TOKENS";
	private static final String TRANSACTION_REQUEST = "TRANSACTION_REQUEST";
	
	@Autowired
	public StrategyGasProvider strategyGasProvider;
	
	@Autowired
	protected EthereumDexTradeContractService ethereumDexTradeService;
	
	@ServiceActivator(inputChannel = "amountsOutChannel", outputChannel = "swapTokenForETHChannel")
	public Map<String, Object> amountsOutChannel(Map<String, Object> tradeOrderMap){
		TransactionRequest transactionRequest = (TransactionRequest) tradeOrderMap.get(TRANSACTION_REQUEST);
		
		BigInteger outputTokens = ethereumDexTradeService.getAmountsOut(transactionRequest.getRoute(),
															     transactionRequest.getCredentials(), 
															     transactionRequest.getInputTokenValueAmountAsBigDecimal(),
															     transactionRequest.getSlipage(),
															     strategyGasProvider, 
															     GasModeEnum.fromValue(transactionRequest.getGasMode()),
															     transactionRequest.getMemoryPath());
		tradeOrderMap.put(INPUT_TOKENS, outputTokens);
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "swapTokenForETHChannel")
	public Map<String, Object> swapTokenForETHChannel(Map<String, Object> tradeOrderMap){
		TransactionRequest transactionRequest = (TransactionRequest) tradeOrderMap.get(TRANSACTION_REQUEST);
		BigInteger outputTokens = (BigInteger)tradeOrderMap.get(INPUT_TOKENS);
		String hash = ethereumDexTradeService.swapTokenForETH(transactionRequest.getRoute(),
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
