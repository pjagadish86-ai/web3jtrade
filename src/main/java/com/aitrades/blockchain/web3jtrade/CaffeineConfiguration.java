package com.aitrades.blockchain.web3jtrade;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CaffeineConfiguration {
    
	@Bean
    public Caffeine caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.MINUTES);
    }

    @Bean
    public CacheManager cacheManager(Caffeine caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.getCache("routePrice");
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }
}