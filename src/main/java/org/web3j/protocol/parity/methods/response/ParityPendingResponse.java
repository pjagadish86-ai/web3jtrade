package org.web3j.protocol.parity.methods.response;

import java.math.BigInteger;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class ParityPendingResponse {

	public String blockHash;
	public BigInteger blockNumber;
	public String creates;
	public String from;
	public String gas;
	public String gasPrice;
	public String hash;
	public String input;
	public Condition condition;
	public Integer chainId;
	public String nonce;
	public String publicKey;
	public String r;
	public String raw;
	public String s;
	public String standardV;
	public String to;
	public Object transactionIndex;
	public String v;
	public String value;

	public ParityPendingResponse() {
	}

	public ParityPendingResponse(String blockHash, BigInteger blockNumber, String creates, String from, String gas,
			String gasPrice, String hash, String input, Condition condition, Integer chainId, String nonce,
			String publicKey, String r, String raw, String s, String standardV, String to, Object transactionIndex,
			String v, String value) {
		super();
		this.blockHash = blockHash;
		this.blockNumber = blockNumber;
		this.creates = creates;
		this.from = from;
		this.gas = gas;
		this.gasPrice = gasPrice;
		this.hash = hash;
		this.input = input;
		this.condition = condition;
		this.chainId = chainId;
		this.nonce = nonce;
		this.publicKey = publicKey;
		this.r = r;
		this.raw = raw;
		this.s = s;
		this.standardV = standardV;
		this.to = to;
		this.transactionIndex = transactionIndex;
		this.v = v;
		this.value = value;
	}

	public String getBlockHash() {
		return blockHash;
	}

	public BigInteger getBlockNumber() {
		return blockNumber;
	}

	public String getCreates() {
		return creates;
	}

	public String getFrom() {
		return from;
	}

	public String getGas() {
		return gas;
	}

	public String getGasPrice() {
		return gasPrice;
	}

	public String getHash() {
		return hash;
	}

	public String getInput() {
		return input;
	}

	public Condition getCondition() {
		return condition;
	}

	public Integer getChainId() {
		return chainId;
	}

	public String getNonce() {
		return nonce;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public String getR() {
		return r;
	}

	public String getRaw() {
		return raw;
	}

	public String getS() {
		return s;
	}

	public String getStandardV() {
		return standardV;
	}

	public String getTo() {
		return to;
	}

	public Object getTransactionIndex() {
		return transactionIndex;
	}

	public String getV() {
		return v;
	}

	public String getValue() {
		return value;
	}
	@JsonDeserialize()
	public static class Condition {
		public Integer block;

		public Condition() {
		}

		public Condition(Integer block) {
			this.block = block;
		}

		public Integer getBlock() {
			return block;
		}

		public void setBlock(Integer block) {
			this.block = block;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
