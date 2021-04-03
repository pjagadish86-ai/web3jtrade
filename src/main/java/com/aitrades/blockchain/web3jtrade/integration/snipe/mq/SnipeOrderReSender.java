package com.aitrades.blockchain.web3jtrade.integration.snipe.mq;

import javax.annotation.Resource;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.aitrades.blockchain.web3jtrade.domain.SnipeTransactionRequest;

@Service
public class SnipeOrderReSender {
	
	@Resource(name="snipeOrderRabbitTemplate")
	private AmqpTemplate snipeOrderRabbitTemplate;
	
	@Async
	public void send(SnipeTransactionRequest snipeTransactionRequest) {
		try {
			System.out.println("Into sleep mode" + snipeTransactionRequest);
			Thread.sleep(10000l);
			snipeOrderRabbitTemplate.convertAndSend(snipeTransactionRequest);
			System.out.println("Snipe Message Sen" + snipeTransactionRequest);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}