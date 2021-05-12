package com.aitrades.blockchain.web3jtrade.domain;

import java.util.HashMap;
import java.util.Map;

public final class TradeConstants {
	
	public static final String UNISWAP = "UNISWAP";
	public static final String WNATIVE = "WNATIVE";
	public static final String FACTORY = "FACTORY";
	public static final String USD = "USD";
	public static final String WUSD = "WUSD";
	public static final String ROUTER = "ROUTER";
	
	public static final String PANCAKE = "PANCAKE";
	public static final String BUSD = "0xe9e7cea3dedca5984780bafc599bd69add087d56";
	
	public static final String SUSHI = "SUSHI";
	
	public static final String ORDER_DECISION = "ORDER_DECISION";

	public static final String SNIPETRANSACTIONREQUEST = "SNIPETRANSACTIONREQUEST";
    public static final String SWAP_ETH_FOR_TOKEN_HASH = "SWAP_ETH_FOR_TOKEN_HASH";
	public static final String APPROVE_HASH = "APPROVE_HASH";
	public static final String PAIR_CREATED = "PAIR_CREATED";
	public static final String HAS_RESERVES = "HAS_RESERVES";
	public static final String HAS_LIQUIDTY_EVENT = "HAS_LIQUIDTY_EVENT";
	
	public static final String OUTPUT_TOKENS = "OUTPUT_TOKENS";
	public static final String INPUT_TOKENS = "INPUT_TOKENS";
	
	
	public static final String ETH = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2";

	public static final String APPROVE_HASH_ISAVAILABLE = "APPROVE_HASH_ISAVAILABLE";

	public static final String FILLED = "FILLED";

	public static final String ORDER = "ORDER";
	public static final String CUSTOM = "CUSTOM";


	public static final String SWAP_TOKEN_FOR_ETH_HASH = "SWAP_TOKEN_FOR_ETH_HASH";
	
	public static final String CREDENTIALS = "CREDENTIALS";
	
	public static final String HAS_LIQUIDTY_EVENT_OR_HAS_RESERVES = "HAS_LIQUIDTY_EVENT_OR_HAS_RESERVES";
	
	public static final String INSUFFICIENT_LIQUIDITY = "INSUFFICIENT_LIQUIDITY";
	public static final String DS_MATH_SUB_UNDERFLOW = "ds-math-sub-underflow";
	public static final String FAILED = "FAILED";
	public static final String SNIPE = "SNIPE";
	public static final String _0X000000 = "0x000000";
	public static final String TOADDRESS = "TOADDRESS";
	public static final String PUBLICKEY = "PUBLICKEY";
	public static final String AMOUNTS_IN = "AMOUNTS_IN";
	public static final String AMOUNTS_OUT = "AMOUNTS_OUT";
	
	public static final String ROUTE = "ROUTE";

	public static final String GAS_LIMIT = "GAS_LIMIT";

	public static final String GAS_PRICE = "GAS_PRICE";
	public static final String SIGNED_TRANSACTION = "SIGNED_TRANSACTION";
	
	public static Map<String, String> DECIMAL_MAP = null;
	
	static {
		DECIMAL_MAP = new HashMap<String, String>();
		DECIMAL_MAP.put("0", "WEI");
		DECIMAL_MAP.put("3", "KWEI");
		DECIMAL_MAP.put("6", "MWEI");
		DECIMAL_MAP.put("9", "GWEI");
		DECIMAL_MAP.put("12", "SZABO");
		DECIMAL_MAP.put("15", "FINNEY");
		DECIMAL_MAP.put("18", "ETHER");
		DECIMAL_MAP.put("21", "KETHER");
		DECIMAL_MAP.put("24", "METHER");
		DECIMAL_MAP.put("27", "GETHER");
		
	}
	
	private static final String ETHERSCAN = "https://etherscan.io/tx/";
	private static final String BSC_SCAN = "https://bscscan.com/tx/";
	private static final String FTM_SCAN = "https://ftmscan.com/tx/";
	private static final String WMATIC_SCAN = "https://ftmscan.com/tx/"; //FIXME
	
	public static Map<String, String> SCAN_API_URL = new HashMap<>();
	
	static {
		SCAN_API_URL.put("1", ETHERSCAN);
		SCAN_API_URL.put("2", ETHERSCAN);
		SCAN_API_URL.put("3", BSC_SCAN);
		SCAN_API_URL.put("4", FTM_SCAN);
		SCAN_API_URL.put("5", FTM_SCAN);
		SCAN_API_URL.put("6", FTM_SCAN);
		SCAN_API_URL.put("7", FTM_SCAN);
		SCAN_API_URL.put("8", WMATIC_SCAN);
	}
	
}
