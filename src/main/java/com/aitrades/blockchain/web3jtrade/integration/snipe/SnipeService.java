package com.aitrades.blockchain.web3jtrade.integration.snipe;

import java.math.BigInteger;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexTradeContractService;
import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;
import com.aitrades.blockchain.web3jtrade.domain.StrategyGasProvider;

@Component
public class SnipeService {

	private static final String TRANSACTION_REQUEST = "TRANSACTION_REQUEST";
	
	@Autowired
	public StrategyGasProvider strategyGasProvider;
	
	@Autowired
	private EthereumDexTradeContractService tradeContractService;
	// this needs serious refactor wtf is this code its so ugly
	public String snipe(Map<String, Object> tradeOrderMap) {
		try {
			SnipeTransactionRequest transactionRequest = (SnipeTransactionRequest) tradeOrderMap.get(TRANSACTION_REQUEST);
			
			tradeContractService.approve(transactionRequest.getRoute(), 
										 transactionRequest.getCredentials(),
										 transactionRequest.getToAddress(), 
										 strategyGasProvider,
										 GasModeEnum.fromValue(transactionRequest.getGasMode()));
			
			BigInteger outputTokens = tradeContractService.getAmountsIn(transactionRequest.getRoute(),
																        transactionRequest.getCredentials(), 
																        transactionRequest.getInputTokenValueAmountAsBigDecimal(),
																        transactionRequest.getSlipage(),
																        strategyGasProvider, 
																        GasModeEnum.fromValue(transactionRequest.getGasMode()),
																        transactionRequest.getMemoryPath());
			
			String hash = tradeContractService.swapETHForTokens(transactionRequest.getRoute(),
																transactionRequest.getCredentials(), 
																transactionRequest.getInputTokenValueAmountAsBigInteger(),
																outputTokens, 
																strategyGasProvider, 
																GasModeEnum.fromValue(transactionRequest.getGasMode()), 
																1234211,
																transactionRequest.getMemoryPath(), 
																false);
			return hash;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
