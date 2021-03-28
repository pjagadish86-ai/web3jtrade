package com.aitrades.blockchain.web3jtrade.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Slipage {

	private String slipagePercent;
	private BigInteger slipagePercentAsBigInteger;
	private BigDecimal slipageInBips;
	private BigDecimal slipapagePercentAsBigDecimal;
	
	private List<AdditionalProperty> addionalProperties;

	public String getSlipagePercent() {
		return slipagePercent;
	}

	public void setSlipagePercent(String slipagePercent) {
		this.slipagePercent = slipagePercent;
	}

	public BigInteger getSlipagePercentAsBigInteger() {
		return slipagePercentAsBigInteger;
	}

	public void setSlipagePercentAsBigInteger(BigInteger slipagePercentAsBigInteger) {
		this.slipagePercentAsBigInteger = slipagePercentAsBigInteger;
	}

	public BigDecimal getSlipapagePercentAsBigDecimal() {
		return slipapagePercentAsBigDecimal;
	}

	public void setSlipapagePercentAsBigDecimal(BigDecimal slipapagePercentAsBigDecimal) {
		this.slipapagePercentAsBigDecimal = slipapagePercentAsBigDecimal;
	}

	public List<AdditionalProperty> getAddionalProperties() {
		return addionalProperties;
	}

	public void setAddionalProperties(List<AdditionalProperty> addionalProperties) {
		this.addionalProperties = addionalProperties;
	}
	
	public BigDecimal getSlipageInBips() {
		if(this.slipageInBips.compareTo(BigDecimal.ZERO) <= 0) {
			slipageInBips = BigDecimal.valueOf(0.05);
		}
		return slipageInBips;
	}

	public void setSlipageInBips(BigDecimal slipageInBips) {
		this.slipageInBips = slipageInBips;
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
