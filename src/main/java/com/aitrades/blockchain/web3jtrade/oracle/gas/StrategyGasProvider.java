package com.aitrades.blockchain.web3jtrade.oracle.gas;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.utils.Convert;

import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;

import reactor.core.scheduler.Schedulers;

@Component
public class StrategyGasProvider {

	private static final String PANCAKE = "PANCAKE";
	private static final String GAS_PRICE_ORACLE ="/gasPriceOracle";

	@Resource(name="gasWebClient")
	private WebClient gasWebClient;
	
	@Autowired
	private Web3jServiceClientFactory  web3jServiceClientFactory;
	
	@SuppressWarnings("unchecked")
	public BigInteger getGasPrice(GasModeEnum gasModeEnum){
		Map<String, Object> gasPrices = gasWebClient.get()
												    .uri(GAS_PRICE_ORACLE)
												    .accept(MediaType.APPLICATION_JSON)
												    .retrieve()
												    .bodyToMono(LinkedCaseInsensitiveMap.class)
												    .subscribeOn(Schedulers.fromExecutor(Executors.newCachedThreadPool()))
												    .block();
		String gasMode = gasModeEnum.getValue().toLowerCase();
		if(StringUtils.equalsIgnoreCase(gasModeEnum.getValue().toLowerCase(), "ULTRA")) {
			gasMode ="FASTEST";
		}
		return Convert.toWei(gasPrices.get(gasMode).toString(), Convert.Unit.GWEI).toBigInteger();
	}
	
	public BigInteger getGasLimit(String route){
		return BigInteger.valueOf(50000l);
	}
	
	@SuppressWarnings("unchecked")
	public BigInteger getGasPricePancake(GasModeEnum gasModeEnum) throws Exception{
		Map<String, Object> gasPrices = gasWebClient.get()
												    .uri(GAS_PRICE_ORACLE)
												    .accept(MediaType.APPLICATION_JSON)
												    .retrieve()
												    .bodyToMono(LinkedCaseInsensitiveMap.class)
												    .subscribeOn(Schedulers.fromExecutor(Executors.newCachedThreadPool()))
												    .block();
		String gasMode = gasModeEnum.getValue().toLowerCase();
		if(StringUtils.equalsIgnoreCase(gasModeEnum.getValue().toLowerCase(), "ULTRA")) {
			gasMode ="FASTEST";
		}
		Object gasPrice = gasPrices.get(gasMode);
		return gasPrice != null ? Convert.toWei(gasPrice.toString(), Convert.Unit.GWEI).toBigInteger() : null;
	}
	
	public BigInteger getGasLimitOfPancake(){
		return  web3jServiceClientFactory.getWeb3jMap().get(PANCAKE).getWeb3j()
				 .ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false)
				 .flowable()
				 .subscribeOn(io.reactivex.schedulers.Schedulers.newThread())
				 .blockingLast()
				 .getBlock()
				 .getGasLimit();
	}

	
	public BigInteger getGasLimit(Transaction transaction, String route) {
		return  web3jServiceClientFactory.getWeb3jMap().get(route).getWeb3j()
										 .ethEstimateGas(transaction)
										 .flowable()
										 .subscribeOn(io.reactivex.schedulers.Schedulers.newThread())
										 .blockingLast()
										 .getAmountUsed();
	}

}
