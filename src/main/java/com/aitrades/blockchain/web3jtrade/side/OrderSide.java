package com.aitrades.blockchain.web3jtrade.side;


public enum OrderSide {

	BUY("BUY", 1), SELL("SELL", 2);

	private final String value;
	private final Integer sortorder;

	private OrderSide(String value, Integer sortorder) {
		this.value = value;
		this.sortorder = sortorder;
	}

	public String value() {
		return value;
	}

	public Integer getSortorder() {
		return sortorder;
	}

	public static OrderSide fromValue(String value) {
		for (final OrderSide tradeSide : OrderSide.values()) {
			if (tradeSide.value.equals(value)) {
				return tradeSide;
			}
		}
		throw new IllegalArgumentException(value);
	}

	public static OrderSide fromName(String name) {

		for (final OrderSide tradeSide : OrderSide.values()) {
			if (tradeSide.name().equals(name)) {
				return tradeSide;
			}
		}
		throw new IllegalArgumentException(name);

	}
}
