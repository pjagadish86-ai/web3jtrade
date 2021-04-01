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
public class LimitTrailingStop {

	private String limitPrice;
	private BigInteger limitPriceBigInteger;
	private BigDecimal limitPriceBigDecimal;
	
	private String trailingStopPercent;
	private BigInteger trailingStopPercentBigInteger;
	private BigDecimal trailingStopPercentBigDecimal;
	
	private boolean isLimitTrailingStopPriceMet;
	private String adjustedtrailingStopPrice;
	private BigInteger adjustedtrailingStopPriceAsBigInteger;
	private BigDecimal adjustedtrailingStopPriceAsBigDecimal;
	
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

	public boolean isLimitTrailingStopPriceMet() {
		return isLimitTrailingStopPriceMet;
	}

	public void setLimitTrailingStopPriceMet(boolean isLimitTrailingStopPriceMet) {
		this.isLimitTrailingStopPriceMet = isLimitTrailingStopPriceMet;
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
