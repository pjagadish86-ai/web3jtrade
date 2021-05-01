package com.aitrades.blockchain.web3jtrade.trade.pendingTransaction;

//public class EthereumGethPendingTransactionsRetriever {
//
//	public static final String UNISWAP_FACTORY_ADDRESS = "0x5c69bee701ef814a2b6a3edd4b1652cb9cc5aa6f";
//    public static final String UNISWAP_ROUTER_ADDRESS = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D";
//    public static final Pattern pattern = Pattern.compile("");
//    
//    private static final Set<String> methodNames =  Set.of("0x7ff36ab5","0x18cbafe5");
//    
//	
//	private Web3jServiceClient web3jServiceClient;
//
//	private BigInteger pendingTransactionsFrontRunnerFilter(BigInteger gas, String contractAddress) {
//		EQ fromAddress = new EQ(UNISWAP_ROUTER_ADDRESS);
//		Iterable<BigInteger> gasIterables =	parity.parityPendingTransactions(null, new ParityPendingTranscationFilter(fromAddress, 
//																													    null, 
//																													    null, 
//																													    new LogicalOperations(gas, null, null), 
//																													    null, 
//																													    null))
//													      .flowable()
//													      .parallel(Runtime.getRuntime().availableProcessors(), 1000)
//												          .runOn(Schedulers.io())
//												          .sequential()
//												          .map(ParityPendingTransactionResponse :: getResult)
//												          .flatMapIterable(resp -> resp)
//												          .map(trans-> filterContractAndCollectGas(trans, contractAddress))
//												          .filter(Objects :: nonNull)
//												          .blockingIterable();
//		return Ordering.natural().max(gasIterables);
//	}
//	
//	private BigInteger filterContractAndCollectGas(Transaction transaction, String contractAddress) {
//		return iftheGivenContractIsInTheData(transaction.getInput(), contractAddress) ? transaction.getGas() : null;
//	}
//	
//	private boolean iftheGivenContractIsInTheData(String input, String contractAddress) {
//		final String inputStr  = input.substring(0, 10);
//		if(methodNames.contains(inputStr)) {
//			final String pendingTranscationCntr = getContractAddress(inputStr);
//			return StringUtils.equalsIgnoreCase(pendingTranscationCntr, contractAddress);
//		}
//		return false;
//	}
//
//	private String getContractAddress(String input) {
//		return Iterables.getLast(Splitter.fixedLength(64).split(input), null);
//	}
//	
//}