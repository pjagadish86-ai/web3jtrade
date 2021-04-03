package com.aitrades.blockchain.web3jtrade.integration.snipe.mq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnipeOrderRetryPublisher {

	@Value("${aitrades.order.submit.snipe.rabbitmq.queue}")
	String snipeOrderQueueName;

	@Value("${aitrades.order.submit.snipe.rabbitmq.exchange}")
	String snipeOrderExchangeName;

	@Value("${aitrades.order.submit.snipe.rabbitmq.routingkey}")
	private String snipeOrderRoutingkey;
	
	@Autowired
	private MessageConverter jsonMessageConverter;

	@Bean(name="snipeOrderQueue")
	public Queue snipeOrderQueue() {
		return new Queue(snipeOrderQueueName, false);
	}

	@Bean(name="snipeOrderDirectExchange")
	public DirectExchange snipeOrderDirectExchange() {
		return new DirectExchange(snipeOrderExchangeName);
	}

	@Bean(name="snipeOrderBinding")
	public Binding snipeOrderBinding() {
		return BindingBuilder.bind(snipeOrderQueue())
							 .to(snipeOrderDirectExchange())
							 .with(snipeOrderRoutingkey);
	}

	@Bean(name = "snipeOrderRabbitTemplate")
	public AmqpTemplate snipeOrderRabbitTemplate(ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter);
		rabbitTemplate.setExchange(snipeOrderExchangeName);
		rabbitTemplate.setDefaultReceiveQueue(snipeOrderQueueName);
		rabbitTemplate.setRoutingKey(snipeOrderRoutingkey);
		return rabbitTemplate;
	}
}
