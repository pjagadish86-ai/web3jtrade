package com.aitrades.blockchain.web3jtrade.integration.snipe;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.tuples.generated.Tuple3;

import com.aitrades.blockchain.web3jtrade.dex.contract.DexTradeContractService;
import com.aitrades.blockchain.web3jtrade.dex.contract.event.EthereumDexEventHandler;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;

import io.reactivex.schedulers.Schedulers;

@Service
public class LiquidityEventOrReserversFinder {
	
	private static final int ZERO = 0;

	@Autowired
	private Web3jServiceClientFactory web3jServiceClientFactory;
	
	@Autowired
	private DexTradeContractService ethereumDexTradeService;
	
	public boolean hasLiquidityOrReserves(final String route, final String  pairAddress,final Credentials credentials,final  BigInteger gasPrice, final  BigInteger gasLimit, BigInteger inputAmount, final String  gasMode) throws Exception{
		 return hasLiquidityOrReservers(getReservesEventChannel(route, pairAddress, credentials, gasPrice, gasLimit, gasMode), 
				 					    hasAddedLiquidityEvent(route, pairAddress),
				 					    inputAmount);
	}
	
	private boolean hasLiquidityOrReservers(final Tuple3<BigInteger, BigInteger, BigInteger> reserves,final EthLog liquidityEvent, BigInteger inputAmount) {
		return (reserves != null && reserves.component1().compareTo(BigInteger.ZERO) > ZERO && reserves.component2().compareTo(inputAmount) >= ZERO) 
				|| liquidityEvent != null;
	}

	private EthLog hasAddedLiquidityEvent(final String  route, final String  pairAddresss) throws Exception{
		try {
			return EthereumDexEventHandler.mintEventFlowables(web3jServiceClientFactory.getWeb3jMap().get(route).getWeb3j(), 
															  pairAddresss, 
														      TradeConstants.ROUTER_MAP.get(pairAddresss))
					.subscribeOn(Schedulers.io())
					.blockingSingle();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	private Tuple3<BigInteger, BigInteger, BigInteger> getReservesEventChannel(final String  route, final String  pairAddress,final  Credentials credentials,final  BigInteger gasPrice, BigInteger gasLimit, final String  gasMode) throws Exception{
		try {
			
			
			return ethereumDexTradeService.getReservesOfPair(route, 
															 pairAddress, 
															 credentials, 
															 gasPrice, 
															 gasLimit,
													         gasMode);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
