package com.aitrades.blockchain.web3jtrade.oracle.gas;

import java.math.BigInteger;

import org.web3j.tx.gas.ContractGasProvider;

public class GasProvider implements ContractGasProvider{

	
	@Override
	public BigInteger getGasPrice(String contractFunc) {
		return new BigInteger(contractFunc);
	}

	@Deprecated
	@Override
	public BigInteger getGasPrice() {
		return null;
	}

	@Override
	public BigInteger getGasLimit(String contractFunc) {
		return new BigInteger(contractFunc);
	}

	@Deprecated
	@Override
	public BigInteger getGasLimit() {
		return null;
	}

}
