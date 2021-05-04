package com.aitrades.blockchain.web3jtrade.dex.contract;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aitrades.blockchain.web3jtrade.dex.generic.GenericSwapService;
import com.aitrades.blockchain.web3jtrade.dex.pancake.PancakeServiceImpl;

@Service
public class DexContractServiceFactory {

	private static final String PANCAKE = "PANCAKE";

	@Autowired
	private Map<String, DexContractService> typesMap;
	
	@Autowired
	private PancakeServiceImpl pancakeServiceImpl;
	
	@Autowired
	private GenericSwapService genericSwapService;
	
	
	@PostConstruct
	public void init() {
		typesMap.put(PANCAKE, pancakeServiceImpl);
	}
	
	public DexContractService getInstance(String condition) {
		return typesMap.get(condition) != null ? typesMap.get(condition) : genericSwapService;
	}

}