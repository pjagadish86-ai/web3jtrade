package com.aitrades.blockchain.web3jtrade.integration.snipe;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.tuples.generated.Tuple3;

import com.aitrades.blockchain.web3jtrade.dex.contract.EthereumDexContract;
import com.aitrades.blockchain.web3jtrade.dex.contract.event.EthereumDexEventHandler;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

@Service
public class LiquidityEventOrReserversFinder2 {
	
	private static final Scheduler SCHEDULER = Schedulers.from(Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors()));

	private static final int ZERO = 0;
	
	@Autowired
	public Executor executor;
	
	@Autowired
	private Web3jServiceClientFactory web3jServiceClientFactory;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Boolean> parallelExecution(final String route, final String  pairAddress, final Credentials credentials,final  BigInteger gasPrice, final  BigInteger gasLimit, BigInteger inputAmount, final String  gasMode) throws Exception {
		List<Boolean> results = new ArrayList(1);
		Flowable.zip(new EthereumDexContract(pairAddress, web3jServiceClientFactory.getWeb3jMap().get(route).getWeb3j(), credentials).getReserves().flowable(), 
												  EthereumDexEventHandler.mintEventFlowables(web3jServiceClientFactory.getWeb3jMap().get(route).getWeb3j(), pairAddress, TradeConstants.ROUTER_MAP.get(pairAddress)),
												  (reserves, liquidityEvent) -> hasLiquidityOrReservers1(reserves, liquidityEvent, inputAmount))
											 .subscribeOn(SCHEDULER)
											 .blockingSubscribe(resp -> {
													results.add(resp);
													return;
												});
		return results;
	}
	
	private boolean hasLiquidityOrReservers1(Tuple3<BigInteger, BigInteger, BigInteger> reserves, EthLog liquidityEvent,
											 BigInteger inputAmount) {
		return (reserves != null && reserves.component1().compareTo(BigInteger.ZERO) > ZERO && reserves.component2().compareTo(inputAmount) >= ZERO) 
				|| liquidityEvent != null;
	}

}
