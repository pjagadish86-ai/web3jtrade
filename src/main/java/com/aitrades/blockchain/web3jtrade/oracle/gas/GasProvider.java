package com.aitrades.blockchain.web3jtrade.oracle.gas;

import java.math.BigInteger;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
@Component
public class GasProvider{
	
	private static final String CUSTOM ="CUSTOM";
	
	@Autowired
	private StrategyGasProvider strategyGasProvider;
	
	public BigInteger getGasPrice(GasModeEnum gasMode, BigInteger gasPrice) throws Exception {
		return StringUtils.equalsIgnoreCase(gasMode.getValue().toLowerCase(), CUSTOM) ? gasPrice : strategyGasProvider.getGasPrice(gasMode);
	}


	public BigInteger getGasLimit(GasModeEnum gasMode, BigInteger gasLimit) throws Exception {
		return StringUtils.equalsIgnoreCase(gasMode.getValue().toLowerCase(), CUSTOM) ? gasLimit : strategyGasProvider.getGasPrice(gasMode);
	}


}
