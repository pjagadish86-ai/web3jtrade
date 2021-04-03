package com.aitrades.blockchain.web3jtrade.integration.snipe.mq;

import java.time.Duration;
import java.time.LocalDateTime;

import javax.annotation.Resource;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;
import com.aitrades.blockchain.web3jtrade.repository.SnipeOrderHistoryRepository;
import com.aitrades.blockchain.web3jtrade.repository.SnipeOrderRepository;

@Service
public class SnipeOrderReSender {
	
	@Resource(name="snipeOrderRabbitTemplate")
	private AmqpTemplate snipeOrderRabbitTemplate;
	
	@Autowired
	private SnipeOrderRepository snipeOrderRepository;
	
	@Autowired
	private SnipeOrderHistoryRepository snipeOrderHistoryRepository;
	
	public void send(SnipeTransactionRequest snipeTransactionRequest) throws Exception {
		try {
			System.out.println("Into sleep mode-" + snipeTransactionRequest);
			Thread.sleep(25000l);
			int response = LocalDateTime.now().getMinute() - snipeTransactionRequest.getCreatedLocalDateTime().getMinute();
			if(response <= 30) {
				requeueMessage(snipeTransactionRequest);
			}else {
				snipeOrderHistoryRepository.save(snipeTransactionRequest);
				snipeOrderRepository.delete(snipeTransactionRequest);
			}
			System.out.println("Snipe Message Sent" + snipeTransactionRequest);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}
	
	private void requeueMessage(SnipeTransactionRequest snipeTransactionRequest) {
		snipeOrderRabbitTemplate.convertAndSend(snipeTransactionRequest);
	}
}