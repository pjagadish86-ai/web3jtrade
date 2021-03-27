package com.aitrades.blockchain.web3jtrade.domain;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Convert;

import com.aitrades.blockchain.web3jtrade.client.Web3jServiceClient;
import com.fasterxml.jackson.databind.ObjectReader;

import reactor.core.scheduler.Schedulers;

@Component
//TODO: Gas should be populated in PairCreated Channel and assign it into snipeRequestObject, so this way we can avoid further or do just make a call?
public class StrategyGasProvider implements ContractGasProvider{

	private static final String GAS_PRICE_ORACLE ="/gasPriceOracle";

	@Resource(name="gasWebClient")
	private WebClient gasWebClient;
	
	@Resource(name= "gasPriceOracle")
	public ObjectReader gasPriceOracleObjectReader;
	
	@Resource(name = "web3jServiceClient")
	private Web3jServiceClient web3jServiceClient;
	
	public static final BigInteger GAS_PRICE = BigInteger.valueOf(220000000000L); // Gas Price (GWEI) 1
	private static final BigInteger GAS_LIMIT = BigInteger.valueOf(467296);// Gas Limit (Units) 167296
	    
	@SuppressWarnings("unchecked")
	public BigInteger getGasPrice(GasModeEnum gasModeEnum) throws Exception{
		Map<String, Double> gasPrices = gasWebClient.get()
													   .uri(GAS_PRICE_ORACLE)
													   .accept(MediaType.APPLICATION_JSON)
													   .retrieve()
													   .bodyToMono(Map.class)
													   .subscribeOn(Schedulers.fromExecutor(Executors.newCachedThreadPool()))
													   .block();
		return Convert.toWei(gasPrices.get(gasModeEnum.getValue()).toString(), Convert.Unit.GWEI).toBigInteger();
	}
	
	public BigInteger getGasLimit() {
		try {
			return getGasLimit(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public BigInteger getGasLimit(boolean sensitive) throws Exception{
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
