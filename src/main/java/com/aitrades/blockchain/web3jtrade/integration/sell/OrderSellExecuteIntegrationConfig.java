package com.aitrades.blockchain.web3jtrade.integration.sell;

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

import com.aitrades.blockchain.web3jtrade.RabbitMQOrderSubmitSellConfig;

@Configuration
@ComponentScan("com.aitrades.blockchain.web3jtrade.integration")
@IntegrationComponentScan("com.aitrades.blockchain.web3jtrade.integration")
@EnableIntegration
public class OrderSellExecuteIntegrationConfig {

	@Autowired
	public RabbitMQOrderSubmitSellConfig rabbitMQOrderSubmitSellConfig;
	
	@Autowired
    private ConnectionFactory connectionFactory;
	
	@Bean
	@Autowired
	public IntegrationFlow sellTrade() {
		return IntegrationFlows.from(Amqp.inboundAdapter(rabbitMQOrderSubmitSellConfig.orderSubmitSellMessageListenerContainer(connectionFactory)))
							   .handle("orderSellExecuteGatewayEndpoint", "swapTokenForETHChannel")
							   .channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME)
							   .get();
	}

	@Bean
	public OrderSellExecuteGatewayEndpoint orderSellExecuteGatewayEndpoint() {
		return new OrderSellExecuteGatewayEndpoint();
	}
}
