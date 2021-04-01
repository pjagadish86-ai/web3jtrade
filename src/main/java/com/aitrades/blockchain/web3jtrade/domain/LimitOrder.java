package com.aitrades.blockchain.web3jtrade.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
@JsonIgnoreProperties(ignoreUnknown = true)	
@JsonInclude(Include.NON_NULL)
public class LimitOrder {

	private String limitPrice;
	private BigInteger limitPriceBigInteger;
	private BigDecimal limitPriceBigDecimal;
	
	private List<AdditionalProperty> addionalProperties;

	public String getLimitPrice() {
		return limitPrice;
	}

	public void setLimitPrice(String limitPrice) {
		this.limitPrice = limitPrice;
	}

	public BigInteger getLimitPriceBigInteger() {
		return limitPriceBigInteger;
	}

	public void setLimitPriceBigInteger(BigInteger limitPriceBigInteger) {
		this.limitPriceBigInteger = limitPriceBigInteger;
	}

	public BigDecimal getLimitPriceBigDecimal() {
		return limitPriceBigDecimal;
	}

	public void setLimitPriceBigDecimal(BigDecimal limitPriceBigDecimal) {
		this.limitPriceBigDecimal = limitPriceBigDecimal;
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
