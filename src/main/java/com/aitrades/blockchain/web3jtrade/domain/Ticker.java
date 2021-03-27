package com.aitrades.blockchain.web3jtrade.domain;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Ticker {

	private String symbol;
	private String address;
	private String decimals;
	private List<AdditionalProperty> addionalProperties;
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getDecimals() {
		return decimals;
	}
	public void setDecimals(String decimals) {
		this.decimals = decimals;
	}
	
	public List<AdditionalProperty> getAddionalProperties() {
		return addionalProperties;
	}
	public void setAddionalProperties(List<AdditionalProperty> addionalProperties) {
		this.addionalProperties = addionalProperties;
	}
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
