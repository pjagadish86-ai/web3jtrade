package com.aitrades.blockchain.web3jtrade.domain;

public enum GasModeEnum {
	SNIP("SNIP",1), 
	FASTEST("RAPID",2), 	
	FAST("FAST",3), 
	STANDARD("AVERAGE",4), 	
	SAFELOW("SLOW",5);
	
	private final String value;
	private final Integer sortorder;
	
	private GasModeEnum(String value, Integer sortorder) {
		this.value = value;
		this.sortorder = sortorder;
	}
	
	public String getValue() {
		return value;
	}
	public Integer getSortorder() {
		return sortorder;
	}
	
	public static GasModeEnum fromValue(String value) {
		for (final GasModeEnum gasMode : GasModeEnum.values()) {
			if (gasMode.value.equals(value)) {
				return gasMode;
			}
		}
		throw new IllegalArgumentException(value);
	}

	public static GasModeEnum fromName(String name) {

		for (final GasModeEnum gasMode : GasModeEnum.values()) {
			if (gasMode.name().equals(name)) {
				return gasMode;
			}
		}
		throw new IllegalArgumentException(name);

	}
}
