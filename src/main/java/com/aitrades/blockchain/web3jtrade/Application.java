package com.aitrades.blockchain.web3jtrade;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.tx.response.NoOpProcessor;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;

import com.aitrades.blockchain.web3jtrade.client.Web3jServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
 
@SpringBootApplication(scanBasePackages = { "com.aitrades.blockchain.web3jtrade" })
@EnableAsync
@EnableCaching
public class Application {

	private static final String ENDPOINT_WSS = "wss://eth-mainnet.ws.alchemyapi.io/v2/_KDa5W9WA_53y-f3KD0TUf-YYv0-QJ_7";
	private static final String BSC_ENDPOINT_WSS ="wss://holy-twilight-violet.bsc.quiknode.pro/9ccdc8c6748f92a972bc9c9c1b8b56de961c0fc6/";
	
	//wss://apis.ankr.com/wss/ec81f8a5c07c4660943c684b6fa7b102/4cd1cd0bb6b4e7809163a3de758926bc/binance/full/main

	private static final long WEBCLIENT_TIMEOUT= 20l;
	private static final String ETH_GAS_PRICE_ORACLE ="https://www.etherchain.org/api";
	@SuppressWarnings("unused")
	private static final String ETH_GAS_STATION ="https://data-api.defipulse.com/api/v1/egs/api/ethgasAPI.json?api-key=2d249b5b77ce8b5d20fdd6a6c09a5ac3a954981252730a2e26dcfbc4a41a";
	private static final String CREATE_ORDER_TASK_EXECUTOR_THREAD = "createOrder_task_executor_thread";

	private static final int _40 = 40;

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

	@Bean(name = "web3jClient")
	public Web3j web3J() {
		return Web3j.build(webSocketService());
	}

	@Bean(name = "web3bscjClient")
	public Web3j web3bscjClient() {
		return Web3j.build(webBSCSocketService());
	}
	
	@Bean(name = "webBSCSocketService")
	public WebSocketService webBSCSocketService() {
		Map<String, String> headers = new HashMap<>();
		headers.put("x-api-key", "eeda04fc-aec0-4606-944c-ae3a6292a1be");
		//WebSocketService webSocketService = new WebSocketService(new WebSocketClient(parseURI(BSC_ENDPOINT_WSS), headers), false);
		WebSocketService webSocketService = new WebSocketService(new WebSocketClient(parseURI(BSC_ENDPOINT_WSS)), false);
		try {
			webSocketService.connect();
		} catch (ConnectException e) {
			e.printStackTrace();
		}
		return webSocketService;
	}
	@Bean(name = "webSocketService")
	public WebSocketService webSocketService() {
		WebSocketService webSocketService = new WebSocketService(new WebSocketClient(parseURI(ENDPOINT_WSS)), false);
		try {
			webSocketService.connect();
		} catch (ConnectException e) {
			e.printStackTrace();
		}
		return webSocketService;
	}
    
	private static URI parseURI(String serverUrl) {
        try {
            return new URI(serverUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("Failed to parse URL: '%s'", serverUrl), e);
        }
    }
    
	@Bean
	public Parity Parity() {
		return Parity.build(webSocketService());
	}
	
	@Bean(name = "web3jServiceClient")
	@Primary
	public Web3jServiceClient web3jServiceClient(@Qualifier("web3jClient") final Web3j web3j,
												 final ObjectMapper objectMapper) {
		return new Web3jServiceClient(web3j, restTemplate(), objectMapper);
	}

	
	@Bean(name = "web3jBscServiceClient")
	public Web3jServiceClient web3jBscServiceClient(@Qualifier("web3bscjClient") final Web3j web3j,
												 final ObjectMapper objectMapper) {
		return new Web3jServiceClient(web3j, restTemplate(), objectMapper);
	}
	
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
	
	@Bean(name= "pollingTransactionReceiptProcessor")
	public PollingTransactionReceiptProcessor pollingTransactionReceiptProcessor() {
		return new PollingTransactionReceiptProcessor(web3J(), 4000, _40);
	}
	
	
	@Bean(name= "noOpProcessor")
	public NoOpProcessor noOpProcessor() {
		return new NoOpProcessor(web3J());
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
