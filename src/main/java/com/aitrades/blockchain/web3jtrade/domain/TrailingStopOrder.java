package com.aitrades.blockchain.web3jtrade.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class TrailingStopOrder {

	private String trailingStopPercent;
	private BigInteger trailingStopPercentBigInteger;
	private BigDecimal trailingStopPercentBigDecimal;
	
	private String adjustedtrailingStopPrice;
	private BigInteger adjustedtrailingStopPriceAsBigInteger;
	private BigDecimal adjustedtrailingStopPriceAsBigDecimal;
	
	private List<AdditionalProperty> addionalProperties;

	public String getTrailingStopPercent() {
		return trailingStopPercent;
	}

	public void setTrailingStopPercent(String trailingStopPercent) {
		this.trailingStopPercent = trailingStopPercent;
	}

	public BigInteger getTrailingStopPercentBigInteger() {
		return trailingStopPercentBigInteger;
	}

	public void setTrailingStopPercentBigInteger(BigInteger trailingStopPercentBigInteger) {
		this.trailingStopPercentBigInteger = trailingStopPercentBigInteger;
	}

	public BigDecimal getTrailingStopPercentBigDecimal() {
		return trailingStopPercentBigDecimal;
	}

	public void setTrailingStopPercentBigDecimal(BigDecimal trailingStopPercentBigDecimal) {
		this.trailingStopPercentBigDecimal = trailingStopPercentBigDecimal;
	}

	public List<AdditionalProperty> getAddionalProperties() {
		return addionalProperties;
	}

	public void setAddionalProperties(List<AdditionalProperty> addionalProperties) {
		this.addionalProperties = addionalProperties;
	}

	public String getAdjustedtrailingStopPrice() {
		return adjustedtrailingStopPrice;
	}

	public void setAdjustedtrailingStopPrice(String adjustedtrailingStopPrice) {
		this.adjustedtrailingStopPrice = adjustedtrailingStopPrice;
	}

	public BigInteger getAdjustedtrailingStopPriceAsBigInteger() {
		return adjustedtrailingStopPriceAsBigInteger;
	}

	public void setAdjustedtrailingStopPriceAsBigInteger(BigInteger adjustedtrailingStopPriceAsBigInteger) {
		this.adjustedtrailingStopPriceAsBigInteger = adjustedtrailingStopPriceAsBigInteger;
	}

	public BigDecimal getAdjustedtrailingStopPriceAsBigDecimal() {
		return adjustedtrailingStopPriceAsBigDecimal;
	}

	public void setAdjustedtrailingStopPriceAsBigDecimal(BigDecimal adjustedtrailingStopPriceAsBigDecimal) {
		this.adjustedtrailingStopPriceAsBigDecimal = adjustedtrailingStopPriceAsBigDecimal;
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
