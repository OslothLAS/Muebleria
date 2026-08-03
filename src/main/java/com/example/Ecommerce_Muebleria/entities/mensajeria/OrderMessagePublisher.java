package com.example.Ecommerce_Muebleria.entities.mensajeria;

import com.example.Ecommerce_Muebleria.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderEmailEvent(OrderEmailMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                message
        );
        System.out.println("Mensaje enviado a la cola de correos para la orden: " + message.getOrderId());
    }
}