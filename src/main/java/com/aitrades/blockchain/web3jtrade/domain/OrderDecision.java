package com.aitrades.blockchain.web3jtrade.domain;

public enum OrderDecision {
	
	BUY("BUY",1), SELL("SELL",2), SNIPE("SNIPE", 3), INSTANT("INSTANT", 4);
	
	private final String value;
	private final Integer sortorder;
	
	private OrderDecision(String value, Integer sortorder) {
		this.value = value;
		this.sortorder = sortorder;
	}
	
	public String getValue() {
		return value;
	}
	public Integer getSortorder() {
		return sortorder;
	}
	
	public static OrderDecision fromValue(String value) {
		for (final OrderDecision orderDecision : OrderDecision.values()) {
			if (orderDecision.value.equals(value)) {
				return orderDecision;
			}
		}
		throw new IllegalArgumentException(value);
	}

	public static OrderDecision fromName(String name) {

		for (final OrderDecision orderDecision : OrderDecision.values()) {
			if (orderDecision.name().equals(name)) {
				return orderDecision;
			}
		}
		throw new IllegalArgumentException(name);

	}
}
