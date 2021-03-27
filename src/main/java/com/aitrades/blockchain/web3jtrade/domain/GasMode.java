package com.aitrades.blockchain.web3jtrade.domain;

public enum GasMode {
	SNIP("SNIP",1), 
	FASTEST("RAPID",2), 	
	FAST("FAST",3), 
	STANDARD("AVERAGE",4), 	
	SAFELOW("SLOW",5);
	
	private final String value;
	private final Integer sortorder;
	
	private GasMode(String value, Integer sortorder) {
		this.value = value;
		this.sortorder = sortorder;
	}
	
	public String getValue() {
		return value;
	}
	public Integer getSortorder() {
		return sortorder;
	}
	
	public static GasMode fromValue(String value) {
		for (final GasMode gasMode : GasMode.values()) {
			if (gasMode.value.equals(value)) {
				return gasMode;
			}
		}
		throw new IllegalArgumentException(value);
	}

	public static GasMode fromName(String name) {

		for (final GasMode gasMode : GasMode.values()) {
			if (gasMode.name().equals(name)) {
				return gasMode;
			}
		}
		throw new IllegalArgumentException(name);

	}
}
