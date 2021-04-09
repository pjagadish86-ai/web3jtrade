package com.aitrades.blockchain.web3jtrade.domain.price;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)	
@JsonInclude(Include.NON_NULL)
public class Cryptonator{
	
    private Ticker ticker;
    private boolean success;
    private String error;
	
    public Cryptonator() {
	}
    
	public Ticker getTicker() {
		return ticker;
	}
	public void setTicker(Ticker ticker) {
		this.ticker = ticker;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)	
	@JsonInclude(Include.NON_NULL)
	public static class Ticker{
		private String base;
		private String target;
		private String price;
		
		public Ticker() {
		}

		public String getBase() {
			return base;
		}
		public void setBase(String base) {
			this.base = base;
		}
		public String getTarget() {
			return target;
		}
		public void setTarget(String target) {
			this.target = target;
		}
		public String getPrice() {
			return price;
		}
		public void setPrice(String price) {
			this.price = price;
		}
	}
    
}

