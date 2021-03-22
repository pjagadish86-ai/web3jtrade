package com.aitrades.blockchain.web3jtrade;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aitrades.blockchain.web3jtrade.domain.price.EthPrice;
import com.aitrades.blockchain.web3jtrade.domain.price.PairPrice;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

@Configuration
public class JsonMapperConfig {

	
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
	
	@Bean(name= "pairPriceObjectReader")
	public ObjectReader pairPriceObjectReader() {
		return objectMapper().readerFor(PairPrice.class);
	}
	
	@Bean(name= "ethPriceObjectReader")
	public ObjectReader ethPriceObjectReader() {
		return objectMapper().readerFor(EthPrice.class);
	}
}
