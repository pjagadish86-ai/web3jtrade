
package com.aitrades.blockchain.web3jtrade.integration.snipe;

import java.math.BigInteger;
import java.time.Instant;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
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
public class OrderSnipeExecuteGatewayEndpoint{
	
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
	
	@Transformer(inputChannel = "rabbitMqSubmitOrderConsumer", outputChannel = "pairCreatedEventChannel")
	public SnipeTransactionRequest rabbitMqSubmitOrderConsumer(byte[] message ) throws Exception{
		return snipeTransactionRequestObjectReader.readValue(message);
	}
	
	@ServiceActivator(inputChannel = "pairCreatedEventChannel", outputChannel = "liquidityEventOrReservesFinderChannel")
	public SnipeTransactionRequest pairCreatedEventChannel(SnipeTransactionRequest snipeTransactionRequest) throws Exception{
		if(StringUtils.isNotBlank(snipeTransactionRequest.getPairAddress())) {
			return snipeTransactionRequest;
		}
		Type pairAddress = null;
		try {
			pairAddress = ethereumDexTradeService.getPairAddress(snipeTransactionRequest.getRoute(), snipeTransactionRequest.getToAddress(),  dexContractStaticCodeValuesService.getDexContractAddress(snipeTransactionRequest.getRoute(), TradeConstants.WNATIVE))
													   .get(0);
			if(pairAddress != null && StringUtils.startsWithIgnoreCase((String)pairAddress.getValue(), _0X000000) && StringUtils.equalsIgnoreCase(snipeTransactionRequest.getRoute(), TradeConstants.PANCAKE)) {
				pairAddress = ethereumDexTradeService.getPairAddress(snipeTransactionRequest.getRoute(), snipeTransactionRequest.getToAddress(), TradeConstants.BUSD)
						   .get(0);
				snipeTransactionRequest.setBusdPair(true);
			}
		} catch (Exception e) {
			pairAddress  = ethereumDexTradeService.getPairAddress(snipeTransactionRequest.getRoute(), snipeTransactionRequest.getToAddress(),  dexContractStaticCodeValuesService.getDexContractAddress(snipeTransactionRequest.getRoute(), TradeConstants.WNATIVE))
		             .parallelStream()
		             .findFirst().get();
		}
		if(pairAddress != null && !StringUtils.startsWithIgnoreCase((String)pairAddress.getValue(), _0X000000)) {
			snipeTransactionRequest.setPairAddress((String)pairAddress.getValue());
			System.out.println("pair found");
			return snipeTransactionRequest;
		}
		else  {
			System.out.println("no pair/Not listed");
			Thread.sleep(1000l);
			snipeOrderReQueue.send(snipeTransactionRequest);
		}
		
		return null;// pairCreatedEventChannel(snipeTransactionRequest);
	}
	
	@ServiceActivator(inputChannel = "liquidityEventOrReservesFinderChannel", outputChannel = "amountsInChannel")
	public SnipeTransactionRequest liquidityEventOrReservesFinderChannel(SnipeTransactionRequest snipeTransactionRequest) throws Exception{
		Tuple3<BigInteger, BigInteger, BigInteger> reserves = liquidityEventOrReserversFinder.fetchReserves(snipeTransactionRequest.getRoute(), 
																									        snipeTransactionRequest.getPairAddress(), 
																									        snipeTransactionRequest.getCredentials(),
																									        snipeTransactionRequest.getGasPrice(), 
																									        snipeTransactionRequest.getGasLimit(), 
																									        snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(), 
																									        snipeTransactionRequest.getGasMode());
		
		if(preChecksForTrade(snipeTransactionRequest, reserves) ) {
			System.out.println("Listed");
			snipeTransactionRequest.setReserves(mapReserves(reserves));
			return snipeTransactionRequest;
		}else {
			System.out.println("Not listed");
			Thread.sleep(1000l);
			//return liquidityEventOrReservesFinderChannel(snipeTransactionRequest);
			snipeOrderReQueue.send(snipeTransactionRequest);
		}
		return null;
	}

