package com.aitrades.blockchain.web3jtrade.integration.sell;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;

import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexTradeContractService;
import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.Order;
import com.aitrades.blockchain.web3jtrade.domain.StrategyGasProvider;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.aitrades.blockchain.web3jtrade.repository.OrderHistoryRepository;
import com.aitrades.blockchain.web3jtrade.repository.OrderRepository;
import com.aitrades.blockchain.web3jtrade.side.OrderState;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Lists;

public class OrderSellExecuteGatewayEndpoint {
	
	private static final String INPUT_TOKENS = "INPUT_TOKENS";
	private static final String WETH = "0xc778417E063141139Fce010982780140Aa0cD5Ab";

	@Autowired
	public StrategyGasProvider strategyGasProvider;
	
	@Autowired
	public OrderRepository orderRepository;
	
	@Autowired
	public OrderHistoryRepository orderHistoryRepository;
	
	@Autowired
	public ObjectReader orderRequestObjectReader;
	
	@Autowired
	protected EthereumDexTradeContractService ethereumDexTradeService;
	
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
	public Map<String, Object> amountsOutChannel(Map<String, Object> tradeOrderMap) throws Exception{
		Order order = (Order) tradeOrderMap.get(TradeConstants.ORDER);
		BigInteger inputTokens = ethereumDexTradeService.getAmountsOut(order.getRoute(),
																		order.getCredentials(), 
																		order.getFrom().getAmountAsBigDecimal(),
																		order.getSlippage().getSlipageInBips(),
																        strategyGasProvider, 
																        GasModeEnum.fromValue(order.getGasMode()),
																        Lists.newArrayList(order.getFrom().getTicker().getAddress(), WETH));
		if(inputTokens != null && inputTokens.compareTo(BigInteger.ZERO) > 0 ) {
			tradeOrderMap.put(INPUT_TOKENS, inputTokens);
			return tradeOrderMap;
		}
		throw new Exception("AmountsOut tokens are zero or some error, will retry");
	}
	
	@ServiceActivator(inputChannel = "swapTokenForETHChannel", outputChannel ="updateSellOrderChannel")
	public Map<String, Object> swapTokenForETHChannel(Map<String, Object> tradeOrderMap) throws Exception{
		if (tradeOrderMap.get(INPUT_TOKENS) != null) {
			Order order = (Order) tradeOrderMap.get(TradeConstants.ORDER);
			BigInteger outputTokens = (BigInteger) tradeOrderMap.get(INPUT_TOKENS);
			String hash = ethereumDexTradeService.swapTokenForETH(order.getRoute(), 
																  order.getCredentials(),
																  order.getFrom().getAmountAsBigInteger(), 
																  outputTokens, 
																  strategyGasProvider,
																  GasModeEnum.fromValue(order.getGasMode()), 
																  250l,
																  Lists.newArrayList(order.getFrom().getTicker().getAddress(), WETH), // TODO populate from Client
																  false);
			if (StringUtils.isNotBlank(hash)) {
				tradeOrderMap.put(TradeConstants.SWAP_TOKEN_FOR_ETH_HASH, true);
				order.getOrderEntity().setOrderState(TradeConstants.FILLED);
				return tradeOrderMap;
			} 
		}
		throw new Exception("Unable to SWAP_TOKEN_FOR_ETH_HASH");
	}
	
	@ServiceActivator(inputChannel = "updateSellOrderChannel")
	public Map<String, Object> updateSellOrderChannel(Map<String, Object> tradeOrderMap) throws Exception{
		Order order = (Order) tradeOrderMap.get(TradeConstants.ORDER);
		if(tradeOrderMap.get(TradeConstants.SWAP_TOKEN_FOR_ETH_HASH) != null) {
			orderHistoryRepository.insert(order);
			orderRepository.delete(order);
		}
		return tradeOrderMap;
	}
	
}
