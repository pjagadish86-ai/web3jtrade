package com.aitrades.blockchain.web3jtrade.domain;

import java.math.BigInteger;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Numeric;

import com.aitrades.blockchain.web3jtrade.client.Web3jServiceClient;
import com.jsoniter.JsonIterator;
import com.jsoniter.spi.TypeLiteral;

import reactor.core.scheduler.Schedulers;

@Component
public class StrategyGasProvider implements ContractGasProvider{

	private static final String GAS_PRICE_ORACLE ="gasPriceOracle";
	
	@Resource(name="gasWebClient")
	private WebClient gasWebClient;
	
	@Resource(name = "web3jServiceClient")
	private Web3jServiceClient web3jServiceClient;
	
	public static final BigInteger GAS_PRICE = BigInteger.valueOf(220000000000L);
	private static final BigInteger GAS_LIMIT = BigInteger.valueOf(43000000);
	    
	public BigInteger getGasPrice(GasModeEnum gasModeEnum) {
		String respo = gasWebClient.get()
							   .uri(GAS_PRICE_ORACLE)
							   .retrieve()
							   .bodyToMono(String.class)
							   .subscribeOn(Schedulers.immediate())
							   .block();
		Map<String, String> gasMap = JsonIterator.deserialize(respo, new TypeLiteral<Map<String, String>>(){});
		return gasModeEnum != null ? Numeric.decodeQuantity(gasMap.get(gasModeEnum.name())): GAS_PRICE;
	}
	
	public BigInteger getGasLimit() {
		return getGasLimit(false);
	}

	public BigInteger getGasLimit(boolean sensitive) {
		return sensitive ? web3jServiceClient.getWeb3j()
											 .ethGetBlockByNumber(DefaultBlockParameterName.PENDING, true)
											 .flowable()
											 .subscribeOn(io.reactivex.schedulers.Schedulers.newThread())
											 .blockingLast()
											 .getBlock()
											 .getGasLimit()
					    : GAS_LIMIT;
	}

	@Override
	public BigInteger getGasPrice(String contractFunc) {
		return null;
	}

	@Override
	public BigInteger getGasLimit(String contractFunc) {
		return null;
	}

	@Override
	public BigInteger getGasPrice() {
		return null;
	}

}
