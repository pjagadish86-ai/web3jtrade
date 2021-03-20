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

public class OrderSellExecuteGatewayEndpoint {
	
	@Autowired
	private EthereumDexTradeContractService ethereumDexTradeService;
	
	@ServiceActivator(inputChannel = "amountsOutChannel", outputChannel = "swapTokenForETHChannel")
	public Map<String, Object> amountsOutChannel(Map<String, Object> tradeOrderMap){
		String route= null;
		Credentials credentials= null;
		BigDecimal inputTokens= null;
		StrategyGasProvider customGasProvider= null;
		GasModeEnum gasModeEnum= null;
		List<String> memoryPathAddress= null;
		ethereumDexTradeService.getAmountsOut(route, credentials, inputTokens, customGasProvider, gasModeEnum, memoryPathAddress);
		// add amounts out into map;
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "swapTokenForETHChannel")
	public Map<String, Object> swapTokenForETHChannel(Map<String, Object> tradeOrderMap){
		String route= null;
		Credentials credentials= null;
		BigInteger inputTokens= null;
		StrategyGasProvider customGasProvider= null;
		GasModeEnum gasModeEnum= null;
		List<String> memoryPathAddress= null;
		BigInteger outputEthers= null;
		long deadLine = 0;
		boolean hasFee = false;
		ethereumDexTradeService.swapTokenForETH(route, credentials, inputTokens, outputEthers, customGasProvider, gasModeEnum, deadLine, memoryPathAddress, hasFee);
		// add amounts out into map;
		return tradeOrderMap;
	}
	
}
