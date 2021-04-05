package com.aitrades.blockchain.web3jtrade.integration.buy;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;

import com.aitrades.blockchain.web3jtrade.dex.contract.DexTradeContractService;
import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.Order;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.aitrades.blockchain.web3jtrade.oracle.gas.GasProvider;
import com.aitrades.blockchain.web3jtrade.repository.OrderHistoryRepository;
import com.aitrades.blockchain.web3jtrade.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Lists;

public class OrderBuyExecuteGatewayEndpoint {

	@Autowired
	private DexTradeContractService ethereumDexTradeService;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderHistoryRepository orderHistoryRepository;
	
	@Autowired
	private ObjectReader orderRequestObjectReader;
	
	@Autowired
	private GasProvider gasProvider;
	
	@Transformer(inputChannel = "transformBuyOrderChannel", outputChannel = "amountsInChannel")
	public Map<String, Object> transformBuyOrderChannel(byte[] message) throws Exception{
		Order order  = orderRequestObjectReader.readValue(message);
		Map<String, Object> aitradesMap = new ConcurrentHashMap<>();
		aitradesMap.put(TradeConstants.ORDER, order);
		return aitradesMap;
	}
	
	@ServiceActivator(inputChannel = "amountsInChannel", outputChannel = "swapETHForTokensChannel")
	public Map<String, Object> amountsInChannel(Map<String, Object> tradeOrderMap) throws Exception{
		Order order = null;
		try {
			order = (Order) tradeOrderMap.get(TradeConstants.ORDER);
			BigInteger outputTokens = ethereumDexTradeService.getAmountsIn(order.getRoute(),
																		   order.getCredentials(), 
																		   order.getFrom().getAmountAsBigInteger(),
																		   order.getSlippage().getSlipageInBipsInDouble(),
																           Lists.newArrayList(order.getTo().getTicker().getAddress(), TradeConstants.WETH_MAP.get(order.getRoute().toUpperCase())),
																           gasProvider.getGasPrice(GasModeEnum.fromValue(order.getGasMode()), order.getGasPrice().getValueBigInteger()),
																	       gasProvider.getGasPrice(GasModeEnum.fromValue(order.getGasMode()), order.getGasLimit().getValueBigInteger()),
																	       order.getGasMode());
			
			if(outputTokens != null && outputTokens.compareTo(BigInteger.ZERO) > 0 ) {
				tradeOrderMap.put(TradeConstants.OUTPUT_TOKENS, outputTokens);
			}
		} catch (Exception e) {
			order.setErrorMessage(e.getMessage());
			purgeMessage(order);
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "swapETHForTokensChannel", outputChannel = "updateBuyOrderChannel")
	public Map<String, Object> swapETHForTokensChannel(Map<String, Object> tradeOrderMap) throws Exception{
		Order order = null;
		try {
			order = (Order) tradeOrderMap.get(TradeConstants.ORDER);
			if (tradeOrderMap.get(TradeConstants.OUTPUT_TOKENS) != null) {
				
				BigInteger outputTokens = (BigInteger) tradeOrderMap.get(TradeConstants.OUTPUT_TOKENS);
				String hash = ethereumDexTradeService.swapETHForTokens(order.getRoute(), 
																       order.getCredentials(),
																	   order.getFrom().getAmountAsBigInteger(), 
																	   outputTokens, 
																	   300l, 
																	   Lists.newArrayList( TradeConstants.WETH_MAP.get(order.getRoute().toUpperCase()), order.getTo().getTicker().getAddress()),
																	   false, 
																	   gasProvider.getGasPrice(GasModeEnum.fromValue(order.getGasMode()), order.getGasPrice().getValueBigInteger()),
																       gasProvider.getGasPrice(GasModeEnum.fromValue(order.getGasMode()), order.getGasLimit().getValueBigInteger()),
																       order.getGasMode());
				if (StringUtils.isNotBlank(hash)) {
					tradeOrderMap.put(TradeConstants.SWAP_ETH_FOR_TOKEN_HASH, true);
					order.setSwappedHash(hash);
					order.getOrderEntity().setOrderState(TradeConstants.FILLED);
				} 
			}
		} catch (Exception e) {
			order.setErrorMessage(e.getMessage());
			purgeMessage(order);
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "updateBuyOrderChannel")
	public Map<String, Object> updateBuyOrderChannel(Map<String, Object> tradeOrderMap) throws Exception{
		Order order = (Order) tradeOrderMap.get(TradeConstants.ORDER);
		if(order.getOrderEntity().getOrderState().equalsIgnoreCase(TradeConstants.FILLED) && tradeOrderMap.get(TradeConstants.SWAP_ETH_FOR_TOKEN_HASH) != null) {
			purgeMessage(order);
		}
		return tradeOrderMap;
	}
	
	private void purgeMessage(Order order) throws Exception {
		orderHistoryRepository.save(order);
		orderRepository.delete(order);
	}
}
