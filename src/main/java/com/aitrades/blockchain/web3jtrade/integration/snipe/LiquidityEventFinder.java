package com.aitrades.blockchain.web3jtrade.integration.snipe;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;

import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;

import io.reactivex.schedulers.Schedulers;
@Service
public class LiquidityEventFinder {

	private static final String MINT = "Mint";
	
	private static final Event MINT_EVENT = new Event(MINT, Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) { }, new TypeReference<Uint256>() { }, new TypeReference<Uint256>() { }));
	private static final String MINT_EVENT_ENCODER = EventEncoder.encode(MINT_EVENT);
	
	@Autowired
	private Web3jServiceClientFactory  web3jServiceClientFactory;

	public EthLog hasLiquidityEvent(final String route, final DefaultBlockParameter fromBlockNbr, final DefaultBlockParameter toBlockNbr, final String routerAddress, final String pairAddress) throws Exception {
		try {
			return web3jServiceClientFactory.getWeb3jMap(route)
											.getWeb3j()
											.ethGetLogs(new EthFilter(fromBlockNbr, toBlockNbr, pairAddress)
															.addSingleTopic(MINT_EVENT_ENCODER)
															.addOptionalTopics(routerAddress))
				   .flowable()
				   .subscribeOn(Schedulers.io())
				   .blockingSingle();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
