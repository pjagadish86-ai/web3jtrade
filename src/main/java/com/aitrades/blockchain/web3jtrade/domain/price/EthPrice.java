package com.aitrades.blockchain.web3jtrade.domain.price;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EthPrice {

	public Data data;

	public EthPrice() {
	}

	public EthPrice(Data data) {
		this.data = data;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public static class Data {

		public Bundle bundle;
		
		public Data() {
		}

		public Data(Bundle bundle) {
			this.bundle = bundle;
		}

		public Bundle getBundle() {
			return bundle;
		}

		public void setBundle(Bundle bundle) {
			this.bundle = bundle;
		}

		public static class Bundle {

			public String ethPrice;

			public Bundle() {
				// TODO Auto-generated constructor stub
			}

			public Bundle(String ethPrice) {
				this.ethPrice = ethPrice;
			}

			public String getEthPrice() {
				return ethPrice;
			}

			public BigDecimal getEthPriceAsBigDecimal() {
				return new BigDecimal(ethPrice).setScale(8, RoundingMode.UP);
			}

			public void setEthPrice(String ethPrice) {
				this.ethPrice = ethPrice;
			}

		}
	}

}