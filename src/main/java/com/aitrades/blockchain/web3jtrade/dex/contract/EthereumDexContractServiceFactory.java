package com.aitrades.blockchain.web3jtrade.dex.contract;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EthereumDexContractServiceFactory {

	@Autowired
	private Map<String, EthereumDexContractService> typesMap;

	public EthereumDexContractService getInstance(String condition) {
		return typesMap.get(condition);
	}

}