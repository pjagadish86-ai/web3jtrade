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
public class Slipage {

	private String slipagePercent;
	private BigInteger slipagePercentAsBigInteger;
	private BigInteger slipageInBips;
	private double slipageInBipsInDouble;
	private BigDecimal slipapagePercentAsBigDecimal;
	
	private List<AdditionalProperty> addionalProperties;

	public String getSlipagePercent() {
		return slipagePercent;
	}
	
	public double getSlipageInBipsInDouble() {
		return slipageInBipsInDouble;
	}


	public void setSlipageInBipsInDouble(double slipageInBipsInDouble) {
		this.slipageInBipsInDouble = slipageInBipsInDouble;
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
	
	public BigInteger getSlipageInBips() {
		return slipageInBips;
	}

	public void setSlipageInBips(BigInteger slipageInBips) {
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
