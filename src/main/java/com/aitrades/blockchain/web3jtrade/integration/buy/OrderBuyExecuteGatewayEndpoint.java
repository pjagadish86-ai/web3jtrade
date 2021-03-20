package com.aitrades.blockchain.web3jtrade.integration.buy;

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

public class OrderBuyExecuteGatewayEndpoint {
	
	@Autowired
	private EthereumDexTradeContractService ethereumDexTradeService;
	
	@ServiceActivator(inputChannel = "approveChannel", outputChannel = "amountsInChannel")
	public Map<String, Object> approveChannel(Map<String, Object> tradeOrderMap){
		String route= null;
		Credentials credentials= null;
		StrategyGasProvider customGasProvider= null;
		GasModeEnum gasModeEnum= null;
		String contractAddress= null;
		ethereumDexTradeService.approve(route, credentials, contractAddress, customGasProvider, gasModeEnum);
		// add amounts out into map;
		return tradeOrderMap;
		
	}
	
	@ServiceActivator(inputChannel = "amountsInChannel", outputChannel = "swapETHForTokensChannel")
	public Map<String, Object> amountsInChannel(Map<String, Object> tradeOrderMap){
		String route= null;
		Credentials credentials= null;
		BigDecimal inputTokens= null;
		StrategyGasProvider customGasProvider= null;
		GasModeEnum gasModeEnum= null;
		List<String> memoryPathAddress= null;
		ethereumDexTradeService.getAmountsIn(route, credentials, inputTokens, customGasProvider, gasModeEnum, memoryPathAddress);
		// add amounts out into map;
		// this should be calculated with slippage tolearanc
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "swapETHForTokensChannel")
	public Map<String, Object> swapETHForTokensChannel(Map<String, Object> tradeOrderMap){
		String route= null;
		Credentials credentials= null;
		BigInteger inputEthers= null;
		StrategyGasProvider customGasProvider= null;
		GasModeEnum gasModeEnum= null;
		List<String> memoryPathAddress= null;
		long deadLine = 0;
		boolean hasFee = false;
		BigInteger outPutTokens = null;// this nothign but amoutnsin
		ethereumDexTradeService.swapETHForTokens(route, credentials, inputEthers, outPutTokens, customGasProvider, gasModeEnum, deadLine, memoryPathAddress, hasFee);
		// add amounts out into map;
		return tradeOrderMap;
	}
}
