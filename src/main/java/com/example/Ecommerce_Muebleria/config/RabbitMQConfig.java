package com.example.Ecommerce_Muebleria.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EMAIL_QUEUE = "email_queue";
    public static final String ORDER_EXCHANGE = "order_exchange";
    public static final String EMAIL_ROUTING_KEY = "order.email.send";

    // 1. Declaramos la Cola
    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true); // true = durable (sobrevive a reinicios)
    }

    // 2. Declaramos el Exchange
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    // 3. Unimos la Cola con el Exchange mediante la Routing Key
    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(emailQueue).to(orderExchange).with(EMAIL_ROUTING_KEY);
    }

    // 4. Conversor para enviar nuestros objetos Java como JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}