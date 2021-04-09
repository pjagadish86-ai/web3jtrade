package com.aitrades.blockchain.web3jtrade.integration.snipe.mq;

import javax.annotation.Resource;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;
import com.aitrades.blockchain.web3jtrade.repository.SnipeOrderHistoryRepository;
import com.aitrades.blockchain.web3jtrade.repository.SnipeOrderRepository;

@Service
public class SnipeOrderReQueue {
	
	@Resource(name="snipeOrderRabbitTemplate")
	private AmqpTemplate snipeOrderRabbitTemplate;
	
	@Autowired
	private SnipeOrderRepository snipeOrderRepository;
	
	@Autowired
	private SnipeOrderHistoryRepository snipeOrderHistoryRepository;
	
	public void send(SnipeTransactionRequest snipeTransactionRequest) throws Exception {
		try {
			Thread.sleep(200l);
			requeueMessage(snipeTransactionRequest);
			// For now we ignore it.
			
//			if (StringUtils.isBlank(snipeTransactionRequest.getCreatedDateTime())) {
//			snipeTransactionRequest.setCreatedDateTime(LocalDateTime.now().toString());
//		}
//			int response = LocalDateTime.now().getMinute() - LocalDateTime.parse(snipeTransactionRequest.getCreatedDateTime()).getMinute();
//			if(response <= 30) {
//				requeueMessage(snipeTransactionRequest);
//			}else {
//				snipeTransactionRequest.setSnipeExpired("retired more than 30 mins snipeorder expired");
//				snipeOrderHistoryRepository.save(snipeTransactionRequest);
//				snipeOrderRepository.delete(snipeTransactionRequest);
//			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}
	
	private void requeueMessage(SnipeTransactionRequest snipeTransactionRequest) {
		snipeOrderRabbitTemplate.convertAndSend(snipeTransactionRequest);
	}
}