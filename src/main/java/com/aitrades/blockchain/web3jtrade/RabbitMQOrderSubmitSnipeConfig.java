package com.aitrades.blockchain.web3jtrade;

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
public class RabbitMQOrderSubmitSnipeConfig {

	@Value("${aitrades.order.submit.snipe.rabbitmq.queue}")
	String orderSubmitSnipeQueueName;

	@Value("${aitrades.order.submit.snipe.rabbitmq.exchange}")
	String orderSubmitSnipeExchangeName;

	@Value("${aitrades.order.submit.snipe.rabbitmq.routingkey}")
	private String orderSubmitSnipeRoutingkey;
	

	@Bean("orderSubmitSnipeQueue")
	public Queue orderSubmitSnipeQueue() {
		return new Queue(orderSubmitSnipeQueueName, false);
	}

	@Bean("orderSubmitSnipeDirectExchange")
	public DirectExchange orderSubmitSnipeDirectExchange() {
		return new DirectExchange(orderSubmitSnipeExchangeName);
	}

	@Bean("orderSubmitSnipeBinding")
	public Binding postorderBinding() {
		return BindingBuilder.bind(orderSubmitSnipeQueue())
							 .to(orderSubmitSnipeDirectExchange())
							 .with(orderSubmitSnipeRoutingkey);
	}

    @Bean
    public SimpleMessageListenerContainer orderSubmitSnipeMessageListenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container =
                new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(orderSubmitSnipeQueue());
        container.setConcurrentConsumers(1);
        container.setDefaultRequeueRejected(true);
//        container.setAdviceChain(new Advice[]{interceptor()});
        return container;
    }

}