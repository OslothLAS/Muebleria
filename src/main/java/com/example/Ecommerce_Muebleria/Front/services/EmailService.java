package com.example.Ecommerce_Muebleria.Front.services;

import com.example.Ecommerce_Muebleria.entities.cart.Order;
import com.example.Ecommerce_Muebleria.entities.cart.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async // 🚀 Se ejecuta en segundo plano para no congelar la web
    public void sendPurchaseConfirmationEmail(String toEmail, Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            // 🚀 También podés agregar tu BCC acá si querés recibir la copia en tu correo personal
            // helper.setBcc("silveroosmar911@gmail.com");

            helper.setSubject("🧾 ¡Compra confirmada! Orden #" + order.getId() + " - El Edén Muebles");

            // Construimos un cuerpo HTML limpio y profesional para el mail
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<div style='font-family: Arial, sans-serif; color: #333; max-width: 650px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 8px;'>");
            htmlContent.append("<h2 style='color: #e69500; text-align: center;'>¡Gracias por tu compra en El Edén!</h2>");
            htmlContent.append("<p>Hola, tu pago ha sido procesado exitosamente. Aquí tenés el detalle de tu orden <strong>#").append(order.getId()).append("</strong>:</p>");

            // --- TABLA DE PRODUCTOS ---
            htmlContent.append("<table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>");
            htmlContent.append("<tr style='background-color: #f8f9fa;'><th style='padding: 10px; border: 1px solid #ddd; text-align: left;'>Producto</th><th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>Cant.</th><th style='padding: 10px; border: 1px solid #ddd; text-align: right;'>Subtotal</th></tr>");

            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    String nombreItem = (item.getProductName() != null) ? item.getProductName() : "Producto ID: " + item.getProductId();
                    double subtotal = item.getPrice().doubleValue() * item.getQuantity();

                    htmlContent.append("<tr>");
                    htmlContent.append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(nombreItem).append("</td>");
                    htmlContent.append("<td style='padding: 10px; border: 1px solid #ddd; text-align: center;'>").append(item.getQuantity()).append("</td>");
                    htmlContent.append("<td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>$").append(subtotal).append("</td>");
                    htmlContent.append("</tr>");
                }
            }

            htmlContent.append("</table>");
            htmlContent.append("<h3 style='text-align: right; margin-top: 20px; color: #333;'>Total Pagado: $").append(order.getTotalAmount()).append("</h3>");

            // --- 🚀 NUEVA SECCIÓN: DATOS DE ENVÍO Y FACTURACIÓN ---
            // Usamos una tabla para garantizar que las dos columnas se vean bien en Gmail/Outlook
            htmlContent.append("<table style='width: 100%; margin-top: 30px; border-collapse: separate; border-spacing: 15px 0;'>");
            htmlContent.append("<tr>");

            // Columna 1: Envío
            htmlContent.append("<td style='width: 50%; vertical-align: top; background-color: #f8f9fa; padding: 15px; border-radius: 6px; border: 1px solid #ddd;'>");
            htmlContent.append("<h4 style='color: #1e2d42; margin-top: 0; margin-bottom: 15px; border-bottom: 1px solid #ccc; padding-bottom: 5px;'>📍 Datos de Envío</h4>");
            htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>Dirección:</strong> ").append(order.getShippingAddress()).append("</p>");
            htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>C.P.:</strong> ").append(order.getZipCode()).append("</p>");
            htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>Ciudad:</strong> ").append(order.getCity()).append("</p>");
            if (order.getReferencesInfo() != null && !order.getReferencesInfo().isEmpty()) {
                htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>Ref:</strong> ").append(order.getReferencesInfo()).append("</p>");
            }
            htmlContent.append("</td>");

            // Columna 2: Facturación
            htmlContent.append("<td style='width: 50%; vertical-align: top; background-color: #f8f9fa; padding: 15px; border-radius: 6px; border: 1px solid #ddd;'>");
            htmlContent.append("<h4 style='color: #1e2d42; margin-top: 0; margin-bottom: 15px; border-bottom: 1px solid #ccc; padding-bottom: 5px;'>🧾 Datos de Facturación</h4>");
            htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>Dirección:</strong> ").append(order.getBillingAddress()).append("</p>");
            htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>C.P.:</strong> ").append(order.getBillingZipCode()).append("</p>");
            htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>Ciudad:</strong> ").append(order.getBillingCity()).append("</p>");
            if (order.getBillingReferencesInfo() != null && !order.getBillingReferencesInfo().isEmpty()) {
                htmlContent.append("<p style='margin: 5px 0; font-size: 14px;'><strong>Ref:</strong> ").append(order.getBillingReferencesInfo()).append("</p>");
            }
            htmlContent.append("</td>");

            htmlContent.append("</tr>");
            htmlContent.append("</table>");
            // --- FIN DE LA SECCIÓN ---

            htmlContent.append("<p style='margin-top: 30px; font-size: 12px; color: #777; text-align: center;'>Si tenés alguna duda sobre tu envío o facturación, respondé a este mensaje.</p>");
            htmlContent.append("</div>");

            helper.setText(htmlContent.toString(), true); // El 'true' habilita HTML

            mailSender.send(message);
            System.out.println("📧 Correo de confirmación enviado exitosamente a: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar el correo de confirmación: " + e.getMessage());
        }
    }
}