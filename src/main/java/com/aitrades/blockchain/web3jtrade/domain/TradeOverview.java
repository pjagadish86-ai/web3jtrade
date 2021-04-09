package com.aitrades.blockchain.web3jtrade.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)	
@JsonInclude(Include.NON_NULL)
public class TradeOverview {

	private String id;
	private String orderDesc;
	private String orderSide;
	private String orderState;
	private String orderType;
	private String approvedHash;
	private String swappedHash;
	private String errorMessage;
	private String route;
	private BigDecimal executedPrice;
	private String contractIntreactedWith;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOrderDesc() {
		return orderDesc;
	}
	public void setOrderDesc(String orderDesc) {
		this.orderDesc = orderDesc;
	}
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
	public String getApprovedHash() {
		return approvedHash;
	}
	public void setApprovedHash(String approvedHash) {
		this.approvedHash = approvedHash;
	}
	public String getSwappedHash() {
		return swappedHash;
	}
	public void setSwappedHash(String swappedHash) {
		this.swappedHash = swappedHash;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getRoute() {
		return route;
	}
	public void setRoute(String route) {
		this.route = route;
	}
	public BigDecimal getExecutedPrice() {
		return executedPrice;
	}
	public void setExecutedPrice(BigDecimal executedPrice) {
		this.executedPrice = executedPrice;
	}
	
}
