package com.aitrades.blockchain.web3jtrade.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.github.benmanes.caffeine.cache.Caffeine;

@Repository
public class LiquidityAvailableCacheService {

	private static com.github.benmanes.caffeine.cache.Cache<String, Boolean> blockchainExchangeCache;

	@Autowired
    public LiquidityAvailableCacheService() {
        blockchainExchangeCache = Caffeine.newBuilder()
                .expireAfterWrite(3, TimeUnit.HOURS)
                .build();
    }
}
