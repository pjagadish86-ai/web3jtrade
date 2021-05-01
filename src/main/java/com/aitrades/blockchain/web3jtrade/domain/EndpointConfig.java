package com.aitrades.blockchain.web3jtrade.domain;

public class EndpointConfig {
	 
	protected String id;
	protected String endpointUrl;
	protected String username;
	protected String blockchain;
	protected String exchange;
	protected String providerName;
	protected boolean enabled;
	protected String rpcProtocol;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getProviderName() {
		return providerName;
	}
	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}
	public String getEndpointUrl() {
		return endpointUrl;
	}
	public void setEndpointUrl(String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getBlockchain() {
		return blockchain;
	}
	public void setBlockchain(String blockchain) {
		this.blockchain = blockchain;
	}
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	public String getRpcProtocol() {
		return rpcProtocol;
	}
	public void setRpcProtocol(String rpcProtocol) {
		this.rpcProtocol = rpcProtocol;
	}
	
}
