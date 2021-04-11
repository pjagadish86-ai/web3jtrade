package com.aitrades.blockchain.web3jtrade.integration.sell;

import java.math.BigDecimal;
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
import com.aitrades.blockchain.web3jtrade.domain.TradeOverview;
import com.aitrades.blockchain.web3jtrade.oracle.gas.GasProvider;
import com.aitrades.blockchain.web3jtrade.repository.OrderHistoryRepository;
import com.aitrades.blockchain.web3jtrade.repository.OrderRepository;
import com.aitrades.blockchain.web3jtrade.repository.TradeOverviewRepository;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Lists;

public class OrderSellExecuteGatewayEndpoint {
	
	@Autowired
	private GasProvider gasProvider;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderHistoryRepository orderHistoryRepository;
	
	@Autowired 
	private TradeOverviewRepository tradeOverviewRepository;
	
	@Autowired
	private ObjectReader orderRequestObjectReader;
	
	@Autowired
	private DexTradeContractService ethereumDexTradeService;
	
	@Transformer(inputChannel = "transformSellOrderChannel", outputChannel = "amountsOutChannel")
	public Map<String, Object> transformSellOrderChannel(byte[] message) throws Exception{
		Order order  = orderRequestObjectReader.readValue(message);
		Map<String, Object> aitradesMap = new ConcurrentHashMap<String, Object>();
		aitradesMap.put(TradeConstants.ORDER, order);
		return aitradesMap;
	}
	
	@ServiceActivator(inputChannel = "amountsOutChannel", outputChannel = "swapTokenForETHChannel")
	public Map<String, Object> amountsOutChannel(Map<String, Object> tradeOrderMap) throws Throwable{
		Order order = null;
		try {
			order = (Order) tradeOrderMap.get(TradeConstants.ORDER);
			BigInteger inputTokens = ethereumDexTradeService.getAmountsOut(order.getRoute(),
																			order.getCredentials(), 
																			order.getFrom().getAmountAsBigInteger(),
																			order.getSlippage().getSlipageInBipsInDouble(),
																	        Lists.newArrayList(order.getFrom().getTicker().getAddress(), TradeConstants.WETH_MAP.get(order.getRoute().toUpperCase())),
																	        gasProvider.getGasPrice(GasModeEnum.fromValue(order.getGasMode()), order.getGasPrice().getValueBigInteger()),
																	     	gasProvider.getGasPrice(GasModeEnum.fromValue(order.getGasMode()), order.getGasLimit().getValueBigInteger()),
																		    order.getGasMode());
			
			
	     	
	     	
			if(inputTokens != null && inputTokens.compareTo(BigInteger.ZERO) > 0 ) {
				tradeOrderMap.put(TradeConstants.INPUT_TOKENS, inputTokens);
				
			}
		} catch (Exception e) {
			order.setErrorMessage(e.getMessage());
			purgeMessage(order);
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "swapTokenForETHChannel", outputChannel ="updateSellOrderChannel")
	public Map<String, Object> swapTokenForETHChannel(Map<String, Object> tradeOrderMap) throws Exception{
		if (tradeOrderMap.get(TradeConstants.INPUT_TOKENS) != null) {
			Order order = null;
			try {
				order = (Order) tradeOrderMap.get(TradeConstants.ORDER);
				BigInteger outputTokens = (BigInteger) tradeOrderMap.get(TradeConstants.INPUT_TOKENS);
				String hash = ethereumDexTradeService.swapTokenForETH(order.getRoute(), 
																	  order.getCredentials(),
																	  order.getFrom().getAmountAsBigInteger(), 
																	  outputTokens, 
																	  250l,
																	  Lists.newArrayList(order.getFrom().getTicker().getAddress(), TradeConstants.WETH_MAP.get(order.getRoute().toUpperCase())),
																	  order.isFee(), 
																	  gasProvider.getGasPrice(GasModeEnum.fromValue(order.getGasMode()), order.getGasPrice().getValueBigInteger()),
																      gasProvider.getGasPrice(GasModeEnum.fromValue(order.getGasMode()), order.getGasLimit().getValueBigInteger()),
																      order.getGasMode());
				if (StringUtils.isNotBlank(hash)) {
					tradeOrderMap.put(TradeConstants.SWAP_TOKEN_FOR_ETH_HASH, true);
					order.setSwappedHash(hash);
					order.getOrderEntity().setOrderState(TradeConstants.FILLED);
				}
			} catch (Exception e) {
				order.setErrorMessage(e.getMessage());
				purgeMessage(order);
			} 
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "updateSellOrderChannel")
	public Map<String, Object> updateSellOrderChannel(Map<String, Object> tradeOrderMap) throws Exception{
		Order order = (Order) tradeOrderMap.get(TradeConstants.ORDER);
		if(tradeOrderMap.get(TradeConstants.SWAP_TOKEN_FOR_ETH_HASH) != null) {
			tradeOverviewRepository.save(mapRequestToTradeOverView(order));
			purgeMessage(order);
		}
		return tradeOrderMap;
	}

	private void purgeMessage(Order order) throws Exception {
		orderHistoryRepository.save(order);
		orderRepository.delete(order);
	}
	
	private TradeOverview mapRequestToTradeOverView(Order request) {
		TradeOverview overview = new TradeOverview();
		overview.setApprovedHash(request.getApprovedHash());
		overview.setSwappedHash(request.getSwappedHash());
		overview.setErrorMessage(request.getErrorMessage());
		overview.setId(request.getId());
		overview.setOrderDesc("TRADE");
		overview.setOrderSide(request.getOrderEntity().getOrderSide());
		overview.setOrderState(request.getOrderEntity().getOrderState());
		overview.setOrderType(request.getOrderEntity().getOrderType());
		overview.setExecutedPrice(request.getExecutionPrice());
		return overview;
	}
	
}
