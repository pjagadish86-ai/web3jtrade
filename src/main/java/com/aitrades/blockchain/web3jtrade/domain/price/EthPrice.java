package com.aitrades.blockchain.web3jtrade.domain.price;

import java.math.BigDecimal;

public class EthPrice {

	public Data data;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public class Data {

		public Bundle bundle;

		public Bundle getBundle() {
			return bundle;
		}

		public void setBundle(Bundle bundle) {
			this.bundle = bundle;
		}

		public class Bundle {

			public String ethPrice;

			public String getEthPrice() {
				return ethPrice;
			}

			public BigDecimal getEthPriceAsBigDecimal() {
				return new BigDecimal(ethPrice);
			}

			public void setEthPrice(String ethPrice) {
				this.ethPrice = ethPrice;
			}

		}
	}

}