package com.example.Ecommerce_Muebleria.entities.mensajeria;
import com.example.Ecommerce_Muebleria.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEmailListener {

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void consumeOrderEmailEvent(OrderEmailMessage message) {
        System.out.println("Recibido evento para enviar mail...");
        System.out.println("Preparando comprobante para: " + message.getUserEmail());
        System.out.println("Notificando al administrador sobre ARBA para orden #" + message.getOrderId());

        try {
            // Aquí iría tu lógica real usando JavaMailSender
            // emailService.sendOrderConfirmation(message);
            Thread.sleep(2000); // Simulando el tiempo que tarda en enviar el mail
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Emails enviados con éxito para la orden #" + message.getOrderId());
    }
}