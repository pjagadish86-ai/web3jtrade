package com.aitrades.blockchain.web3jtrade.integration.sell;

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
import com.aitrades.blockchain.web3jtrade.oracle.gas.StrategyGasProvider;
import com.aitrades.blockchain.web3jtrade.repository.OrderHistoryRepository;
import com.aitrades.blockchain.web3jtrade.repository.OrderRepository;
import com.aitrades.blockchain.web3jtrade.side.OrderState;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Lists;

public class OrderSellExecuteGatewayEndpoint {
	
	@Autowired
	public StrategyGasProvider strategyGasProvider;
	
	@Autowired
	public OrderRepository orderRepository;
	
	@Autowired
	public OrderHistoryRepository orderHistoryRepository;
	
	@Autowired
	public ObjectReader orderRequestObjectReader;
	
	@Autowired
	protected DexTradeContractService ethereumDexTradeService;
	
	@Transformer(inputChannel = "transformSellOrderChannel", outputChannel = "amountsOutChannel")
	public Map<String, Object> transformSellOrderChannel(byte[] message) throws Exception{
		Order order  = orderRequestObjectReader.readValue(message);
		if(order.getOrderEntity().getOrderState().equalsIgnoreCase(OrderState.FILLED.name())) {
			throw new Exception("Order has already been successfull, if need any new order, other please place  order");
		}
		
		Map<String, Object> aitradesMap = new ConcurrentHashMap<String, Object>();
		aitradesMap.put(TradeConstants.ORDER, order);
		return aitradesMap;
	}
	
	@ServiceActivator(inputChannel = "amountsOutChannel", outputChannel = "swapTokenForETHChannel")
	public Map<String, Object> amountsOutChannel(Map<String, Object> tradeOrderMap) throws Throwable{
		Order order = null;
		try {
			order = (Order) tradeOrderMap.get(TradeConstants.ORDER);
			BigInteger gasPrice = null;
			BigInteger gasLimit = null;
			if(order.getGasMode().equalsIgnoreCase("CUSTOM")) {
				gasPrice = order.getGasPrice().getValueBigInteger();
				gasLimit = order.getGasLimit().getValueBigInteger();
			}else {
				gasPrice = strategyGasProvider.getGasPrice(GasModeEnum.fromValue(order.getGasMode()));
				gasLimit = strategyGasProvider.getGasLimit(order.getRoute());
			}
			BigInteger inputTokens = ethereumDexTradeService.getAmountsOut(order.getRoute(),
																			order.getCredentials(), 
																			order.getFrom().getAmountAsBigInteger(),
																			order.getSlippage().getSlipageInBipsInDouble(),
																	        Lists.newArrayList(order.getFrom().getTicker().getAddress(), TradeConstants.WETH_MAP.get(order.getRoute().toUpperCase())),
																	        gasPrice,
																	        gasLimit);
			if(inputTokens != null && inputTokens.compareTo(BigInteger.ZERO) > 0 ) {
				tradeOrderMap.put(TradeConstants.INPUT_TOKENS, inputTokens);
				
			}
		} catch (Exception e) {
			purgeMessage(order);
			e.printStackTrace();
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "swapTokenForETHChannel", outputChannel ="updateSellOrderChannel")
	public Map<String, Object> swapTokenForETHChannel(Map<String, Object> tradeOrderMap) throws Exception{
		if (tradeOrderMap.get(TradeConstants.INPUT_TOKENS) != null) {
			Order order = null;
			try {
				order = (Order) tradeOrderMap.get(TradeConstants.ORDER);
				BigInteger gasPrice = null;
				BigInteger gasLimit = null;
				if(order.getGasMode().equalsIgnoreCase("CUSTOM")) {
					gasPrice = order.getGasPrice().getValueBigInteger();
					gasLimit = order.getGasLimit().getValueBigInteger();
				}else {
					gasPrice = strategyGasProvider.getGasPrice(GasModeEnum.fromValue(order.getGasMode()));
					gasLimit = strategyGasProvider.getGasLimit(order.getRoute());
				}
				BigInteger outputTokens = (BigInteger) tradeOrderMap.get(TradeConstants.INPUT_TOKENS);
				String hash = ethereumDexTradeService.swapTokenForETH(order.getRoute(), 
																	  order.getCredentials(),
																	  order.getFrom().getAmountAsBigInteger(), 
																	  outputTokens, 
																	  250l,
																	  Lists.newArrayList(order.getFrom().getTicker().getAddress(), TradeConstants.WETH_MAP.get(order.getRoute().toUpperCase())), // TODO populate from Client
																	  false, 
																	  gasPrice,
																	  gasLimit);
				if (StringUtils.isNotBlank(hash)) {
					tradeOrderMap.put(TradeConstants.SWAP_TOKEN_FOR_ETH_HASH, true);
					order.setSwappedHash(hash);
					order.getOrderEntity().setOrderState(TradeConstants.FILLED);
				}
			} catch (Exception e) {
				purgeMessage(order);
				e.printStackTrace();
			} 
		}
		return tradeOrderMap;
	}
	
	@ServiceActivator(inputChannel = "updateSellOrderChannel")
	public Map<String, Object> updateSellOrderChannel(Map<String, Object> tradeOrderMap) throws Exception{
		Order order = (Order) tradeOrderMap.get(TradeConstants.ORDER);
		if(tradeOrderMap.get(TradeConstants.SWAP_TOKEN_FOR_ETH_HASH) != null) {
			purgeMessage(order);
		}
		return tradeOrderMap;
	}

	private void purgeMessage(Order order) throws Exception {
		orderHistoryRepository.save(order);
		orderRepository.delete(order);
	}
	
}
