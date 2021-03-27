package com.aitrades.blockchain.web3jtrade.domain;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class OrderEntity {
	
	private String orderSide;
	private String orderState;
	private String orderType;
	private LimitOrder limitOrder;
	private StopOrder stopOrder;
	private StopLimitOrder stopLimitOrder;
	private TrailingStopOrder trailingStopOrder;
	private LimitTrailingStop limitTrailingStop;
	
	private List<AdditionalProperty> addionalProperties;
	
	public String getOrderSide() {
		return orderSide;
	}

	public void setOrderSide(String orderSide) {
		this.orderSide = orderSide;
	}

	public String getOrderState() {
		return orderState;
	}

	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public List<AdditionalProperty> getAddionalProperties() {
		return addionalProperties;
	}

	public void setAddionalProperties(List<AdditionalProperty> addionalProperties) {
		this.addionalProperties = addionalProperties;
	}
	
	public LimitOrder getLimitOrder() {
		return limitOrder;
	}

	public void setLimitOrder(LimitOrder limitOrder) {
		this.limitOrder = limitOrder;
	}

	public StopOrder getStopOrder() {
		return stopOrder;
	}

	public void setStopOrder(StopOrder stopOrder) {
		this.stopOrder = stopOrder;
	}

	public StopLimitOrder getStopLimitOrder() {
		return stopLimitOrder;
	}

	public void setStopLimitOrder(StopLimitOrder stopLimitOrder) {
		this.stopLimitOrder = stopLimitOrder;
	}

	public TrailingStopOrder getTrailingStopOrder() {
		return trailingStopOrder;
	}

	public void setTrailingStopOrder(TrailingStopOrder trailingStopOrder) {
		this.trailingStopOrder = trailingStopOrder;
	}

	public LimitTrailingStop getLimitTrailingStop() {
		return limitTrailingStop;
	}

	public void setLimitTrailingStop(LimitTrailingStop limitTrailingStop) {
		this.limitTrailingStop = limitTrailingStop;
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