	private boolean preChecksForTrade(SnipeTransactionRequest snipeTransactionRequest,
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

	@ServiceActivator(inputChannel = "amountsInChannel", outputChannel = "swapETHForTokensChannel")
	public SnipeTransactionRequest amountsInChannel(SnipeTransactionRequest snipeTransactionRequest) throws Exception{
		try {
			if(snipeTransactionRequest.getExpectedOutPutToken() != null) {
				snipeTransactionRequest.setOuputTokenValueAmounttAsBigInteger(snipeTransactionRequest.getExpectedOutPutToken());
				return snipeTransactionRequest;
			}
			BigInteger outputTokens = ethereumDexTradeService.getAmountsIn(snipeTransactionRequest.getRoute(),
																		   snipeTransactionRequest.getCredentials(),
																		   snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(),
																		   snipeTransactionRequest.getSlipageInDouble(),
																		   Lists.newArrayList(new Address(snipeTransactionRequest.getToAddress()), 
																		   			  new Address( dexContractStaticCodeValuesService.getDexContractAddress(snipeTransactionRequest.getRoute(), TradeConstants.WNATIVE))),
																		   gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasPrice()),
																		   gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasLimit()),
																		   snipeTransactionRequest.getGasMode(),
																		   snipeTransactionRequest.getToAddressDecimals());

			if (outputTokens != null && outputTokens.compareTo(BigInteger.ZERO) > 0) {
				snipeTransactionRequest.setOuputTokenValueAmounttAsBigInteger(outputTokens);
				return snipeTransactionRequest;
			}else {
				System.err.println("output token zero");
				snipeTransactionRequest.setErrorMessage("output token zero");
				purgeMessage(snipeTransactionRequest);
			}
		} catch (Exception e) {
			if (StringUtils.containsIgnoreCase(e.getMessage(), TradeConstants.INSUFFICIENT_LIQUIDITY) || StringUtils.containsIgnoreCase(e.getMessage(), TradeConstants.DS_MATH_SUB_UNDERFLOW)) {
				//return amountsInChannel(snipeTransactionRequest);
				snipeOrderReQueue.send(snipeTransactionRequest);
			} else {
				System.err.println("error in amountsin purging order-> "+ e.getMessage());
				snipeTransactionRequest.setErrorMessage(e.getMessage());
				purgeMessage(snipeTransactionRequest);
			}
		} 
		return null;
	}
	
	@ServiceActivator(inputChannel = "swapETHForTokensChannel", outputChannel = "updateOrDeleteSnipeOrderChannel")
	public SnipeTransactionRequest swapETHForTokensChannel(SnipeTransactionRequest snipeTransactionRequest) throws Exception{
		try {
			String hash = ethereumDexTradeService.swapETHForTokens(snipeTransactionRequest.getRoute(),
																   snipeTransactionRequest.getCredentials(),
																   snipeTransactionRequest.getInputTokenValueAmountAsBigInteger(),
																   snipeTransactionRequest.getOuputTokenValueAmounttAsBigInteger(),
																   snipeTransactionRequest.getDeadLine(),
																   Lists.newArrayList(new Address( dexContractStaticCodeValuesService.getDexContractAddress(snipeTransactionRequest.getRoute(), TradeConstants.WNATIVE)),
																		 	  	      new Address(snipeTransactionRequest.getToAddress())),
																   snipeTransactionRequest.isFeeEligible(),
																   gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasPrice()),
																   gasProvider.getGasPrice(GasModeEnum.fromValue(snipeTransactionRequest.getGasMode()), snipeTransactionRequest.getGasLimit()),
																   snipeTransactionRequest.getGasMode());	
			if (StringUtils.isNotBlank(hash)) {
				System.out.println("snipe hash-> "+ hash);
				snipeTransactionRequest.setSwappedHash(hash);
				snipeTransactionRequest.setSnipeStatus(TradeConstants.FILLED);
				snipeTransactionRequest.setSnipe(true);
				snipeTransactionRequest.getAuditInformation().setUpdatedDateTime(Instant.now().toString());
				tradeOverviewRepository.save(mapRequestToTradeOverView(snipeTransactionRequest));
				return snipeTransactionRequest;
			}
		} catch (Exception e) {
			snipeTransactionRequest.setErrorMessage(e.getMessage());
			purgeMessage(snipeTransactionRequest);
		} 
	
		return null;
	}
	
	@ServiceActivator(inputChannel = "updateOrDeleteSnipeOrderChannel")
	public SnipeTransactionRequest updateOrDeleteSnipeOrderChannel(SnipeTransactionRequest snipeTransactionRequest) throws Exception{
		if (snipeTransactionRequest.hasSniped()) {
			purgeMessage(snipeTransactionRequest);
		} 
		return null;
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
