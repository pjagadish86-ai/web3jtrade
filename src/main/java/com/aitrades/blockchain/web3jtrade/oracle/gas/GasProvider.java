package com.aitrades.blockchain.web3jtrade.oracle.gas;

import java.math.BigInteger;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.request.Transaction;

import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
@Component
public class GasProvider{
	
	private static final String CUSTOM ="CUSTOM";
	
	@Autowired
	private StrategyGasProvider strategyGasProvider;
	
	public BigInteger getGasPrice(GasModeEnum gasMode, BigInteger gasPrice) throws Exception {
		return StringUtils.equalsIgnoreCase(gasMode.getValue().toLowerCase(), CUSTOM) ? gasPrice : strategyGasProvider.getGasPrice(gasMode);
	}


	public BigInteger getGasLimit(GasModeEnum gasMode, BigInteger gasLimit) throws Exception {
		return StringUtils.equalsIgnoreCase(gasMode.getValue().toLowerCase(), CUSTOM) ? gasLimit : strategyGasProvider.getGasLimit(TradeConstants.UNISWAP);
	}

	public BigInteger getGasLimit(GasModeEnum gasMode,  BigInteger gasLimit, Transaction transaction) throws Exception {
		if(StringUtils.equalsIgnoreCase(gasMode.getValue().toLowerCase(), CUSTOM)) {
			return gasLimit;
		}else {
			return strategyGasProvider.getGasLimit(transaction, TradeConstants.UNISWAP);
		}
	}
	
	public BigInteger gasLimitPancake(String address, String data, String route) {
		try {
			return strategyGasProvider.getGasLimit(Transaction.createFunctionCallTransaction(address,  null, null, null, TradeConstants.PANCAKE_ROUTER_ADDRESS, data), route);
		} catch (Exception e) {
			return BigInteger.valueOf(21000l).add(BigInteger.valueOf(68l)
					.multiply(BigInteger.valueOf(data.getBytes().length)));
		}
	}

}
