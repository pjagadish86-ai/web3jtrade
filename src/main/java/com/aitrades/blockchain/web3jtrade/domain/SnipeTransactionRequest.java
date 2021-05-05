package com.aitrades.blockchain.web3jtrade.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
public class SnipeTransactionRequest {

	public static final BigInteger MAX_AMOUNT_APPROVE = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
	
	@Id
	private String id;
	
	private String route;
	
	private String fromAddress;
	private String toAddress;
	private String toAddressDecimals;
	
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
	
	private Double slipageInDouble;
	
	private long deadLine;
	
	private String orderType;
	
	private String orderSide;
	
	private String createdDateTime;
	
	private String updatedDateTime;
	
	private boolean snipe;
	
	private boolean isPreApproved;
	
	private String approvedHash;
	
	private String swappedHash;
	
	private boolean hasApproved;
	
	private boolean isFeeEligible;
	
	private String snipeStatus;
	
	private String doSell;
	private String doSellPercentage;
	
	private AtomicInteger atomicInteger;
	private String read;
	
	private String snipeExpired;
	private String errorMessage;
	private BigInteger expectedOutPutToken;
	private BigInteger liquidityQuantity;
	private Reserves reserves;
	private AuditInformation auditInformation;
	private boolean isBusdPair;
	
	private BigInteger earliestBlockNbr;
	
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

	public BigDecimal getOuputTokenValueAmounttAsBigDecimal() {
		return ouputTokenValueAmounttAsBigDecimal;
	}

	public void setOuputTokenValueAmounttAsBigDecimal(BigDecimal ouputTokenValueAmounttAsBigDecimal) {
		this.ouputTokenValueAmounttAsBigDecimal = ouputTokenValueAmounttAsBigDecimal;
	}

	public WalletInfo getWalletInfo() {
		return walletInfo;
	}

	public void setWalletInfo(WalletInfo walletInfo) {
		this.walletInfo = walletInfo;
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
	
	public String getApprovedHash() {
		return approvedHash;
	}

	public void setApprovedHash(String approvedHash) {
		this.approvedHash = approvedHash;
	}

	public boolean isHasApproved() {
		return hasApproved;
	}

	public void setHasApproved(boolean hasApproved) {
		this.hasApproved = hasApproved;
	}

	public long getDeadLine() {
		if(this.deadLine <= 0) {
			this.deadLine = 300l;
		}
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

	public String getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(String createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public String getUpdatedDateTime() {
		return updatedDateTime;
	}

	public void setUpdatedDateTime(String updatedDateTime) {
		this.updatedDateTime = updatedDateTime;
	}

	public boolean isSnipe() {
		return snipe;
	}
	
	public String getDoSell() {
		return doSell;
	}

	public void setDoSell(String doSell) {
		this.doSell = doSell;
	}

	public String getDoSellPercentage() {
		return doSellPercentage;
	}

	public void setDoSellPercentage(String doSellPercentage) {
		this.doSellPercentage = doSellPercentage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@JsonIgnore
	public boolean hasSniped() {
		return isSnipe();
	}

	public void setSnipe(boolean snipe) {
		this.snipe = snipe;
	}

	public String getSnipeStatus() {
		return snipeStatus;
	}

	public void setSnipeStatus(String snipeStatus) {
		this.snipeStatus = snipeStatus;
	}

	public static BigInteger getMaxAmountApprove() {
		return MAX_AMOUNT_APPROVE;
	}
	
	public boolean isPreApproved() {
		return isPreApproved;
	}

	public void setPreApproved(boolean isPreApproved) {
		this.isPreApproved = isPreApproved;
	}

	public boolean isFeeEligible() {
		return isFeeEligible;
	}

	public void setFeeEligible(boolean isFeeEligible) {
		this.isFeeEligible = isFeeEligible;
	}
	
	public String getOrderSide() {
		return orderSide;
	}

	public void setOrderSide(String orderSide) {
		this.orderSide = orderSide;
	}

	public String getRead() {
		return read;
	}

	public void setRead(String read) {
		this.read = read;
	}
	
	public Double getSlipageInDouble() {
		return slipageInDouble;
	}

	public void setSlipageInDouble(Double slipageInDouble) {
		this.slipageInDouble = slipageInDouble;
	}

	public AtomicInteger getAtomicInteger() {
		return atomicInteger;
	}

	public void setAtomicInteger(AtomicInteger atomicInteger) {
		this.atomicInteger = atomicInteger;
	}

	public String getSnipeExpired() {
		return snipeExpired;
	}

	public void setSnipeExpired(String snipeExpired) {
		this.snipeExpired = snipeExpired;
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

	public String getSwappedHash() {
		return swappedHash;
	}

	public void setSwappedHash(String swappedHash) {
		this.swappedHash = swappedHash;
	}

	public Reserves getReserves() {
		return reserves;
	}

	public void setReserves(Reserves reserves) {
		this.reserves = reserves;
	}

	public AuditInformation getAuditInformation() {
		return auditInformation;
	}

	public void setAuditInformation(AuditInformation auditInformation) {
		this.auditInformation = auditInformation;
	}

	public BigInteger getExpectedOutPutToken() {
		return expectedOutPutToken;
	}

	public void setExpectedOutPutToken(BigInteger expectedOutPutToken) {
		this.expectedOutPutToken = expectedOutPutToken;
	}

	public BigInteger getLiquidityQuantity() {
		return liquidityQuantity;
	}

	public void setLiquidityQuantity(BigInteger liquidityQuantity) {
		this.liquidityQuantity = liquidityQuantity;
	}

	public String getToAddressDecimals() {
		return toAddressDecimals;
	}

	public void setToAddressDecimals(String toAddressDecimals) {
		this.toAddressDecimals = toAddressDecimals;
	}

	public boolean isBusdPair() {
		return isBusdPair;
	}

	public void setBusdPair(boolean isBusdPair) {
		this.isBusdPair = isBusdPair;
	}

	public BigInteger getEarliestBlockNbr() {
		return earliestBlockNbr;
	}

	public void setEarliestBlockNbr(BigInteger earliestBlockNbr) {
		this.earliestBlockNbr = earliestBlockNbr;
	}
	
}
