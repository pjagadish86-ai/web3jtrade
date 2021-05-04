
package com.aitrades.blockchain.web3jtrade.integration.snipe;

import java.awt.Desktop;
import java.math.BigInteger;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple3;

import com.aitrades.blockchain.web3jtrade.client.DexNativePriceOracleClient;
import com.aitrades.blockchain.web3jtrade.dex.contract.DexTradeContractService;
import com.aitrades.blockchain.web3jtrade.domain.GasModeEnum;
import com.aitrades.blockchain.web3jtrade.domain.Reserves;
import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.aitrades.blockchain.web3jtrade.domain.TradeOverview;
import com.aitrades.blockchain.web3jtrade.integration.snipe.mq.SnipeOrderReQueue;
import com.aitrades.blockchain.web3jtrade.oracle.gas.GasProvider;
import com.aitrades.blockchain.web3jtrade.repository.SnipeOrderHistoryRepository;
import com.aitrades.blockchain.web3jtrade.repository.SnipeOrderRepository;
import com.aitrades.blockchain.web3jtrade.repository.TradeOverviewRepository;
import com.aitrades.blockchain.web3jtrade.service.DexContractStaticCodeValuesService;
import com.aitrades.blockchain.web3jtrade.service.Web3jServiceClientFactory;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Lists;
@SuppressWarnings({"unused", "rawtypes"})
public class SnipeExeEndpointV1{
	
	private static final String RUNDLL32_URL_DLL_FILE_PROTOCOL_HANDLER = "rundll32 url.dll,FileProtocolHandler ";
	private static final String ETHERSCAN = "https://etherscan.io/tx/";
	private static final String BSC_SCAN = "https://bscscan.com/tx/";
	private static final String FAILED = "FAILED";
	private static final String SNIPE = "SNIPE";
	private static final String _0X000000 = "0x000000";

	@Autowired
	private Web3jServiceClientFactory web3jServiceClientFactory;

	@Resource(name="orderSubmitRabbitTemplate")
	private AmqpTemplate orderSubmitRabbitTemplate;
	
	@Autowired
	private GasProvider gasProvider;
	
	@Autowired
	private DexNativePriceOracleClient dexNativePriceOracleClient2;
	
	@Resource(name="snipeTransactionRequestObjectReader")
	private ObjectReader snipeTransactionRequestObjectReader;
	
	@Autowired
	private DexTradeContractService ethereumDexTradeService;
	
	@Autowired
	private SnipeOrderRepository snipeOrderRepository;
	
	@Autowired
	private SnipeOrderHistoryRepository snipeOrderHistoryRepository;
	
	@Autowired 
	private TradeOverviewRepository tradeOverviewRepository;
	
	@Autowired
	private SnipeOrderReQueue snipeOrderReQueue;
	
	@Autowired
	private DexNativePriceOracleClient dexNativePriceOracleClient;
	
	@Autowired
	private LiquidityEventOrReserversFinder liquidityEventOrReserversFinder;
	
	@Autowired
	private DexContractStaticCodeValuesService dexContractStaticCodeValuesService;
	
	@Transformer(inputChannel = "snipeOrderMQReciever", outputChannel = "snipeSwapChannel")
	public SnipeTransactionRequest snipeOrderMQReciever(byte[] message ) throws Exception{
		return snipeTransactionRequestObjectReader.readValue(message);
	}
	
