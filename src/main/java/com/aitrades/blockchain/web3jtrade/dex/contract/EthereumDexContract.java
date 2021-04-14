package com.aitrades.blockchain.web3jtrade.dex.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint112;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class EthereumDexContract extends Contract {
    public static final String BIN_NOT_PROVIDED = "Bin file was not provided";
	private static final String FUNC_GETRESERVES = "getReserves";
	public static final String FUNC_GETAMOUNTSOUT = "getAmountsOut";
    public static final String FUNC_GETAMOUNTSIN = "getAmountsIn";
    
    public static final String FUNC_TRANSFER = "transfer";
    public static final String FUNC_TRANSFERFROM = "transferFrom";
    public static final String FUNC_WITHDRAW = "withdraw";
    public static final String FUNC_DEPOSIT = "deposit";
    
    public static final String FUNC_SWAPTOKENSFOREXACTTOKENS = "swapTokensForExactTokens";

    private static final ContractGasProvider CONTRACT_GAS_PROVIDER = new DefaultGasProvider();
    
    public EthereumDexContract(String contractAddress, Web3j web3j, Credentials credentials) {
		super(BIN_NOT_PROVIDED, contractAddress, web3j, credentials, CONTRACT_GAS_PROVIDER);
	}
    
	public EthereumDexContract(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
		super(BIN_NOT_PROVIDED, contractAddress, web3j, credentials, gasPrice, gasLimit);
	}

	public RemoteFunctionCall<List> getAmountsOut(BigInteger amountIn, List<String> path) {
        final Function function = new Function(FUNC_GETAMOUNTSOUT, 
                Arrays.<Type>asList(new Uint256(amountIn), 
                new DynamicArray<Address>(
                        Address.class, getAddress(path))), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteFunctionCall<>(function,
                new Callable<List>() {
					@Override
                    public List call() throws Exception {
                        List<Type> result = executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    

	public RemoteFunctionCall<List> getAmountsIn(BigInteger amountOut, List<Address> path) {
        final Function function = new Function(FUNC_GETAMOUNTSIN, 
                Arrays.<Type>asList(new Uint256(amountOut), 
                new DynamicArray<Address>(Address.class, path)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteFunctionCall<>(function,
                new Callable<List>() {
                    @Override
                    public List call() throws Exception {
                        List<Type> result = executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }
	
	private List<Address> getAddress(List<String> path) {
		List<Address>  addresses = new ArrayList<>();
		for(String addr : path) {
			addresses.add(new Address(addr));
		}
		return addresses;
	}

	public RemoteFunctionCall<Tuple3<BigInteger, BigInteger, BigInteger>> getReserves() {
        final Function function = new Function(FUNC_GETRESERVES, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint112>() {}, new TypeReference<Uint112>() {}, new TypeReference<Uint32>() {}));
        return new RemoteFunctionCall<>(function,
                new Callable<Tuple3<BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple3<BigInteger, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        if(results == null || results.isEmpty()) {
                        	return new Tuple3<>(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
                        }
                        return new Tuple3<>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue());
                    }
                });
    }
	
	
	  public RemoteFunctionCall<TransactionReceipt> deposit(BigInteger weiValue) {
	        final Function function = new Function(
	                FUNC_DEPOSIT, 
	                Arrays.<Type>asList(), 
	                Collections.<TypeReference<?>>emptyList());
	        return executeRemoteCallTransaction(function, weiValue);
	    }
	  
	    public RemoteFunctionCall<TransactionReceipt> withdraw(BigInteger wad) {
	        final Function function = new Function(
	                FUNC_WITHDRAW, 
	                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(wad)), 
	                Collections.<TypeReference<?>>emptyList());
	        return executeRemoteCallTransaction(function);
	    }
	    
	    public RemoteFunctionCall<TransactionReceipt> transferFrom(String src, String dst, BigInteger wad) {
	        final Function function = new Function(
	                FUNC_TRANSFERFROM, 
	                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, src), 
	                new org.web3j.abi.datatypes.Address(160, dst), 
	                new org.web3j.abi.datatypes.generated.Uint256(wad)), 
	                Collections.<TypeReference<?>>emptyList());
	        return executeRemoteCallTransaction(function);
	    }
	    
	    public RemoteFunctionCall<TransactionReceipt> transfer(String dst, BigInteger wad) {
	        final Function function = new Function(
	                FUNC_TRANSFER, 
	                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, dst), 
	                new org.web3j.abi.datatypes.generated.Uint256(wad)), 
	                Collections.<TypeReference<?>>emptyList());
	        return executeRemoteCallTransaction(function);
	    }

	    
	    public RemoteFunctionCall<TransactionReceipt> swapTokensForExactTokens(BigInteger amountOut, BigInteger amountInMax, List<String> path, String to, BigInteger deadline) {
	        final Function function = new Function(
	                FUNC_SWAPTOKENSFOREXACTTOKENS, 
	                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(amountOut), 
	                new org.web3j.abi.datatypes.generated.Uint256(amountInMax), 
	                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
	                        org.web3j.abi.datatypes.Address.class,
	                        org.web3j.abi.Utils.typeMap(path, org.web3j.abi.datatypes.Address.class)), 
	                new org.web3j.abi.datatypes.Address(160, to), 
	                new org.web3j.abi.datatypes.generated.Uint256(deadline)), 
	                Collections.<TypeReference<?>>emptyList());
	        return executeRemoteCallTransaction(function);
	    }


}
