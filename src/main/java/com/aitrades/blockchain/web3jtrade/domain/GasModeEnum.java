package com.aitrades.blockchain.web3jtrade.domain;

public enum GasModeEnum {
	ULTRA("ultra",1), 
	FASTEST("fastest",2), 	
	FAST("fast",3), 
	STANDARD("standard",4), 	
	SAFELOW("safelow",5);
	
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
			if (gasMode.value.equalsIgnoreCase(value)) {
				return gasMode;
			}
		}
		return GasModeEnum.STANDARD;
	}

	public static GasModeEnum fromName(String name) {

		for (final GasModeEnum gasMode : GasModeEnum.values()) {
			if (gasMode.name().equalsIgnoreCase(name)) {
				return gasMode;
			}
		}
		return GasModeEnum.STANDARD;

	}
}
