package com.aitrades.blockchain.web3jtrade;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class DefaultContentTypeInterceptor implements Interceptor {

	public Response intercept(Interceptor.Chain chain) throws IOException {
		Request originalRequest = chain.request();
		Request requestWithUserAgent = originalRequest.newBuilder()
													  .header("Content-Type", "application/json").build();
		return chain.proceed(requestWithUserAgent);
	}
}