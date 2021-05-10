package com.aitrades.blockchain.web3jtrade.service;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.web3j.protocol.Network;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

import com.aitrades.blockchain.web3jtrade.CustomWebSocketClient;
import com.aitrades.blockchain.web3jtrade.client.Web3jServiceClient;
import com.aitrades.blockchain.web3jtrade.domain.EndpointConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
public class Web3jServiceClientFactory {
	
	private static final String IPC = "IPC";
	private static final String HTTP = "HTTP";
	
	private static final String ENDPOINT_CONFIG_URLS = "ENDPOINT_CONFIG_URLS";
	
	private final Map<String, Web3jServiceClient> WEB3J_MAP = new HashMap<>();
	
	private static com.github.benmanes.caffeine.cache.Cache<String, List<EndpointConfig>> blockChainExchangesConnections;

	@Autowired
	private DexContractStaticCodeValuesService dexContractStaticCodeValuesService;
	
	@Autowired
    private Web3jServiceClientFactory() {
		blockChainExchangesConnections = Caffeine.newBuilder()
	                               .expireAfterWrite(2, TimeUnit.MINUTES)
	                               .build();
    }
	
	@Autowired
	private EndpointConfigServiceResolver endpointConfigServiceResolver;

	public Web3jServiceClient getWeb3jMap(String route) throws Exception {
		Web3jServiceClient web3jServiceClient = WEB3J_MAP.get(route);
		if(web3jServiceClient == null) {
			List<EndpointConfig> endpointConfigs = blockChainExchangesConnections.get(ENDPOINT_CONFIG_URLS, this :: fetchBlockChainExchanges);
			String blckExchge = dexContractStaticCodeValuesService.fetchBlockChainExchanges().stream()
					.filter(ex ->ex.getCode().toString().equalsIgnoreCase(route)).findFirst().get().getBlockchainName();
			for(EndpointConfig endpointConfig : endpointConfigs ) {
				if(endpointConfig.isEnabled() && StringUtils.equalsIgnoreCase(blckExchge, endpointConfig.getBlockchain())) {
					WEB3J_MAP.put(route, buildWeb3jServiceClient(endpointConfig));
					web3jServiceClient = WEB3J_MAP.get(route);
					break;
				}
			}
		}
		return web3jServiceClient;
	}
	
	public List<EndpointConfig> fetchBlockchainExchanges(String endpointConfigUrlKey) {
		return blockChainExchangesConnections.get(endpointConfigUrlKey, this :: fetchBlockChainExchanges);
	}
	
	private List<EndpointConfig> fetchBlockChainExchanges(String blockchainExgs){
		return endpointConfigServiceResolver.fetchEndpointConfigs();
	}
	
	private Web3jServiceClient buildWeb3jServiceClient(EndpointConfig endpointConfig) throws Exception {
		Web3j web3j = null;
		if(StringUtils.equalsIgnoreCase(endpointConfig.getRpcProtocol(), HTTP)) {
			web3j = buildWeb3jHttp(endpointConfig);
		}else if(StringUtils.equalsIgnoreCase(endpointConfig.getRpcProtocol(), IPC)) {
			web3j = buildWeb3jIPC(endpointConfig);
		}else {
			web3j = buildWeb3jWebSocket(endpointConfig);
		}
		return new Web3jServiceClient(web3j, new RestTemplate(), new ObjectMapper());
	}

	//FIXME
	private Web3j buildWeb3jIPC(EndpointConfig endpointConfig) throws Exception {
		Network network = null;// StringUtils.containsIgnoreCase(SystemUtils.OS_NAME, "WINDOWS") ? new WindowsIpcService(endpointConfig.getEndpointUrl()): new UnixDomainSocket(endpointConfig.getEndpointUrl());
		return Web3j.build(network );
	}

	private Web3j buildWeb3jHttp(EndpointConfig endpointConfig) {
		return Web3j.build(new HttpService(endpointConfig.getEndpointUrl()));
	}

	private Web3j buildWeb3jWebSocket(EndpointConfig endpointConfig) throws Exception {
		WebSocketService webSocketService =	null;
		try {
			if(webSocketService == null) {
				webSocketService =	buildWebSocketService(endpointConfig);
				return Web3j.build(webSocketService);
			}
		} catch (Exception e) {
			try {
				return Web3j.build(webSocketService);
			} catch (Exception e1) {
			}
		}
		return null;
	}

	private WebSocketService buildWebSocketService(EndpointConfig endpointConfig) throws Exception {
		WebSocketService webSocketService = null;
		try {
			webSocketService = new WebSocketService(new CustomWebSocketClient(parseURI(endpointConfig.getEndpointUrl())), false);
			webSocketService.connect();
		} catch (Exception e) {
			Thread.sleep(2000l);
			try {
				webSocketService.connect();
				System.out.println("reconnecting to -> "+ endpointConfig.getBlockchain());
			} catch (ConnectException e1) {
				System.err.println(endpointConfig.getBlockchain() +" node WSS failed, restart app");
			}
		}
		return webSocketService;
	}

	private static URI parseURI(String serverUrl) {
        try {
            return new URI(serverUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("Failed to parse URL: '%s'", serverUrl), e);
        }
    }
}
