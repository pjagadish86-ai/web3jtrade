package com.aitrades.blockchain.web3jtrade.integration.snipe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog;

import com.aitrades.blockchain.web3jtrade.dex.contract.event.EthereumDexEventHandler;
import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

@Service
//TODO: come back to fine tune the code.
public class LiquidityEventRetriever {
	
	@Autowired
	private Web3jServiceClientFactory web3jServiceClientFactory;

	public void hasLiquidityEvent(SnipeTransactionRequest snipeTransactionRequest) {
		try {
			Flowable<EthLog> flowable = EthereumDexEventHandler.mintEventFlowables(web3jServiceClientFactory.getWeb3jMap().get(snipeTransactionRequest.getRoute()).getWeb3j(), 
																				   snipeTransactionRequest.getPairAddress(), 
																				   TradeConstants.ROUTER_MAP.get(snipeTransactionRequest.getRoute()));
			
			flowable.subscribeOn(Schedulers.computation())
					.subscribe(resp -> {
						if(resp != null) {
							snipeTransactionRequest.setHasLiquidity(true);
							return;
						}
					});
		} catch (Exception e) {
		}
	}

	
}
