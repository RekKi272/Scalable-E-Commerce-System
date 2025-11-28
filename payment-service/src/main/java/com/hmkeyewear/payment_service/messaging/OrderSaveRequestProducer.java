package com.hmkeyewear.payment_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderSaveRequestProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaveRequestProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.order.routing-key}")
    private String routingKey;

}
