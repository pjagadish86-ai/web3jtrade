package com.aitrades.blockchain.web3jtrade.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;

import com.aitrades.blockchain.web3jtrade.domain.price.EthPrice;
import com.aitrades.blockchain.web3jtrade.domain.price.PairPrice;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.ImmutableMap;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
@Service
public class DexSubGraphPriceServiceClient  {

	@Resource(name="graphHqlPriceHttpClient")
	public CloseableHttpClient closeableHttpClient;
	
	@Resource(name="pairPriceObjectReader")
	private ObjectReader pairPriceObjectReader;
	
	@Resource(name="ethPriceObjectReader")
	private ObjectReader ethPriceObjectReader;
	
	private static final String SUSHI = "SUSHI";
	private static final String UNISWAP = "UNISWAP";
	
	private static final String UNISWAP_SUBGRAPH_URL = "https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v2";
	private static final String SUSHISWAP_SUBGRAPH_URL = "https://api.thegraph.com/subgraphs/name/sushiswap/exchange";

	@SuppressWarnings("unused")
	private static final Map<String, String> DEX_PRICE_URL = ImmutableMap.of(UNISWAP, UNISWAP_SUBGRAPH_URL, SUSHI, SUSHISWAP_SUBGRAPH_URL);
	
	private static final String QUERY_PAIR_DATA_0= "{	\"query\": \"{ pair(id: ";
	private static final String QUERY_PAIR_DATA_1 = "\\"+ "\"";
	private static final String QUERY_PAIR_DATA_3= "\\"+ "\"){     token0 {       id       symbol       name       derivedETH     }     token1 {       id       symbol       name      "
			+ " derivedETH     }     reserve0     reserve1     reserveUSD     trackedReserveETH     token0Price "
			+ "    token1Price     volumeUSD     txCount }}" +"\",";
	private static final String QUERY_PAIR_DATA_4 = "	\"variables\": null}";
	
	private static final String QUERY_ETH_PRICE ="{\"query\":\"{ bundle(id: \\\"1\\\" ) {   ethPrice }}\",\"variables\":null}";
	
	public BigDecimal getPriceOfTicker(String pairAddress, Credentials credentials) throws Exception {
		if(StringUtils.isBlank(pairAddress)) {
			throw new Exception("Pair Address is empty");
		}
		PairPrice pairPrice = null;
		EthPrice ethPrice = null;
		try {
			pairPrice = getPairDataFromUniswap(pairAddress);
			if(pairPrice == null ||  pairPrice.getData() == null || pairPrice.getData().getPair() == null) {
				throw new Exception("Unable to find pricess");
			}
			ethPrice = getEthPriceFrmGraph();
			
			return calculateTickerPrice(pairPrice, ethPrice);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//token price = ETH RESR * ETH PRICE / UPI  RESR
	public BigDecimal calculateTickerPrice(PairPrice pairPrice, EthPrice ethPrice) {
		return pairPrice.getData().getPair().getReserve1AsBigDecimal()
				.multiply(ethPrice.getData().getBundle().getEthPriceAsBigDecimal()).setScale(8, RoundingMode.HALF_UP)
				.divide(pairPrice.getData().getPair().getReserve0AsBigDecimal(), 8, RoundingMode.HALF_UP);
	}
	
	public Flowable<PairPrice> getPairData(final String pairAddress) {
	    return Flowable.create(subscribe -> getPairDataFromUniswap(pairAddress), BackpressureStrategy.BUFFER);
	}
	
	public Flowable<EthPrice> getEthPrice() {
	    return Flowable.create(subscribe -> getEthPriceFrmGraph(), BackpressureStrategy.BUFFER);
	}
	
	private EthPrice getEthPriceFrmGraph() throws IOException {
		HttpPost post = new HttpPost(UNISWAP_SUBGRAPH_URL);
        post.setEntity(new StringEntity(QUERY_ETH_PRICE));
        try {
            return ethPriceObjectReader.readValue(closeableHttpClient.execute(post).getEntity().getContent());
        }catch (Exception e) {
        	e.printStackTrace();
		}
        return null;
	}
	//TODO: ugly code
	private PairPrice getPairDataFromUniswap(String pairAddress) throws IOException {
		StringBuffer builder = new StringBuffer();
		builder.append(QUERY_PAIR_DATA_0);
		builder.append(QUERY_PAIR_DATA_1);
		builder.append(pairAddress);
		builder.append(QUERY_PAIR_DATA_3);
		builder.append(QUERY_PAIR_DATA_4);
        HttpPost post = new HttpPost(UNISWAP_SUBGRAPH_URL);
        post.setEntity(new StringEntity(builder.toString()));
        try {
			return pairPriceObjectReader.readValue(closeableHttpClient.execute(post).getEntity().getContent());
        }catch (Exception e) {
        	e.printStackTrace();
		}
        return null;
    }

}
