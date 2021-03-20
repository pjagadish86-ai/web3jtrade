package com.aitrades.blockchain.web3jtrade.trade.snipe;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.methods.request.EQ;
import org.web3j.protocol.parity.methods.request.LogicalOperations;
import org.web3j.protocol.parity.methods.request.ParityPendingTranscationFilter;
import org.web3j.protocol.parity.methods.response.ParityPendingTransactionResponse;

import com.aitrades.blockchain.web3jtrade.client.Web3jServiceClient;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Component
@SuppressWarnings("unused")
public class EthereumGethPendingTransactionsRetriever {

	public static final String UNISWAP_FACTORY_ADDRESS = "0x5c69bee701ef814a2b6a3edd4b1652cb9cc5aa6f";
    public static final String UNISWAP_ROUTER_ADDRESS = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D";
    public static final Pattern pattern = Pattern.compile("");
    
    private static final Set<String> methodNames =  Set.of("0x7ff36ab5","0x18cbafe5");
    
	@Autowired
	public Parity parity;
	
	@Resource(name = "web3jServiceClient")
	private Web3jServiceClient web3jServiceClient;

	private BigInteger pendingTransactionsFrontRunnerFilter(BigInteger gas, String contractAddress) {
		EQ fromAddress = new EQ(UNISWAP_ROUTER_ADDRESS);
		Iterable<BigInteger> gasIterables =	parity.parityPendingTransactions(null, new ParityPendingTranscationFilter(fromAddress, 
																													    null, 
																													    null, 
																													    new LogicalOperations(gas, null, null), 
																													    null, 
																													    null))
													      .flowable()
													      .parallel(Runtime.getRuntime().availableProcessors(), 1000)
												          .runOn(Schedulers.io())
												          .sequential()
												          .map(ParityPendingTransactionResponse :: getResult)
												          .flatMapIterable(resp -> resp)
												          .map(trans-> filterContractAndCollectGas(trans, contractAddress))
												          .filter(Objects :: nonNull)
												          .blockingIterable();
		return Ordering.natural().max(gasIterables);
	}
	
	private BigInteger filterContractAndCollectGas(Transaction transaction, String contractAddress) {
		return iftheGivenContractIsInTheData(transaction.getInput(), contractAddress) ? transaction.getGas() : null;
	}
	
	private boolean iftheGivenContractIsInTheData(String input, String contractAddress) {
		final String inputStr  = input.substring(0, 10);
		if(methodNames.contains(inputStr)) {
			final String pendingTranscationCntr = getContractAddress(inputStr);
			return StringUtils.equalsIgnoreCase(pendingTranscationCntr, contractAddress);
		}
		return false;
	}

	private String getContractAddress(String input) {
		return Iterables.getLast(Splitter.fixedLength(64).split(input), null);
	}
	
}
