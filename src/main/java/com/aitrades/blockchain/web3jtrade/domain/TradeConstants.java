package com.aitrades.blockchain.web3jtrade.domain;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public final class TradeConstants {
	public static final String UNISWAP = "UNISWAP";
	
	public static final String UNISWAP_ROUTERADDRESS ="0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D";
	public static final String SUSHI_ROUTERADDRESS ="";
	public static final String PANCAKE_ROUTERADDRESS ="";
	
	public static final String UNISWAP_FACOTRYADDRESS ="0x5c69bee701ef814a2b6a3edd4b1652cb9cc5aa6f";
	public static final String SUSHI_FACOTRYADDRESS ="";
	public static final String PANCAKE_FACOTRYADDRESS ="";
	public static final String SNIPETRANSACTIONREQUEST = "SNIPETRANSACTIONREQUEST";
	public static final String ORDER_DECISION = "ORDER_DECISION";
	
    public static final String ROUTER_ADDRESS = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D";
    public static final String SWAP_ETH_FOR_TOKEN_HASH = "SWAP_ETH_FOR_TOKEN_HASH";
	public static final String APPROVE_HASH = "APPROVE_HASH";
	public static final String PAIR_CREATED = "PAIR_CREATED";
	public static final String HAS_RESERVES = "HAS_RESERVES";
	public static final String HAS_LIQUIDTY_EVENT = "HAS_LIQUIDTY_EVENT";
	public static final String OUTPUT_TOKENS = "OUTPUT_TOKENS";
	
	public static final Map<String, String> ROUTER_MAP = ImmutableMap.of(UNISWAP, UNISWAP_ROUTERADDRESS);
	
	public static final String APPROVE_HASH_ISAVAILABLE = "APPROVE_HASH_ISAVAILABLE";
	
	
}
