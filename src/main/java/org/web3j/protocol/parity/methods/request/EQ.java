package org.web3j.protocol.parity.methods.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class EQ {
	private String eq;

	public String getEq() {
		return eq;
	}

	public EQ() {
	}

	public EQ(String eq) {
		super();
		this.eq = eq;
	}
	
}
