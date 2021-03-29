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
public class Gas {

	private String mode;
	private String value;
	private BigInteger valueBigInteger;
	private BigDecimal valueBigDecimal;
	private List<AdditionalProperty> AdditionalProperty;
	
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public BigInteger getValueBigInteger() {
		return valueBigInteger;
	}

	public void setValueBigInteger(BigInteger valueBigInteger) {
		this.valueBigInteger = valueBigInteger;
	}

	public BigDecimal getValueBigDecimal() {
		return valueBigDecimal;
	}

	public void setValueBigDecimal(BigDecimal valueBigDecimal) {
		this.valueBigDecimal = valueBigDecimal;
	}

	public List<AdditionalProperty> getAdditionalProperty() {
		return AdditionalProperty;
	}

	public void setAdditionalProperty(List<AdditionalProperty> AdditionalProperty) {
		this.AdditionalProperty = AdditionalProperty;
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
