package com.aitrades.blockchain.web3jtrade.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class PairData {
	
	private Ticker pairAddress;
	
	public Ticker getPairAddress() {
		return pairAddress;
	}

	public void setPairAddress(Ticker pairAddress) {
		this.pairAddress = pairAddress;
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
