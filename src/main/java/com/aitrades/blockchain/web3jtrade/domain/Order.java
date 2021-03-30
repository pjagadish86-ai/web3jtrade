package com.aitrades.blockchain.web3jtrade.domain;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.web3j.crypto.Credentials;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
@JsonIgnoreProperties(ignoreUnknown = true)	
@JsonInclude(Include.NON_NULL)
public class Order {

	@Id
	private String id;
	private String route;
	private WalletInfo walletInfo;
	private TickerEntity from;
	private TickerEntity to;
	private String gasMode;
	private Gas gasPrice;
	private Gas gasLimit;
	private Slipage slippage;
	private PairData pairData;
	private OrderEntity orderEntity;
	private EventState eventState;
	private List<AdditionalProperty> AdditionalProperty;
	private Integer orderCode;
	private String approvedHash;
	private String swappedHash;
	private String read;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getRoute() {
		return route;
	}
	public void setRoute(String route) {
		this.route = route;
	}
	
	public WalletInfo getWalletInfo() {
		return walletInfo;
	}
	public void setWalletInfo(WalletInfo walletInfo) {
		this.walletInfo = walletInfo;
	}
	public TickerEntity getFrom() {
		return from;
	}
	public void setFrom(TickerEntity from) {
		this.from = from;
	}
	public TickerEntity getTo() {
		return to;
	}
	public void setTo(TickerEntity to) {
		this.to = to;
	}
	public Gas getGasPrice() {
		return gasPrice;
	}
	public void setGasPrice(Gas gasPrice) {
		this.gasPrice = gasPrice;
	}
	public Gas getGasLimit() {
		return gasLimit;
	}
	public void setGasLimit(Gas gasLimit) {
		this.gasLimit = gasLimit;
	}

	public Slipage getSlippage() {
		return slippage;
	}
	public void setSlippage(Slipage slippage) {
		this.slippage = slippage;
	}
	public PairData getPairData() {
		return pairData;
	}
	public void setPairData(PairData pairData) {
		this.pairData = pairData;
	}
	public OrderEntity getOrderEntity() {
		return orderEntity;
	}
	public void setOrderEntity(OrderEntity orderEntity) {
		this.orderEntity = orderEntity;
	}
	public EventState getEventState() {
		return eventState;
	}
	public void setEventState(EventState eventState) {
		this.eventState = eventState;
	}
	public List<AdditionalProperty> getAdditionalProperty() {
		return AdditionalProperty;
	}
	public void setAdditionalProperty(List<AdditionalProperty> AdditionalProperty) {
		this.AdditionalProperty = AdditionalProperty;
	}
	public Integer getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(Integer orderCode) {
		this.orderCode = orderCode;
	}
	
	public String getGasMode() {
		return gasMode;
	}
	public void setGasMode(String gasMode) {
		this.gasMode = gasMode;
	}
	
	public String getApprovedHash() {
		return approvedHash;
	}
	public void setApprovedHash(String approvedHash) {
		this.approvedHash = approvedHash;
	}
	
	public String getRead() {
		return read;
	}
	public void setRead(String read) {
		this.read = read;
	}
	
	public String getSwappedHash() {
		return swappedHash;
	}
	public void setSwappedHash(String swappedHash) {
		this.swappedHash = swappedHash;
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
	@JsonIgnore
	public Credentials getCredentials() {
		return Credentials.create(getWalletInfo().getPrivateKey());
	}
}
