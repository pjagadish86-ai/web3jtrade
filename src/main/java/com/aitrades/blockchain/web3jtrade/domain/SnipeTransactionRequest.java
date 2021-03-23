package com.aitrades.blockchain.web3jtrade.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.web3j.crypto.Credentials;

public class SnipeTransactionRequest {

	public static final BigInteger MAX_AMOUNT_APPROVE = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
	
	@Id
	private String id;
	
	private String route;
	
	private String fromAddress;
	private String toAddress;
	
	private String pairAddress;
	
	private BigInteger inputTokenValueAmountAsBigInteger;
	private BigInteger ouputTokenValueAmounttAsBigInteger;
	
	private BigDecimal inputTokenValueAmountAsBigDecimal;
	private BigDecimal ouputTokenValueAmounttAsBigDecimal;
	
	private WalletInfo walletInfo;
	
	private BigInteger gasPrice;
	private BigInteger gasLimit;
	
	private List<String> memoryPath;
	
	private boolean hasFee;
	private String gasMode;
	
	private BigDecimal slipage;
	
	private long deadLine;
	
	private String orderType;
	
	private Instant createdDateTime = Instant.now();
	
	private Instant updatedDateTime;
	
	private boolean snipe;

	public SnipeTransactionRequest() {
	}
	
	public WalletInfo getWalletInfo() {
		return walletInfo;
	}

	public void setWalletInfo(WalletInfo walletInfo) {
		this.walletInfo = walletInfo;
	}

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

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public String getPairAddress() {
		return pairAddress;
	}

	public void setPairAddress(String pairAddress) {
		this.pairAddress = pairAddress;
	}

	public BigInteger getInputTokenValueAmountAsBigInteger() {
		return inputTokenValueAmountAsBigInteger;
	}

	public void setInputTokenValueAmountAsBigInteger(BigInteger inputTokenValueAmountAsBigInteger) {
		this.inputTokenValueAmountAsBigInteger = inputTokenValueAmountAsBigInteger;
	}

	public BigInteger getOuputTokenValueAmounttAsBigInteger() {
		return ouputTokenValueAmounttAsBigInteger;
	}

	public void setOuputTokenValueAmounttAsBigInteger(BigInteger ouputTokenValueAmounttAsBigInteger) {
		this.ouputTokenValueAmounttAsBigInteger = ouputTokenValueAmounttAsBigInteger;
	}

	public BigDecimal getInputTokenValueAmountAsBigDecimal() {
		return inputTokenValueAmountAsBigDecimal;
	}

	public void setInputTokenValueAmountAsBigDecimal(BigDecimal inputTokenValueAmountAsBigDecimal) {
		this.inputTokenValueAmountAsBigDecimal = inputTokenValueAmountAsBigDecimal;
	}

	public void setCreatedDateTime(Instant createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public BigDecimal getOuputTokenValueAmounttAsBigDecimal() {
		return ouputTokenValueAmounttAsBigDecimal;
	}

	public void setOuputTokenValueAmounttAsBigDecimal(BigDecimal ouputTokenValueAmounttAsBigDecimal) {
		this.ouputTokenValueAmounttAsBigDecimal = ouputTokenValueAmounttAsBigDecimal;
	}

	public BigInteger getGasPrice() {
		return gasPrice;
	}

	public void setGasPrice(BigInteger gasPrice) {
		this.gasPrice = gasPrice;
	}

	public BigInteger getGasLimit() {
		return gasLimit;
	}

	public void setGasLimit(BigInteger gasLimit) {
		this.gasLimit = gasLimit;
	}

	public List<String> getMemoryPath() {
		return memoryPath;
	}

	public void setMemoryPath(List<String> memoryPath) {
		this.memoryPath = memoryPath;
	}

	public boolean isHasFee() {
		return hasFee;
	}

	public void setHasFee(boolean hasFee) {
		this.hasFee = hasFee;
	}

	public String getGasMode() {
		return gasMode;
	}

	public void setGasMode(String gasMode) {
		this.gasMode = gasMode;
	}

	public BigDecimal getSlipage() {
		return slipage;
	}

	public void setSlipage(BigDecimal slipage) {
		this.slipage = slipage;
	}

	public long getDeadLine() {
		return deadLine;
	}

	public void setDeadLine(long deadLine) {
		this.deadLine = deadLine;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	
	public Instant getCreatedDateTime() {
		return createdDateTime;
	}

	public Instant getUpdatedDateTime() {
		return updatedDateTime;
	}

	public void setUpdatedDateTime(Instant updatedDateTime) {
		this.updatedDateTime = updatedDateTime;
	}
	
	public boolean isSnipe() {
		return snipe;
	}

	public void setSnipe(boolean snipe) {
		this.snipe = snipe;
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

	public Credentials getCredentials() {
		return Credentials.create(getWalletInfo().getPrivateKey(), getWalletInfo().getPublicKey());
	}

}
