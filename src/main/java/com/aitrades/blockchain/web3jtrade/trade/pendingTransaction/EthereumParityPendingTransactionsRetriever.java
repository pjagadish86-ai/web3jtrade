package com.aitrades.blockchain.web3jtrade.trade.pendingTransaction;

//public class EthereumParityPendingTransactionsRetriever {
//
//	private static final String PANCAKE = "PANCAKE";
//	private static final String SUSHI = "SUSHI";
//	private static final String UNISWAP = "UNISWAP";
//	public static final String UNISWAP_FACTORY_ADDRESS = "0x5c69bee701ef814a2b6a3edd4b1652cb9cc5aa6f";
//    public static final String UNISWAP_ROUTER_ADDRESS = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D";
//    public static final Pattern pattern = Pattern.compile("");
//    
//    private static final Set<String> UNISWAP_TRADE_METHODS =  Set.of("0x7ff36ab5","0x18cbafe5");
//    
//    private static final Set<String> UNISWAP_LIQUID_METHODS =  Set.of("0x7ff36ab5","0x18cbafe5");
//
//	@Autowired
//	private Parity parity;
//	
//	@Resource(name = "web3jServiceClient")
//	private Web3jServiceClient web3jServiceClient;
//
//	// Find a solution between frontrunning which needs a subscription and single 78
//	public BigInteger pendingTransactionsFrontRunnerFilter(String route, boolean isFrontRun, boolean hasLiquidityAdded, BigInteger gas, String contractAddress) {
//		EQ fromAddress = getRoute(route);
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
//												          .map(trans-> filterContractAndCollectGas(trans, contractAddress, hasLiquidityAdded))
//												          .filter(Objects :: nonNull)
//												          .blockingIterable();
//		return Ordering.natural().max(gasIterables);
//	}
//
//	private EQ getRoute(String route) {
//		if(StringUtils.equalsIgnoreCase(route, UNISWAP)) {
//			return new EQ(UNISWAP_ROUTER_ADDRESS);	
//		}else if(StringUtils.equalsIgnoreCase(route, SUSHI)) {
//			return new EQ(UNISWAP_ROUTER_ADDRESS);	
//		}else if(StringUtils.equalsIgnoreCase(route, PANCAKE)) {
//			return new EQ(UNISWAP_ROUTER_ADDRESS);	
//		}
//		return null;
//	}
//	
//	private BigInteger filterContractAndCollectGas(Transaction transaction, String contractAddress, boolean hasLiquidityAdded) {
//		return isContractMatch(transaction.getInput(), contractAddress, hasLiquidityAdded) ? transaction.getGas() : null;
//	}
//	
//	private boolean isContractMatch(String input, String contractAddress, boolean hasLiquidityAdded) {
//		final String inputStr  = input.substring(0, 10);
//		boolean hasValidEventMethod = hasLiquidityAdded ? UNISWAP_TRADE_METHODS.contains(inputStr) : UNISWAP_LIQUID_METHODS.contains(inputStr);
//		if(hasValidEventMethod) {
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
