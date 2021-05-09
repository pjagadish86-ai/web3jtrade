package com.aitrades.blockchain.web3jtrade.domain;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)	
@JsonInclude(Include.NON_NULL)
public class DexContractStaticCodeValue {

	private String id;
	private String dexChain; // ETH, BSC, ZIL, FTM
	private String dexName;  // ETH -> uniswap, sushi or BSC - pancake
	private String routerAddress; // router address
	private String factoryAddress; // factory address
	private String wrappedNativeAddress;
	private String usdNativeAddress;
	
	private List<AdditionalProperty> additionalProperties;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDexChain() {
		return dexChain;
	}

	public void setDexChain(String dexChain) {
		this.dexChain = dexChain;
	}

	public String getDexName() {
		return dexName;
	}

	public void setDexName(String dexName) {
		this.dexName = dexName;
	}

	public String getRouterAddress() {
		return routerAddress;
	}

	public void setRouterAddress(String routerAddress) {
		this.routerAddress = routerAddress;
	}

	public String getFactoryAddress() {
		return factoryAddress;
	}

	public void setFactoryAddress(String factoryAddress) {
		this.factoryAddress = factoryAddress;
	}

	public String getWrappedNativeAddress() {
		return wrappedNativeAddress;
	}

	public void setWrappedNativeAddress(String wrappedNativeAddress) {
		this.wrappedNativeAddress = wrappedNativeAddress;
	}

	public List<AdditionalProperty> getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(List<AdditionalProperty> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	public String getUsdNativeAddress() {
		return usdNativeAddress;
	}

	public void setUsdNativeAddress(String usdNativeAddress) {
		this.usdNativeAddress = usdNativeAddress;
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
