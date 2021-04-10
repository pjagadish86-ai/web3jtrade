package com.aitrades.blockchain.web3jtrade.domain;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Reserves {

	private BigInteger reserve0;
	private BigInteger reserve1;
	
	public BigInteger getReserve0() {
		return reserve0;
	}
	public void setReserve0(BigInteger reserve0) {
		this.reserve0 = reserve0;
	}
	public BigInteger getReserve1() {
		return reserve1;
	}
	public void setReserve1(BigInteger reserve1) {
		this.reserve1 = reserve1;
	}
	
}
