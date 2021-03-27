package com.aitrades.blockchain.web3jtrade.side;


public enum OrderState {

	FILLED("FILLED", 1),
	PARTIAL_FILLED("PARTIAL_FILLED", 2),
	WORKING("WORKING", 3),
	CANCELLED("CANCELLED", 4);
	
	private final String value;
	private final Integer sortorder;
	
	private OrderState(String value, Integer sortorder) {
		this.value = value;
		this.sortorder = sortorder;
	}
	
	
	public String getValue() {
		return value;
	}
	public Integer getSortorder() {
		return sortorder;
	}
	
	public static OrderState fromValue(String value) {
		for (final OrderState orderStatus : OrderState.values()) {
			if (orderStatus.value.equals(value)) {
				return orderStatus;
			}
		}
		throw new IllegalArgumentException(value);
	}

	public static OrderState fromName(String name) {

		for (final OrderState orderType : OrderState.values()) {
			if (orderType.name().equals(name)) {
				return orderType;
			}
		}
		throw new IllegalArgumentException(name);

	}
	
}
