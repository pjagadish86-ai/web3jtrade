package com.aitrades.blockchain.web3jtrade.integration.snipe;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import com.aitrades.blockchain.web3jtrade.RabbitMQOrderSubmitSnipeConfig;

@Configuration
@ComponentScan("com.aitrades.blockchain.web3jtrade.integration")
@IntegrationComponentScan("com.aitrades.blockchain.web3jtrade.integration")
@EnableIntegration
public class OrderSnipeExecuteIntegrationConfig {

	@Autowired
	public RabbitMQOrderSubmitSnipeConfig rabbitMQOrderSubmitSnipeConfig;
	
	@Autowired
    private ConnectionFactory connectionFactory;
	
//	@Bean
//	@Autowired
//	public IntegrationFlow snipeTrade() {
//		return IntegrationFlows.from(Amqp.inboundAdapter(rabbitMQOrderSubmitSnipeConfig.orderSubmitSnipeMessageListenerContainer(connectionFactory)))
//							   .handle("orderSnipeExecuteGatewayEndpoint", "rabbitMqSubmitOrderConsumer")
//							   .handle("orderSnipeExecuteGatewayEndpoint", "pairCreatedEventChannel")
//							   .handle("orderSnipeExecuteGatewayEndpoint", "liquidityEventOrReservesFinderChannel")
//							   .handle("orderSnipeExecuteGatewayEndpoint", "amountsInChannel")
//							   .handle("orderSnipeExecuteGatewayEndpoint", "swapETHForTokensChannel")
//							   .handle("orderSnipeExecuteGatewayEndpoint", "updateOrDeleteSnipeOrderChannel")
//							   .channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME)
//							   .get();
//	}
	
	@Bean
	@Autowired
	public IntegrationFlow snipeTrade() {
		return IntegrationFlows.from(Amqp.inboundAdapter(rabbitMQOrderSubmitSnipeConfig.orderSubmitSnipeMessageListenerContainer(connectionFactory)))
							   .handle("snipeExeEndpoint", "snipeOrderMQReciever")
							   .handle("snipeExeEndpoint", "snipeSwapChannel")
							   .channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME)
							   .get();
	}
	
//	@Bean
//	@Autowired
//	public IntegrationFlow snipeTrade() {
//		return IntegrationFlows.from(Amqp.inboundAdapter(rabbitMQOrderSubmitSnipeConfig.orderSubmitSnipeMessageListenerContainer(connectionFactory)))
//							   .handle("orderSnipeExecuteGatewayEndpoint", "rabbitMqSubmitOrderConsumer")
//							   .handle("orderSnipeExecuteGatewayEndpoint", "pairCreatedEventChannel")
//							   .handle("orderSnipeExecuteGatewayEndpoint", "getReservesEventChannel")
//							   .handle("orderSnipeExecuteGatewayEndpoint", "addLiquidityEvent")
//							//   .handle("orderSnipeExecuteGatewayEndpoint", "approveChannel")
//							   .handle("orderSnipeExecuteGatewayEndpoint", "amountsInChannel")
//							   .handle("orderSnipeExecuteGatewayEndpoint", "swapETHForTokensChannel")
//							   .handle("orderSnipeExecuteGatewayEndpoint", "updateOrDeleteSnipeOrderChannel")
//							   .channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME)
//							   .get();
//	}
	@Bean(name ="snipeExeEndpoint")
	public SnipeExeEndpointV1 snipeExeEndpoint() {
		return new SnipeExeEndpointV1();
	}
//
//	@Bean
//	public OrderSnipeExecuteGatewayEndpoint orderSnipeExecuteGatewayEndpoint() {
//		return new OrderSnipeExecuteGatewayEndpoint();
//	}

	
}
