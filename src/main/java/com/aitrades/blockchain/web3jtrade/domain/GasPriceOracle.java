package com.aitrades.blockchain.web3jtrade.domain;

import java.math.BigInteger;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class GasPriceOracle {

	private BigInteger safeLow;
	private BigInteger standard;
	private BigInteger fast;
	private BigInteger fastest;
	
	public BigInteger getSafeLow() {
		return safeLow;
	}

	public void setSafeLow(BigInteger safeLow) {
		this.safeLow = safeLow;
	}

	public BigInteger getStandard() {
		return standard;
	}

	public void setStandard(BigInteger standard) {
		this.standard = standard;
	}

	public BigInteger getFast() {
		return fast;
	}

	public void setFast(BigInteger fast) {
		this.fast = fast;
	}

	public BigInteger getFastest() {
		return fastest;
	}

	public void setFastest(BigInteger fastest) {
		this.fastest = fastest;
	}

	@JsonIgnore
	public BigInteger getUltraRapid() {
		return getFastest().add(getStandard().multiply(new BigInteger("2")));
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