	@ServiceActivator(inputChannel = "snipeSwapChannel")
	public SnipeTransactionRequest snipeSwapChannel(SnipeTransactionRequest snipeTransactionRequest) throws Exception{
		
		Credentials credentials = snipeTransactionRequest.getCredentials();
		String dexContractAddress = dexContractStaticCodeValuesService.getDexContractAddress(snipeTransactionRequest.getRoute(), TradeConstants.WNATIVE);
		Address wnativeAddress = new Address(dexContractAddress);
		Address toAddress = new Address(snipeTransactionRequest.getToAddress());
		BigInteger gasPrice = gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasPrice());
		BigInteger gasLimit = gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasLimit());
		List<Address> swapMemoryPath = Lists.newArrayList(wnativeAddress, toAddress);
		
		// Get Pair;
		String pairAddress = getPairAddress(snipeTransactionRequest, dexContractAddress); 
		if(StringUtils.isNotBlank(pairAddress)) {
			snipeTransactionRequest.setPairAddress(pairAddress);
			System.err.println("Pair found");
		}else {
			return null;
		}

		
		// check for reserves
		Tuple3<BigInteger, BigInteger, BigInteger> reserves = getReserves(snipeTransactionRequest, credentials);
		if(reserves == null) {
			return null;
		}
		System.err.println("have reserves found");
		// get amounts out based on input
		BigInteger outputTokens = snipeTransactionRequest.getExpectedOutPutToken() == null ?  getAmountsIn(credentials, snipeTransactionRequest, Lists.newArrayList(toAddress, wnativeAddress), gasPrice, gasLimit) : snipeTransactionRequest.getExpectedOutPutToken();
		if(outputTokens == null) {
			return null;
		}
		System.err.println("received output tokesn "+ outputTokens);
		// perform swap:
		String hash = null;
		try {
				   hash = ethereumDexTradeService.swapETHForTokens(snipeTransactionRequest.getRoute(),
																   credentials,
																   snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(),
																   outputTokens,
																   snipeTransactionRequest.getDeadLine(),
																   swapMemoryPath,
																   snipeTransactionRequest.isFeeEligible(),
																   gasPrice,
																   gasLimit,
																   snipeTransactionRequest.getGasMode());	
			if (StringUtils.isNotBlank(hash)) {
				String url = StringUtils.equalsIgnoreCase(snipeTransactionRequest.getRoute(), TradeConstants.PANCAKE) ? BSC_SCAN+hash : ETHERSCAN+hash;
				System.out.println( url);
				System.out.println("snipe hash >>-> "+ hash);
				Runtime rt = Runtime.getRuntime();
			    rt.exec(RUNDLL32_URL_DLL_FILE_PROTOCOL_HANDLER + url);
				snipeTransactionRequest.setSwappedHash(hash);
				snipeTransactionRequest.setSnipeStatus(TradeConstants.FILLED);
				snipeTransactionRequest.setSnipe(true);
				snipeTransactionRequest.getAuditInformation().setUpdatedDateTime(Instant.now().toString());
				tradeOverviewRepository.save(mapRequestToTradeOverView(snipeTransactionRequest));
				purgeMessage(snipeTransactionRequest);
			}
		} catch (Exception e) {
			snipeTransactionRequest.setErrorMessage(e.getMessage());
			purgeMessage(snipeTransactionRequest);
		} 
		return null;
	}

	private BigInteger getAmountsIn(Credentials credentials, SnipeTransactionRequest snipeTransactionRequest, List<Address> amountsInMemoryPath, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
		BigInteger outputTokens = null;
		try {
			outputTokens = ethereumDexTradeService.getAmountsIn(snipeTransactionRequest.getRoute(),
																credentials,
															    snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(),
															    snipeTransactionRequest.getSlipageInDouble(),
															    amountsInMemoryPath,
															    gasPrice,
															    gasLimit,
															    snipeTransactionRequest.getGasMode(),
															    snipeTransactionRequest.getToAddressDecimals());
		} catch (Exception e) {
			System.err.println(" getAmountsIn error "+ e.getMessage());
			if (StringUtils.containsIgnoreCase(e.getMessage(), TradeConstants.INSUFFICIENT_LIQUIDITY) || StringUtils.containsIgnoreCase(e.getMessage(), TradeConstants.DS_MATH_SUB_UNDERFLOW)) {
				System.err.println(" getAmountsIn error insufficient liquidity "+ e.getMessage());
			} else {
				System.err.println("error in getAmountsIn purging order-> "+ e.getMessage());
				snipeTransactionRequest.setErrorMessage(e.getMessage());
				purgeMessage(snipeTransactionRequest);
				throw e;
			}
		}

		if (outputTokens != null && outputTokens.compareTo(BigInteger.ZERO) > 0) {
			return outputTokens;
		} else {
			Thread.sleep(1000l);
			return getAmountsIn(credentials, snipeTransactionRequest, amountsInMemoryPath, gasPrice, gasLimit);
		}
	}

	private Tuple3<BigInteger, BigInteger, BigInteger> getReserves(SnipeTransactionRequest snipeTransactionRequest, Credentials credentials) 	throws Exception {
		Tuple3<BigInteger, BigInteger, BigInteger> reserves = null;
		try {
			reserves = liquidityEventOrReserversFinder.fetchReserves(snipeTransactionRequest.getRoute(), 
															         snipeTransactionRequest.getPairAddress(), 
															         credentials,
															         snipeTransactionRequest.getGasPrice(), 
															         snipeTransactionRequest.getGasLimit(), 
															         snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(), 
															         snipeTransactionRequest.getGasMode());
		} catch (Exception e) {
			System.err.println(" getReserves error "+ e.getMessage());
		}
		
		if(reserves != null && preChecksForSnipe(snipeTransactionRequest, reserves)) {
			return reserves;
		}else {
			System.err.println("Not Listed / No Reserves");
			Thread.sleep(1000l);
			return getReserves(snipeTransactionRequest, credentials);
		}
	}
	
	
	
	private String getPairAddress(SnipeTransactionRequest snipeTransactionRequest, String dexContractAddress) throws Exception {
		String pairAddress = getPairAddressFrmExchange(snipeTransactionRequest, dexContractAddress);
		if(StringUtils.isNotBlank(pairAddress)) {
			return pairAddress;
		}else {
			System.err.println("Not Listed / No Pair");
			Thread.sleep(1000l);
			return getPairAddress(snipeTransactionRequest, dexContractAddress);
		}
	}

	private String getPairAddressFrmExchange(SnipeTransactionRequest snipeTransactionRequest, String dexServiceContractAddress) throws Exception {
		Type pairAddress = null; 
		try {
			pairAddress = ethereumDexTradeService.getPairAddress(snipeTransactionRequest.getRoute(), 
																 snipeTransactionRequest.getToAddress(),  
																 dexServiceContractAddress)
					   							 .get(0);
			if(pairAddress != null && StringUtils.startsWithIgnoreCase((String)pairAddress.getValue(), _0X000000) && StringUtils.equalsIgnoreCase(snipeTransactionRequest.getRoute(), TradeConstants.PANCAKE)) {
				pairAddress = ethereumDexTradeService.getPairAddress(snipeTransactionRequest.getRoute(), 
																	 snipeTransactionRequest.getToAddress(), 
																	 TradeConstants.BUSD)
													 .get(0);
				snipeTransactionRequest.setBusdPair(true);
			}
		} catch (Exception e) {
			System.err.println(" getPairAddressFrmExchange error "+ e.getMessage());
			pairAddress  = ethereumDexTradeService.getPairAddress(snipeTransactionRequest.getRoute(), 
																  snipeTransactionRequest.getToAddress(), 
																  dexServiceContractAddress)
		             							  .parallelStream()
		             							  .findFirst()
		             							  .get();
		}
		if(pairAddress != null && !StringUtils.startsWithIgnoreCase((String)pairAddress.getValue(), _0X000000)) {
			return (String)pairAddress.getValue();
		}
		return null;
	}


	private boolean preChecksForSnipe(SnipeTransactionRequest snipeTransactionRequest,
									  Tuple3<BigInteger, BigInteger, BigInteger> reserves) {
		boolean hasReserves = reserves != null  && reserves.component1().compareTo(BigInteger.ZERO) > 0
												&& reserves.component2().compareTo(BigInteger.ZERO) > 0;
		
		boolean hasLiquidityQuantity = snipeTransactionRequest.getLiquidityQuantity() != null;
		
		if(hasReserves && hasLiquidityQuantity) {
			if(reserves.component2().compareTo(snipeTransactionRequest.getLiquidityQuantity()) >= 0) {
				return true;
			}else {
				return false;
			}
		}
		return hasReserves;
	}
	
	private Reserves mapReserves(Tuple3<BigInteger, BigInteger, BigInteger> reserves) {
		Reserves reserves2 = new Reserves();
		reserves2.setReserve0(reserves.component1());
		reserves2.setReserve1(reserves.component2());
		return reserves2;
	}


	private void purgeMessage(SnipeTransactionRequest snipeTransactionRequest) throws Exception {
		snipeOrderHistoryRepository.save(snipeTransactionRequest);
		snipeOrderRepository.delete(snipeTransactionRequest);
	}

	private TradeOverview mapRequestToTradeOverView(SnipeTransactionRequest request) throws Exception {
		TradeOverview overview = new TradeOverview();
		overview.setApprovedHash(request.getApprovedHash());
		overview.setSwappedHash(request.getSwappedHash());
		overview.setErrorMessage(request.getErrorMessage());
		overview.setId(request.getId());
		overview.setOrderDesc(SNIPE);
		overview.setOrderSide(null);
		overview.setOrderState(StringUtils.isNotBlank(request.getErrorMessage())? FAILED: request.getSnipeStatus());
		overview.setOrderType(SNIPE);
		overview.setRoute(request.getRoute());
		try {
			overview.setExecutedPrice(dexNativePriceOracleClient.tokenPrice(request.getPairAddress(), request.getRoute(), request.getCredentials()));
		} catch (Exception e) {
			overview.setErrorMessage(request.getErrorMessage()+" your fucking code sucks!!!");
		}
		return overview;
	}
	
}
