package org.web3j.protocol.parity.methods.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ParityPendingTransactionRequest {

	private Integer limit;
	private ParityPendingTranscationFilter filter;
	@JsonCreator
	public ParityPendingTransactionRequest(@JsonProperty("limit") Integer limit, @JsonProperty("filter")  ParityPendingTranscationFilter filter) {
		this.limit = limit;
		this.filter = filter;
	}
	public Integer getLimit() {
		return limit;
	}
	public ParityPendingTranscationFilter getFilter() {
		return filter;
	}
}
