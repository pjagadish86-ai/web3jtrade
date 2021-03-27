package com.aitrades.blockchain.web3jtrade.domain;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
@JsonInclude(Include.NON_EMPTY)
public class EventState {
	
	private Boolean pairCreated;
	private Boolean reserves;
	private Boolean liquidity;
	private List<AdditionalProperty> addionalProperties;
	
	public boolean isPairCreated() {
		return pairCreated;
	}

	public void setPairCreated(boolean pairCreated) {
		this.pairCreated = pairCreated;
	}

	public boolean isReserves() {
		return reserves;
	}

	public void setReserves(boolean reserves) {
		this.reserves = reserves;
	}

	public boolean isLiquidity() {
		return liquidity;
	}

	public void setLiquidity(boolean liquidity) {
		this.liquidity = liquidity;
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
