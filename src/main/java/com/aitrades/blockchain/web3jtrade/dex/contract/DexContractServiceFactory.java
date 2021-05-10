package com.aitrades.blockchain.web3jtrade.dex.contract;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aitrades.blockchain.web3jtrade.dex.generic.GenericSwapService;
import com.aitrades.blockchain.web3jtrade.dex.pancake.PancakeServiceImpl;
import com.aitrades.blockchain.web3jtrade.dex.uniswap.UniswapV3Service;
import com.aitrades.blockchain.web3jtrade.dex.uniswap.UniswapV3ServiceImpl;

@Service
public class DexContractServiceFactory {

	private static final String UNISWAPV3 = "UNISWAPV3";

	@Autowired
	private Map<String, DexContractService> typesMap;
	
	@Autowired
	private PancakeServiceImpl pancakeServiceImpl;
	
	@Autowired
	private UniswapV3ServiceImpl uniswapV3ServiceImpl;
	
	
	@Autowired
	private UniswapV3Service uniswapV3Service;
	
	@Autowired
	private GenericSwapService genericSwapService;
	
	@PostConstruct
	public void init() {
		typesMap.put("3", pancakeServiceImpl);
		typesMap.put(UNISWAPV3, uniswapV3ServiceImpl);
	}
	
	public DexContractService getInstance(String condition) {
		return typesMap.get(condition) != null ? typesMap.get(condition) : genericSwapService;
	}
	
	public DexContractServiceV3 getV3Instance(String condition) {
		if(StringUtils.equalsIgnoreCase(condition, UNISWAPV3)) {
			return uniswapV3Service;
		}
		return null;
	}

}