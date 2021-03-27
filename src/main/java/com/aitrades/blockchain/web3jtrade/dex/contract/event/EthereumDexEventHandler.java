package com.aitrades.blockchain.web3jtrade.dex.contract.event;

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

import io.reactivex.Flowable;

public class EthereumDexEventHandler {

	private static final String MINT = "Mint";

	public static final Event MINT_EVENT = new Event(MINT, Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) { }, new TypeReference<Uint256>() { }, new TypeReference<Uint256>() { }));

	public static Flowable<EthLog> mintEventFlowables(Web3j web3j, String pairAddress, String routerAddress) {
		EthFilter filter = new EthFilter(new DefaultBlockParameterNumber(9903308), DefaultBlockParameterName.LATEST,	pairAddress);
		filter.addSingleTopic(EventEncoder.encode(MINT_EVENT));
		filter.addOptionalTopics("0x" + TypeEncoder.encode(new Address(routerAddress.substring(2))));
		return web3j.ethGetLogs(filter).flowable();
	}

}
