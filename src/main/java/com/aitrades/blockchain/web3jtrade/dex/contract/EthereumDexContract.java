package com.aitrades.blockchain.web3jtrade.dex.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;

@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class EthereumDexContract extends Contract {
    public static final String BIN_NOT_PROVIDED = "Bin file was not provided";
	private static final String FUNC_GETRESERVES = "getReserves";
	public static final String FUNC_GETAMOUNTSOUT = "getAmountsOut";
    public static final String FUNC_GETAMOUNTSIN = "getAmountsIn";
    
	public EthereumDexContract(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
		super(BIN_NOT_PROVIDED, contractAddress, web3j, credentials, gasPrice, gasLimit);
	}

	public RemoteFunctionCall<List> getAmountsOut(BigInteger amountIn, List<String> path) {
        final Function function = new Function(FUNC_GETAMOUNTSOUT, 
                Arrays.<Type>asList(new Uint256(amountIn), 
                new DynamicArray<Address>(
                        Address.class, getAddress(path))), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
					@Override
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    

	public RemoteFunctionCall<List> getAmountsIn(BigInteger amountOut, List<String> path) {
        final Function function = new Function(FUNC_GETAMOUNTSIN, 
                Arrays.<Type>asList(new Uint256(amountOut), 
                new DynamicArray<Address>(Address.class, getAddress(path))), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }
	
	private List<Address> getAddress(List<String> path) {
		List<Address>  addresses = new ArrayList<Address>();
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
}
