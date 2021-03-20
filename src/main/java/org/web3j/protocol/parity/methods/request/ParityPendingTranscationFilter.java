package org.web3j.protocol.parity.methods.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ParityPendingTranscationFilter {
	private EQ from;
	private EQ to;
	private LogicalOperations gas;
	private LogicalOperations gas_price;
	private LogicalOperations value;
	private LogicalOperations nonce;

	public ParityPendingTranscationFilter() {
	}

	public ParityPendingTranscationFilter(EQ from, EQ to, LogicalOperations gas, LogicalOperations gas_price,
			LogicalOperations value, LogicalOperations nonce) {
		super();
		this.from = from;
		this.to = to;
		this.gas = gas;
		this.gas_price = gas_price;
		this.value = value;
		this.nonce = nonce;
	}

	public EQ getFrom() {
		return from;
	}

	public EQ getTo() {
		return to;
	}

	public LogicalOperations getGas() {
		return gas;
	}

	public LogicalOperations getGas_price() {
		return gas_price;
	}

	public LogicalOperations getValue() {
		return value;
	}

	public LogicalOperations getNonce() {
		return nonce;
	}

}
