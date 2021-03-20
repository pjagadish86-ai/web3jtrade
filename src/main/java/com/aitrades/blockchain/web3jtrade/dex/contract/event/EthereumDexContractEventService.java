package com.aitrades.blockchain.web3jtrade.dex.contract.event;

import java.math.BigInteger;
import java.util.Arrays;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import io.reactivex.Flowable;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.5.5.
 */
public class EthereumDexContractEventService extends Contract {
	
    private static final String BINARY = "Bin file was not provided";

    public static final String FUNC_ADDLIQUIDITY = "addLiquidity";
    
    public static final String FUNC_ADDLIQUIDITYETH = "addLiquidityEth";

    public static final Event ADDLIQUIDITY_EVENT = new Event(FUNC_ADDLIQUIDITY, 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>(true) {}));
    ;

    public static final Event ADDLIQUIDITYETH_EVENT = new Event(FUNC_ADDLIQUIDITYETH, 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>(true) {}));
    ;


    protected EthereumDexContractEventService(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }


    protected EthereumDexContractEventService(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    
    public static EthereumDexContractEventService load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new EthereumDexContractEventService(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static EthereumDexContractEventService load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new EthereumDexContractEventService(contractAddress, web3j, transactionManager, contractGasProvider);
    }
    
    /****************************
     *  add liquidity eth event  *
     ****************************/
    public Flowable<AddLiquidityEventResponse> addLiquidityEthEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ADDLIQUIDITYETH_EVENT));
        return addLiquidityEthEventFlowable(filter);
    }
    
    private Flowable<AddLiquidityEventResponse> addLiquidityEthEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter)
        		    .map(this :: mapAddLiqiudityEthEventResponse);
    }

    private AddLiquidityEventResponse  mapAddLiqiudityEthEventResponse(Log log){
    	Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(ADDLIQUIDITYETH_EVENT, log);
        return populateResponse(log, eventValues);
    }
    

    public Flowable<AddLiquidityEventResponse> addLiquidityEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ADDLIQUIDITY_EVENT));
        return addLiquidityEventFlowable(filter);
    }
    
    private Flowable<AddLiquidityEventResponse> addLiquidityEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter)
        			.map(this :: mapAddLiqiudityEventResponse);
    }

    private AddLiquidityEventResponse mapAddLiqiudityEventResponse(Log log){
    	Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(ADDLIQUIDITY_EVENT, log);
        return populateResponse(log, eventValues);
    }
    
    private AddLiquidityEventResponse populateResponse(Log log, Contract.EventValuesWithLog eventValues) {
		AddLiquidityEventResponse typedResponse = new AddLiquidityEventResponse();
        typedResponse.log = log;
        typedResponse.provider = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.eth_amount = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.token_amount = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
        return typedResponse;
	}
    
    public static class AddLiquidityEventResponse extends BaseEventResponse {
    	
        public String provider;

        public BigInteger eth_amount;

        public BigInteger token_amount;
    }

}
