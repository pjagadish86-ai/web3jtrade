package com.aitrades.blockchain.web3jtrade;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQOrderSubmitBuyConfig {

	@Value("${aitrades.order.submit.buy.rabbitmq.queue}")
	private String orderSubmitBuyQueueName;

	@Value("${aitrades.order.submit.buy.rabbitmq.exchange}")
	private String orderSubmitBuyExchangeName;

	@Value("${aitrades.order.submit.buy.rabbitmq.routingkey}")
	private String orderSubmitBuyRoutingkey;
	

	@Bean("orderSubmitBuyQueue")
	public Queue orderSubmitBuyQueue() {
		return new Queue(orderSubmitBuyQueueName, false);
	}

	@Bean("orderSubmitBuyDirectExchange")
	public DirectExchange orderSubmitBuyDirectExchange() {
		return new DirectExchange(orderSubmitBuyExchangeName);
	}

	@Bean("orderSubmitBuyBinding")
	public Binding postorderBinding() {
		return BindingBuilder.bind(orderSubmitBuyQueue())
							 .to(orderSubmitBuyDirectExchange())
							 .with(orderSubmitBuyRoutingkey);
	}

    @Bean
    public SimpleMessageListenerContainer orderSubmitBuyMessageListenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container =
                new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(orderSubmitBuyQueue());
        container.setConcurrentConsumers(5);
        container.setDefaultRequeueRejected(false);
        container.setAcknowledgeMode(AcknowledgeMode.NONE);
        return container;
    }

}