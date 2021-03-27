package com.aitrades.blockchain.web3jtrade.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class TickerEntity {
	
	private Ticker ticker;
	private String amount;
	private BigInteger amountAsBigInteger;
	private BigDecimal amountAsBigDecimal;
	private List<AdditionalProperty> addionalProperties;

	public Ticker getTicker() {
		return ticker;
	}

	public void setTicker(Ticker ticker) {
		this.ticker = ticker;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public BigInteger getAmountAsBigInteger() {
		return amountAsBigInteger;
	}

	public void setAmountAsBigInteger(BigInteger amountAsBigInteger) {
		this.amountAsBigInteger = amountAsBigInteger;
	}

	public BigDecimal getAmountAsBigDecimal() {
		return amountAsBigDecimal;
	}

	public void setAmountAsBigDecimal(BigDecimal amountAsBigDecimal) {
		this.amountAsBigDecimal = amountAsBigDecimal;
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
