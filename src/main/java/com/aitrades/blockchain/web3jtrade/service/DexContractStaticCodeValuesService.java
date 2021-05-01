package com.aitrades.blockchain.web3jtrade.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aitrades.blockchain.web3jtrade.domain.DexContractStaticCodeValue;
import com.aitrades.blockchain.web3jtrade.domain.TradeConstants;
import com.aitrades.blockchain.web3jtrade.repository.DexContractStaticCodeValueRepository;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
public class DexContractStaticCodeValuesService {
	
	private static com.github.benmanes.caffeine.cache.Cache<String, Map<String, String>> dexContractAddress;
	
	@Autowired
	private DexContractStaticCodeValueRepository dexContractStaticCodeValueRepository;
	
	@Autowired
    private DexContractStaticCodeValuesService() {
        dexContractAddress = Caffeine.newBuilder()
	                               .expireAfterWrite(3, TimeUnit.HOURS)
	                               .build();
    }
	
	private Map<String, String> getDexContractAddress(String route){
        return dexContractAddress.get(route, this :: getStaticCodeValuesMap);
	}
	
	public String getDexContractAddress(String route, String type) {
		getDexContractAddress(route);
		return dexContractAddress.getIfPresent(route) != null ? dexContractAddress.getIfPresent(route).get(type) : null;
	}
	
	private Map<String, String> getStaticCodeValuesMap(String route) {
		List<DexContractStaticCodeValue> staticCodeValues = dexContractStaticCodeValueRepository.fetchAllDexContractRouterAndFactoryAddress();
		Map<String, String> contractAddress = new HashMap<>();	
		for(DexContractStaticCodeValue dexContractStaticCodeValue : staticCodeValues) {
			if(StringUtils.equalsIgnoreCase(route, dexContractStaticCodeValue.getDexName())) {
				contractAddress.put(TradeConstants.ROUTER, dexContractStaticCodeValue.getRouterAddress());
				contractAddress.put(TradeConstants.FACTORY, dexContractStaticCodeValue.getFactoryAddress());
				contractAddress.put(TradeConstants.WNATIVE, dexContractStaticCodeValue.getWrappedNativeAddress());
				return contractAddress;
			}
		}
		return null;
	}

}
