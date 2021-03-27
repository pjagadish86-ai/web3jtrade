package com.aitrades.blockchain.web3jtrade.side;


public enum OrderType {
	SNIPE("SNIPE", 1),
	MARKET("MARKET", 2),
	LIMIT("LIMIT", 3),
	STOPLOSS("STOPLOSS", 4),
	STOPLIMIT("STOPLIMIT", 5),
	TRAILLING_STOP("TRAILLING_STOP", 6),
	LIMIT_TRAILLING_STOP("LIMIT_TRAILLING_STOP", 7);

	private final String value;
	private final Integer sortorder;
	
	private OrderType(String value, Integer sortorder) {
		this.value = value;
		this.sortorder = sortorder;
	}
	
	public String getValue() {
		return value;
	}

	public Integer getSortorder() {
		return sortorder;
	}

	public static OrderType fromValue(String value) {
		for (final OrderType orderType : OrderType.values()) {
			if (orderType.value.equals(value)) {
				return orderType;
			}
		}
		throw new IllegalArgumentException(value);
	}

	public static OrderType fromName(String name) {

		for (final OrderType orderType : OrderType.values()) {
			if (orderType.name().equals(name)) {
				return orderType;
			}
		}
		throw new IllegalArgumentException(name);

	}
	
}
