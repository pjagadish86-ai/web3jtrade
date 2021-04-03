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
public class RabbitMQOrderSubmitSellConfig {

	@Value("${aitrades.order.submit.sell.rabbitmq.queue}")
	String orderSubmitSellQueueName;

	@Value("${aitrades.order.submit.sell.rabbitmq.exchange}")
	String orderSubmitSellExchangeName;

	@Value("${aitrades.order.submit.sell.rabbitmq.routingkey}")
	private String orderSubmitSellRoutingkey;
	

	@Bean("orderSubmitSellQueue")
	public Queue orderSubmitSellQueue() {
		return new Queue(orderSubmitSellQueueName, false);
	}

	@Bean("orderSubmitSellDirectExchange")
	public DirectExchange orderSubmitSellDirectExchange() {
		return new DirectExchange(orderSubmitSellExchangeName);
	}

	@Bean("orderSubmitSellBinding")
	public Binding postorderBinding() {
		return BindingBuilder.bind(orderSubmitSellQueue())
							 .to(orderSubmitSellDirectExchange())
							 .with(orderSubmitSellRoutingkey);
	}
	
    @Bean
    public SimpleMessageListenerContainer orderSubmitSellMessageListenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container =
                new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(orderSubmitSellQueue());
        container.setConcurrentConsumers(5);
        container.setDefaultRequeueRejected(false);
        container.setAcknowledgeMode(AcknowledgeMode.NONE);
        return container;
    }

}