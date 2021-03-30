package com.aitrades.blockchain.web3jtrade.dex.contract;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aitrades.blockchain.web3jtrade.dex.pancake.PancakeServiceImpl;
import com.aitrades.blockchain.web3jtrade.dex.sushi.SushiServiceImpl;
import com.aitrades.blockchain.web3jtrade.dex.uniswap.UniswapServiceImpl;
import com.aitrades.blockchain.web3jtrade.dex.uniswap.UniswapServiceImplVersion2;

@Service
public class EthereumDexContractServiceFactory {

	
	@Autowired
	private Map<String, EthereumDexContractService> typesMap;
	
	@Autowired
	private UniswapServiceImpl uniswapServiceImpl;
	
	@Autowired
	private UniswapServiceImplVersion2 uniswapServiceImplVersion2;
	
	@Autowired
	private SushiServiceImpl sushiServiceImpl;
	
	@Autowired
	private PancakeServiceImpl pancakeServiceImpl;
	

	@PostConstruct
	public void init() {
		typesMap.put("UNISWAP", uniswapServiceImpl);
		typesMap.put("SUSHI", sushiServiceImpl);
		typesMap.put("PANCAKE", pancakeServiceImpl);
		typesMap.put("UNISWAPV2", uniswapServiceImplVersion2);
	}
	
	public EthereumDexContractService getInstance(String condition) {
		return typesMap.get(condition);
	}

}