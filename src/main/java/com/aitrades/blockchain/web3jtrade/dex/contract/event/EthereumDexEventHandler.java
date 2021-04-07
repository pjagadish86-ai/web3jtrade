package com.aitrades.blockchain.web3jtrade.dex.contract.event;

import java.math.BigInteger;
import java.util.Arrays;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;

import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;

import io.reactivex.Flowable;

public class EthereumDexEventHandler {
	
	private EthereumDexEventHandler() {
	}

	private static final String ZERO_X = "0x";
	private static final String MINT = "Mint";
	
	private static final String UNISWAP_ROUTER_TYPE_ENCODER = ZERO_X + TypeEncoder.encode(new Address(TradeConstants.UNISWAP_ROUTERADDRESS.substring(2)));
	private static final String PANCAKE_ROUTER_TYPE_ENCODER = ZERO_X + TypeEncoder.encode(new Address(TradeConstants.PANCAKE_ROUTER_ADDRESS.substring(2)));

	private static final Event MINT_EVENT = new Event(MINT, Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) { }, new TypeReference<Uint256>() { }, new TypeReference<Uint256>() { }));
	private static final String MINT_EVENT_ENCODER = EventEncoder.encode(MINT_EVENT);
	
	// for now hardcoded please pull from db for block numbers
	private static final BigInteger BSC_FROM_BLOCK_NBR = BigInteger.valueOf(12189557);
	private static final BigInteger ETH_FROM_BLOCK_NBR = BigInteger.valueOf(6346390);

	public static Flowable<EthLog> mintEventFlowables(Web3j web3j, String pairAddress, String route) {
		final EthFilter filter = new EthFilter(new DefaultBlockParameterNumber(TradeConstants.PANCAKE.equalsIgnoreCase(route) ? BSC_FROM_BLOCK_NBR : ETH_FROM_BLOCK_NBR), DefaultBlockParameterName.LATEST, pairAddress);
		filter.addSingleTopic(MINT_EVENT_ENCODER);
		filter.addOptionalTopics(TradeConstants.PANCAKE.equalsIgnoreCase(route) ? PANCAKE_ROUTER_TYPE_ENCODER : UNISWAP_ROUTER_TYPE_ENCODER);
		return web3j.ethGetLogs(filter)
					.flowable();
	}

}
