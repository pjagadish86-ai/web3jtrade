package com.aitrades.blockchain.web3jtrade.repository;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

import com.aitrades.blockchain.web3jtrade.domain.DexContractStaticCodeValue;

@Repository
public class DexContractStaticCodeValueRepository {

	@Resource(name = "dexCntrStcCdsReactiveMongoTemplate")
	private ReactiveMongoTemplate dexCntrStcCdsReactiveMongoTemplate;
	
	public List<DexContractStaticCodeValue> fetchAllDexContractRouterAndFactoryAddress() {
		return dexCntrStcCdsReactiveMongoTemplate.findAll(DexContractStaticCodeValue.class).collectList().block();
	}
	
}
