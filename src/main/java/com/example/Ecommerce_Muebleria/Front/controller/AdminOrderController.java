package com.example.Ecommerce_Muebleria.Front.controller;

import com.example.Ecommerce_Muebleria.BackCartOrder.repositories.OrderRepository;
import com.example.Ecommerce_Muebleria.entities.cart.Order;
import com.example.Ecommerce_Muebleria.entities.mensajeria.OrderEmailMessage;
import com.example.Ecommerce_Muebleria.entities.mensajeria.OrderMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderRepository orderRepository;
    // Inyectamos tu publicador de RabbitMQ para mandar el recibo cuando apruebes la transferencia
    private final OrderMessagePublisher orderMessagePublisher;

    // 1. Mostrar todas las órdenes en la tabla
    @GetMapping
    public String listAllOrders(Model model) {
        // Traemos todas las órdenes, de la más reciente a la más antigua
        List<Order> allOrders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "dateCreated"));
        model.addAttribute("orders", allOrders);
        return "admin/admin-orders"; // Apunta al HTML que vamos a crear
    }

    // 2. Actualizar el estado de una orden
    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        // Guardamos el estado anterior para saber si hubo un cambio a APPROVED
        String previousStatus = order.getStatus();

        order.setStatus(newStatus);
        orderRepository.save(order);

        // 🚀 MAGIA: Si pasaste una transferencia de PENDIENTE a APROBADO, disparamos el mail de recibo
        if (!"APPROVED".equalsIgnoreCase(previousStatus) && "APPROVED".equalsIgnoreCase(newStatus)) {
            // (Asegurate de tener el email del usuario guardado en la orden o buscalo.
            // Si no lo tenés en la entidad Order, podés buscar al User en la BD usando order.getUserId())
            OrderEmailMessage emailMessage = new OrderEmailMessage(
                    order.getId(),
                    order.getUserId(),
                    "cliente@ejemplo.com", // Reemplazar por el email real del cliente asociado a esta orden
                    "Cliente",
                    order.getTotalAmount()
            );
            orderMessagePublisher.publishOrderEmailEvent(emailMessage);
        }

        return "redirect:/admin/orders";
    }
}