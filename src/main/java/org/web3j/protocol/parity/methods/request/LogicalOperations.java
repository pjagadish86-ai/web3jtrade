package org.web3j.protocol.parity.methods.request;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class LogicalOperations {
	private String gt;
	private String lt;
	private String eq;
	
	public LogicalOperations() {
	}
	
	public LogicalOperations(BigInteger gt, BigInteger lt, BigInteger eq) {
		if(gt != null) {
			this.gt = Numeric.encodeQuantity(Convert.toWei(new BigDecimal(gt), Convert.Unit.GWEI).toBigInteger());
		}
		if(lt != null) {
			this.lt = Numeric.encodeQuantity(Convert.toWei(new BigDecimal(lt), Convert.Unit.GWEI).toBigInteger());
		}
		if(eq != null) {
			this.eq = Numeric.encodeQuantity(Convert.toWei(new BigDecimal(eq), Convert.Unit.GWEI).toBigInteger());
		}
	}
	public String getGt() {
		return gt;
	}
	public String getLt() {
		return lt;
	}
	public String getEq() {
		return eq;
	}
	
	
}
