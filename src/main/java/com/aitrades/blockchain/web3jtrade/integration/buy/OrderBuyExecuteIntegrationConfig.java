package com.aitrades.blockchain.web3jtrade.integration.buy;

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

import com.aitrades.blockchain.web3jtrade.RabbitMQOrderSubmitBuyConfig;

@Configuration
@ComponentScan("com.aitrades.blockchain.web3jtrade.integration")
@IntegrationComponentScan("com.aitrades.blockchain.web3jtrade.integration")
@EnableIntegration
public class OrderBuyExecuteIntegrationConfig {

	@Autowired
	public RabbitMQOrderSubmitBuyConfig rabbitMQOrderSubmitBuyConfig;
	
	@Autowired
    private ConnectionFactory connectionFactory;
	
	@Bean
	@Autowired
	public IntegrationFlow buyTrade() {
		return IntegrationFlows.from(Amqp.inboundAdapter(rabbitMQOrderSubmitBuyConfig.orderSubmitBuyMessageListenerContainer(connectionFactory)))
							   .handle("orderBuyExecuteGatewayEndpoint", "transformBuyOrderChannel")
							   .handle("orderBuyExecuteGatewayEndpoint", "amountsInChannel")
							   .handle("orderBuyExecuteGatewayEndpoint", "swapETHForTokensChannel")
							   .handle("orderBuyExecuteGatewayEndpoint", "updateBuyOrderChannel")
							   .channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME)
							   .get();
	}
	
	@Bean
	public OrderBuyExecuteGatewayEndpoint orderBuyExecuteGatewayEndpoint() {
		return new OrderBuyExecuteGatewayEndpoint();
	}

}
