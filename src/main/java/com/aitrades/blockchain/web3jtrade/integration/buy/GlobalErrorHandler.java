package com.aitrades.blockchain.web3jtrade.integration.buy;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import com.aitrades.blockchain.web3jtrade.domain.Order;
import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;
import com.aitrades.blockchain.web3jtrade.repository.OrderRepository;
import com.aitrades.blockchain.web3jtrade.repository.SnipeOrderRepository;

public class GlobalErrorHandler {

	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private SnipeOrderRepository snipeOrderRepository;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ServiceActivator(inputChannel = "errorFlow")
	public void errorFlow(Message<?> message) throws Exception {
		//System.out.println("@@@@@@@@@@@@@@@@@@@@@" + ((MessagingException) message.getPayload()).getFailedMessage().getPayload());
		try {
			
			Object object = ((MessagingException) message.getPayload()).getFailedMessage() != null
								&& ((MessagingException) message.getPayload()).getFailedMessage().getPayload() != null
								? ((MessagingException) message.getPayload()).getFailedMessage().getPayload()  
										: Collections.emptyList();
			if(object instanceof List && !((List)object).isEmpty()){
			    if(((List)object).get(0) instanceof Order){
			    	List<Order> orders  = (List<Order>)object;
			    	for(Order order : orders) {
						orderRepository.updateAvail(order);	
					}
			    }
			    
			    if(((List)object).get(0) instanceof SnipeTransactionRequest){
			    	List<SnipeTransactionRequest> snipeTransactionRequests  = (List<SnipeTransactionRequest>)object;
			    	for(SnipeTransactionRequest snipeTransactionRequest : snipeTransactionRequests) {
			    		snipeOrderRepository.updateAvail(snipeTransactionRequest);	
					}
			    
			    }
			}
			if(object instanceof List){
			   
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
