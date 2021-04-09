package com.aitrades.blockchain.web3jtrade.domain.price;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PairPrice {

	public Data data;
	
	public PairPrice() {
	}

	public PairPrice(Data data) {
		this.data = data;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public static class Data {
		public Pair pair;
		
		public Data() {
		}

		public Data(Pair pair) {
			this.pair = pair;
		}

		public Pair getPair() {
			return pair;
		}

		public void setPair(Pair pair) {
			this.pair = pair;
		}

	}

	public static class Pair {
		public String reserve0;
		public String reserve1;
		public String reserveUSD;
		public Token0 token0;
		public String token0Price;
		public Token1 token1;
		public String token1Price;
		public String trackedReserveETH;
		public String txCount;
		public String volumeUSD;

		public Pair() {
		}

		public Pair(String reserve0, String reserve1, String reserveUSD, Token0 token0, String token0Price,
				Token1 token1, String token1Price, String trackedReserveETH, String txCount, String volumeUSD) {
			this.reserve0 = reserve0;
			this.reserve1 = reserve1;
			this.reserveUSD = reserveUSD;
			this.token0 = token0;
			this.token0Price = token0Price;
			this.token1 = token1;
			this.token1Price = token1Price;
			this.trackedReserveETH = trackedReserveETH;
			this.txCount = txCount;
			this.volumeUSD = volumeUSD;
		}

		public String getReserve0() {
			return reserve0;
		}

		public BigDecimal getReserve0AsBigDecimal() {
			return new BigDecimal(reserve0).setScale(8, RoundingMode.UP);
		}

		public void setReserve0(String reserve0) {
			this.reserve0 = reserve0;
		}

		public String getReserve1() {
			return reserve1;
		}

		public BigDecimal getReserve1AsBigDecimal() {
			return new BigDecimal(reserve1).setScale(8, RoundingMode.UP);
		}

		public void setReserve1(String reserve1) {
			this.reserve1 = reserve1;
		}

		public String getReserveUSD() {
			return reserveUSD;
		}

		public void setReserveUSD(String reserveUSD) {
			this.reserveUSD = reserveUSD;
		}

		public Token0 getToken0() {
			return token0;
		}

		public void setToken0(Token0 token0) {
			this.token0 = token0;
		}

		public String getToken0Price() {
			return token0Price;
		}

		public void setToken0Price(String token0Price) {
			this.token0Price = token0Price;
		}

		public Token1 getToken1() {
			return token1;
		}

		public void setToken1(Token1 token1) {
			this.token1 = token1;
		}

		public String getToken1Price() {
			return token1Price;
		}

		public void setToken1Price(String token1Price) {
			this.token1Price = token1Price;
		}

		public String getTrackedReserveETH() {
			return trackedReserveETH;
		}

		public void setTrackedReserveETH(String trackedReserveETH) {
			this.trackedReserveETH = trackedReserveETH;
		}

		public String getTxCount() {
			return txCount;
		}

		public void setTxCount(String txCount) {
			this.txCount = txCount;
		}

		public String getVolumeUSD() {
			return volumeUSD;
		}

		public void setVolumeUSD(String volumeUSD) {
			this.volumeUSD = volumeUSD;
		}

	}

	public static class Token0 {
		public String derivedETH;
		public String id;
		public String name;
		public String symbol;
		
		public Token0() {
			// TODO Auto-generated constructor stub
		}

		public Token0(String derivedETH, String id, String name, String symbol) {
			this.derivedETH = derivedETH;
			this.id = id;
			this.name = name;
			this.symbol = symbol;
		}

		public String getDerivedETH() {
			return derivedETH;
		}

		public void setDerivedETH(String derivedETH) {
			this.derivedETH = derivedETH;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}

	}

	public static class Token1 {

		public String derivedETH;
		public String id;
		public String name;
		public String symbol;
		
		public Token1() {
			// TODO Auto-generated constructor stub
		}

		public Token1(String derivedETH, String id, String name, String symbol) {
			this.derivedETH = derivedETH;
			this.id = id;
			this.name = name;
			this.symbol = symbol;
		}

		public String getDerivedETH() {
			return derivedETH;
		}

		public void setDerivedETH(String derivedETH) {
			this.derivedETH = derivedETH;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}

	}

}
