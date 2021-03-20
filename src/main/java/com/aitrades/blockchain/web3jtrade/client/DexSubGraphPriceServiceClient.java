package com.aitrades.blockchain.web3jtrade.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import com.aitrades.blockchain.web3jtrade.domain.price.EthPrice;
import com.aitrades.blockchain.web3jtrade.domain.price.PairPrice;
import com.google.common.collect.ImmutableMap;
import com.jsoniter.JsonIterator;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
@Service
public class DexSubGraphPriceServiceClient implements DexSubGraphPriceClient {

	@Resource(name="uniswapPriceHttpClient")
	public CloseableHttpClient closeableHttpClient;
	
	private static final String SUSHI = "SUSHI";
	private static final String UNISWAP = "UNISWAP";
	
	private static final String UNISWAP_SUBGRAPH_URL = "https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v2";
	private static final String SUSHISWAP_SUBGRAPH_URL = "https://api.thegraph.com/subgraphs/name/sushiswap/exchange";

	private static Map<String, String> DEX_PRICE_URL = ImmutableMap.of(UNISWAP, UNISWAP_SUBGRAPH_URL, SUSHI, SUSHISWAP_SUBGRAPH_URL);

	
	private static final String QUERY_PAIR_DATA_0= "{	\"query\": \"{ pair(id: ";
	private static final String QUERY_PAIR_DATA_1 = "\\"+ "\"";
	private static final String QUERY_PAIR_DATA_3= "\\"+ "\"){     token0 {       id       symbol       name       derivedETH     }     token1 {       id       symbol       name      "
			+ " derivedETH     }     reserve0     reserve1     reserveUSD     trackedReserveETH     token0Price "
			+ "    token1Price     volumeUSD     txCount }}" +"\",";
	private static final String QUERY_PAIR_DATA_4 = "	\"variables\": null}";
	
	private static final String QUERY_ETH_PRICE ="{\"query\":\"{\\n bundle(id: \\\"1\\\" ) {\\n   ethPrice\\n }\\n}\",\"variables\":null}";
	
	
	public BigDecimal getPriceOfTicker(String pairAddress) {
		return Flowable.zip(getPairData(pairAddress), getEthPrice(), this :: calculateTickerPrice)
					   .subscribeOn(Schedulers.io())
					   .blockingSingle();
	}
	
	//token price = ETH RESR * ETH PRICE / UPI  RESR
	public BigDecimal calculateTickerPrice(PairPrice pairPriceUniswap, EthPrice ethPriceUniswap) {
		return pairPriceUniswap.getData().getPair().getReserve1AsBigDecimal()
				.multiply(ethPriceUniswap.getData().getBundle().getEthPriceAsBigDecimal())
				.divide(pairPriceUniswap.getData().getPair().getReserve0AsBigDecimal());
	}
	
	public Flowable<PairPrice> getPairData(final String pairAddress) {
	    return Flowable.fromCallable(()-> getPairDataFromUniswap(pairAddress));
	}
	
	public Flowable<EthPrice> getEthPrice() {
	    return Flowable.fromCallable(()-> getEthPriceFrmSubGraphbox());
	}
	
	private EthPrice getEthPriceFrmSubGraphbox() throws IOException {
		HttpPost post = new HttpPost(UNISWAP_SUBGRAPH_URL);
        post.setEntity(new StringEntity(QUERY_ETH_PRICE));
        try {
            return JsonIterator.deserialize(EntityUtils.toString(closeableHttpClient.execute(post).getEntity()),EthPrice.class);
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
            return JsonIterator.deserialize(EntityUtils.toString(closeableHttpClient.execute(post).getEntity()), PairPrice.class);
        }catch (Exception e) {
        	e.printStackTrace();
		}
        return null;
    }
	
	@Override
	public String getResourceUrl(String route) {
		return DEX_PRICE_URL.get(route);
	}

}
