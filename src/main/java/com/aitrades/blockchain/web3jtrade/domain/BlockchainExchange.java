package com.aitrades.blockchain.web3jtrade.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)	
@JsonInclude(Include.NON_NULL)
public class BlockchainExchange {

	private String id;
	private String blockchainName;
	private String exchangeName;
	private String nativeCoinTicker;

	@JsonIgnore
	private boolean enabled;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBlockchainName() {
		return blockchainName;
	}
	public void setBlockchainName(String blockchainName) {
		this.blockchainName = blockchainName;
	}
	public String getExchangeName() {
		return exchangeName;
	}
	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
	}
	
	public String getNativeCoinTicker() {
		return nativeCoinTicker;
	}
	public void setNativeCoinTicker(String nativeCoinTicker) {
		this.nativeCoinTicker = nativeCoinTicker;
	}
	@JsonIgnore
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
}
