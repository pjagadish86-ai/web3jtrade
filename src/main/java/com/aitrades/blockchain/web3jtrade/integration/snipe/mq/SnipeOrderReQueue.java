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
		snipeOrderRabbitTemplate.convertAndSend(snipeTransactionRequest);
	}
	
}