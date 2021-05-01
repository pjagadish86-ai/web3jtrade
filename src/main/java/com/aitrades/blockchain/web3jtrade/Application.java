package com.aitrades.blockchain.web3jtrade;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
 
@SpringBootApplication(scanBasePackages = { "com.aitrades.blockchain.web3jtrade" })
@EnableAsync
@EnableCaching
public class Application {


	private static final long WEBCLIENT_TIMEOUT= 20l;
	private static final String ETH_GAS_PRICE_ORACLE ="https://www.etherchain.org/api";
	@SuppressWarnings("unused")
	private static final String ETH_GAS_STATION ="https://data-api.defipulse.com/api/v1/egs/api/ethgasAPI.json?api-key=2d249b5b77ce8b5d20fdd6a6c09a5ac3a954981252730a2e26dcfbc4a41a";
	private static final String CREATE_ORDER_TASK_EXECUTOR_THREAD = "createOrder_task_executor_thread";

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	public Executor executor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(20);
		executor.setThreadNamePrefix(CREATE_ORDER_TASK_EXECUTOR_THREAD);
		executor.initialize();
		return executor;
	}
	
	@Resource(name="bscPriceHttpClient")
	public CloseableHttpClient closeableHttpClient;

    
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean(name = "gasWebClient")
	public WebClient getWebClient(@Qualifier("externalHttpClientCalls") HttpClient externalHttpClientCalls) {
		return WebClient.builder()
					    .baseUrl(ETH_GAS_PRICE_ORACLE)
					    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					    .clientConnector(new ReactorClientHttpConnector(externalHttpClientCalls))
					    .build();
	}

	@Bean(name = "externalHttpClientCalls")
	public HttpClient getHttpClient() {
		return HttpClient.create()
			    		 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 50000)
			    		 .responseTimeout(Duration.ofMillis(50000))
			    		 .doOnConnected(conn -> 
	    		  						conn.addHandlerLast(new ReadTimeoutHandler(WEBCLIENT_TIMEOUT, TimeUnit.SECONDS))
	    		  							.addHandlerLast(new WriteTimeoutHandler(WEBCLIENT_TIMEOUT, TimeUnit.SECONDS)));
	}
	
	
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
	
	@Bean(name = "orderSubmitRabbitTemplate")
	public AmqpTemplate postorderRabbitTemplate(ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}
	
	@Bean(name ="graphHqlPriceHttpClient")
	public CloseableHttpClient uniswapPriceHttpClient() {
		return HttpClients.createMinimal();	
	}
	
	@Bean(name ="bscPriceHttpClient")
	public CloseableHttpClient bscPriceHttpClient() {
		return HttpClients.createMinimal();	
	}
	
}
