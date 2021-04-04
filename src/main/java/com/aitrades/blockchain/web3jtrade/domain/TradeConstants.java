package com.aitrades.blockchain.web3jtrade.domain;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public final class TradeConstants {
	
	public static final String UNISWAP = "UNISWAP";
	public static final String UNISWAP_FACOTRYADDRESS ="0x5c69bee701ef814a2b6a3edd4b1652cb9cc5aa6f";
	public static final String UNISWAP_ROUTERADDRESS ="0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D";
	public static final String UNISWAP_WETH_FACOTRYADDRESS ="0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2";

	
	public static final String PANCAKE = "PANCAKE";
	public static final String PANCAKE_FACTORY_ADDRESS = "0xbcfccbde45ce874adcb698cc183debcf17952812";
	public static final String PANCAKE_ROUTER_ADDRESS = "0x05ff2b0db69458a0750badebc4f9e13add608c7f";
	public static final String PANCAKE_WBNB_FACOTRYADDRESS ="0xbb4cdb9cbd36b01bd1cbaebf2de08d9173bc095c";

	public static final String SUSHI = "SUSHI";
	public static final String SUSHI_FACTORY_ADDRESS = "0xc0aee478e3658e2610c5f7a4a2e1777ce9e4f2ac";
	public static final String SUSHI_ROUTER_ADDRESS = "0xd9e1cE17f2641f24aE83637ab66a2cca9C378B9F";
	public static final String SUSHI_WETH_FACOTRYADDRESS ="0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2";
	
	public static final String ORDER_DECISION = "ORDER_DECISION";

	public static final String SNIPETRANSACTIONREQUEST = "SNIPETRANSACTIONREQUEST";
    public static final String SWAP_ETH_FOR_TOKEN_HASH = "SWAP_ETH_FOR_TOKEN_HASH";
	public static final String APPROVE_HASH = "APPROVE_HASH";
	public static final String PAIR_CREATED = "PAIR_CREATED";
	public static final String HAS_RESERVES = "HAS_RESERVES";
	public static final String HAS_LIQUIDTY_EVENT = "HAS_LIQUIDTY_EVENT";
	
	public static final String OUTPUT_TOKENS = "OUTPUT_TOKENS";
	public static final String INPUT_TOKENS = "INPUT_TOKENS";
	
	public static final Map<String, String> ROUTER_MAP = ImmutableMap.of(UNISWAP, UNISWAP_ROUTERADDRESS, PANCAKE, PANCAKE_ROUTER_ADDRESS, SUSHI, SUSHI_ROUTER_ADDRESS);
	
	public static final Map<String, String> FACTORY_MAP = ImmutableMap.of(UNISWAP, UNISWAP_FACOTRYADDRESS, PANCAKE, PANCAKE_FACTORY_ADDRESS, SUSHI, SUSHI_FACTORY_ADDRESS);
	
	public static final Map<String, String> WETH_MAP = ImmutableMap.of(UNISWAP, UNISWAP_WETH_FACOTRYADDRESS, PANCAKE, PANCAKE_WBNB_FACOTRYADDRESS, SUSHI, SUSHI_WETH_FACOTRYADDRESS);
	
	public static final String ETH = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2";

	public static final String APPROVE_HASH_ISAVAILABLE = "APPROVE_HASH_ISAVAILABLE";

	public static final String FILLED = "FILLED";

	public static final String ORDER = "ORDER";

	public static final String SWAP_TOKEN_FOR_ETH_HASH = "SWAP_TOKEN_FOR_ETH_HASH";
	
	
}
