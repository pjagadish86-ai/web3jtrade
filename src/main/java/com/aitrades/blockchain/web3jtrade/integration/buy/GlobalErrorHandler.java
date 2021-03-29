package com.aitrades.blockchain.web3jtrade.integration.buy;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import com.aitrades.blockchain.web3jtrade.domain.Order;
import com.aitrades.blockchain.web3jtrade.repository.OrderRepository;

public class GlobalErrorHandler {

	@Autowired
	public OrderRepository orderRepository;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ServiceActivator(inputChannel = "errorFlow")
	public void errorFlow(Message<?> message) throws Exception {
		//System.out.println("@@@@@@@@@@@@@@@@@@@@@" + ((MessagingException) message.getPayload()).getFailedMessage().getPayload());
		try {
			
			Object object = ((MessagingException) message.getPayload()).getFailedMessage().getPayload();
			if(object instanceof List){
			    if(((List)object).size() > 0 && (((List)object).get(0) instanceof Order)){
			    	List<Order> orders  = (List<Order>)object;
			    	for(Order order : orders) {
						orderRepository.updateAvail(order);	
					}
			    
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
