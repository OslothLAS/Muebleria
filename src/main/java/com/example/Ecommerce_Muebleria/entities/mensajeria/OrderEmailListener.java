package com.example.Ecommerce_Muebleria.entities.mensajeria;

import com.example.Ecommerce_Muebleria.BackCartOrder.repositories.OrderRepository;
import com.example.Ecommerce_Muebleria.entities.cart.Order;
import com.example.Ecommerce_Muebleria.entities.cart.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEmailListener {

    private final JavaMailSender mailSender;
    private final OrderRepository orderRepository;

    @Async
    @RabbitListener(queues = "email_queue")
    @Transactional
    public void handleOrderEmailEvent(OrderEmailMessage message) {
        System.out.println("LLEGÓ EL EVENTO AL LISTENER PARA LA ORDEN: " + message.getOrderId());
        try {
            Order order = orderRepository.findById(message.getOrderId()).orElse(null);
            if (order == null) return;

            // 🚀 Identificamos si es una compra por transferencia
            boolean isTransfer = "PENDING_TRANSFER".equalsIgnoreCase(order.getStatus());

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(message.getUserEmail());
            helper.setBcc("silveroosmar911@gmail.com");

            // Cambiamos el Asunto según el método de pago
            if (isTransfer) {
                helper.setSubject("⏳ Instrucciones de pago - Orden #" + order.getId() + " - El Edén Muebles");
            } else {
                helper.setSubject("🧾 ¡Compra confirmada! Orden #" + order.getId() + " - El Edén Muebles");
            }

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<div style='font-family: Arial, sans-serif; color: #333; max-width: 650px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 8px;'>");

            // Cambiamos el título y la bajada
            if (isTransfer) {
                htmlContent.append("<h2 style='color: #1e2d42; text-align: center;'>¡Tu pedido está reservado!</h2>");
                htmlContent.append("<p>Hola, hemos registrado tu pedido exitosamente. Para que empecemos a prepararlo, realizá la transferencia y <strong>respondé este correo adjuntando el comprobante</strong>. Aquí tenés el detalle de tu orden <strong>#").append(order.getId()).append("</strong>:</p>");
            } else {
                htmlContent.append("<h2 style='color: #1e2d42; text-align: center;'>¡Gracias por tu compra en El Edén!</h2>");
                htmlContent.append("<p>Hola, tu pago ha sido procesado exitosamente. Aquí tenés el detalle de tu orden <strong>#").append(order.getId()).append("</strong>:</p>");
            }

            htmlContent.append("<table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>");
            htmlContent.append("<tr style='background-color: #f8f9fa;'><th style='padding: 10px; border: 1px solid #ddd; text-align: left;'>Producto</th><th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>Cant.</th><th style='padding: 10px; border: 1px solid #ddd; text-align: right;'>Subtotal</th></tr>");

            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    double subtotal = item.getPrice().doubleValue() * item.getQuantity();
                    String nombreMueble = (item.getProductName() != null && !item.getProductName().isEmpty())
                            ? item.getProductName()
                            : "Mueble ID: " + item.getProductId();

                    htmlContent.append("<tr>");
                    htmlContent.append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(nombreMueble).append("</td>");
                    htmlContent.append("<td style='padding: 10px; border: 1px solid #ddd; text-align: center;'>").append(item.getQuantity()).append("</td>");
                    htmlContent.append("<td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>$").append(subtotal).append("</td>");
                    htmlContent.append("</tr>");
                }
            }
            htmlContent.append("</table>");

            // Cambiamos la etiqueta del total y agregamos la caja con datos bancarios si es transferencia
            if (isTransfer) {
                htmlContent.append("<h3 style='text-align: right; margin-top: 20px; color: #333;'>Total a Transferir: $").append(order.getTotalAmount()).append("</h3>");

                htmlContent.append("<div style='background-color: #fff3cd; border: 1px solid #ffeeba; padding: 15px; margin-top: 20px; border-radius: 6px; color: #856404;'>");
                htmlContent.append("<h4 style='margin-top: 0; border-bottom: 1px solid #ffeeba; padding-bottom: 5px;'>Datos para la Transferencia</h4>");
                htmlContent.append("<p style='margin: 5px 0;'><strong>Banco:</strong> Banco Provincia</p>");
                htmlContent.append("<p style='margin: 5px 0;'><strong>Titular:</strong> Mueblería El Edén S.R.L.</p>");
                htmlContent.append("<p style='margin: 5px 0;'><strong>CBU:</strong> 0140000000000000000000</p>");
                htmlContent.append("<p style='margin: 5px 0;'><strong>Alias:</strong> EL.EDEN.MUEBLES</p>");
                htmlContent.append("</div>");
            } else {
                htmlContent.append("<h3 style='text-align: right; margin-top: 20px; color: #333;'>Total Pagado: $").append(order.getTotalAmount()).append("</h3>");
            }

            // Datos de Envío y Facturación
            htmlContent.append("<table style='width: 100%; margin-top: 30px; border-collapse: separate; border-spacing: 15px 0;'>");
            htmlContent.append("<tr>");

            // Envío
            htmlContent.append("<td style='width: 50%; vertical-align: top; background-color: #f8f9fa; padding: 15px; border-radius: 6px; border: 1px solid #ddd;'>");
            htmlContent.append("<h4 style='color: #1e2d42; margin-top: 0; margin-bottom: 15px; border-bottom: 1px solid #ccc; padding-bottom: 5px;'>📍 Datos de Envío</h4>");
            htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>Dirección:</strong> ").append(order.getShippingAddress() != null ? order.getShippingAddress() : "N/A").append("</p>");
            htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>C.P.:</strong> ").append(order.getZipCode() != null ? order.getZipCode() : "N/A").append("</p>");
            htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>Ciudad:</strong> ").append(order.getCity() != null ? order.getCity() : "N/A").append("</p>");
            if (order.getReferencesInfo() != null && !order.getReferencesInfo().isEmpty()) {
                htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>Ref:</strong> ").append(order.getReferencesInfo()).append("</p>");
            }
            htmlContent.append("</td>");

            // Facturación
            htmlContent.append("<td style='width: 50%; vertical-align: top; background-color: #f8f9fa; padding: 15px; border-radius: 6px; border: 1px solid #ddd;'>");
            htmlContent.append("<h4 style='color: #1e2d42; margin-top: 0; margin-bottom: 15px; border-bottom: 1px solid #ccc; padding-bottom: 5px;'>🧾 Datos de Facturación</h4>");
            htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>Dirección:</strong> ").append(order.getBillingAddress() != null ? order.getBillingAddress() : "N/A").append("</p>");
            htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>C.P.:</strong> ").append(order.getBillingZipCode() != null ? order.getBillingZipCode() : "N/A").append("</p>");
            htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>Ciudad:</strong> ").append(order.getBillingCity() != null ? order.getBillingCity() : "N/A").append("</p>");
            if (order.getBillingReferencesInfo() != null && !order.getBillingReferencesInfo().isEmpty()) {
                htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>Ref:</strong> ").append(order.getBillingReferencesInfo()).append("</p>");
            }
            htmlContent.append("</td>");

            htmlContent.append("</tr>");
            htmlContent.append("</table>");

            htmlContent.append("<p style='margin-top: 30px; font-size: 12px; color: #777; text-align: center;'>Si tenés alguna duda sobre tu envío o facturación, respondé a este mensaje.</p>");
            htmlContent.append("</div>");

            helper.setText(htmlContent.toString(), true);

            mailSender.send(mimeMessage);
            System.out.println("📧 Correo enviado exitosamente a: " + message.getUserEmail() + " | ¿Es transferencia?: " + isTransfer);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar el correo en segundo plano: " + e.getMessage());
        }
    }
}