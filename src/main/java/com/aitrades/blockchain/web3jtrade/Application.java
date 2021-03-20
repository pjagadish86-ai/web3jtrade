package com.aitrades.blockchain.web3jtrade;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

import com.aitrades.blockchain.web3jtrade.client.Web3jServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
 
@SpringBootApplication(scanBasePackages = { "com.aitrades.blockchain.web3jtrade" })
public class Application {

	private static final String ENDPOINT_WSS = "wss://icy-aged-wood.quiknode.pro/13d44d88a3379583681c7871538ad5fe4e6727c7/";
	
	private static final long WEBCLIENT_TIMEOUT= 20l;
	private static final String ETH_GAS_PRICE_ORACLE ="https://www.etherchain.org/api";
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

	@Bean(name = "web3jClient")
	public Web3j web3J() {
		return Web3j.build(webSocketService());
	}

	@Bean(name = "web3bscjClient")
	public Web3j web3bscjClient() {
		return Web3j.build(webSocketService());
	}
	
	@Bean
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
	public Web3jServiceClient web3jServiceClient(@Qualifier("web3jClient") final Web3j web3j,
												 final ObjectMapper objectMapper) {
		return new Web3jServiceClient(web3j, restTemplate(), objectMapper);
	}

	
	@Bean(name = "web3jBscServiceClient")
	public Web3jServiceClient web3jBscServiceClient(@Qualifier("web3bscjClient") final Web3j web3j,
												 final ObjectMapper objectMapper) {
		return new Web3jServiceClient(web3j, restTemplate(), objectMapper);
	}
	
	@Bean(name = "credentials")
	public Credentials credentials() {
		return Credentials.create("0x03f150fde140188e4de36b962072033c533c297aa8f407d81da96ec598127c44");
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
			    		 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
			    		 .responseTimeout(Duration.ofMillis(5000))
			    		 .doOnConnected(conn -> 
	    		  						conn.addHandlerLast(new ReadTimeoutHandler(WEBCLIENT_TIMEOUT, TimeUnit.MILLISECONDS))
	    		  							.addHandlerLast(new WriteTimeoutHandler(WEBCLIENT_TIMEOUT, TimeUnit.MILLISECONDS)));
	}
	
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
	
	//TODO: async rabitamq
	@Bean("orderSubmitRabbitTemplate")
	public AmqpTemplate postorderRabbitTemplate(ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}
}
