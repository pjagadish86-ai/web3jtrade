package com.aitrades.blockchain.web3jtrade.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)	
@JsonInclude(Include.NON_NULL)
public class LiquidityAvailable {
	
	private String contractAddress;
	private String route;
	private String chain;
	private boolean isLiquidityAdded;
	
	public String getContractAddress() {
		return contractAddress;
	}
	public void setContractAddress(String contractAddress) {
		this.contractAddress = contractAddress;
	}
	public String getRoute() {
		return route;
	}
	public void setRoute(String route) {
		this.route = route;
	}
	public String getChain() {
		return chain;
	}
	public void setChain(String chain) {
		this.chain = chain;
	}
	public boolean isLiquidityAdded() {
		return isLiquidityAdded;
	}
	public void setLiquidityAdded(boolean isLiquidityAdded) {
		this.isLiquidityAdded = isLiquidityAdded;
	}
	
	

}
