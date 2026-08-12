package com.example.Ecommerce_Muebleria.Front.controller;

import com.example.Ecommerce_Muebleria.BackCartOrder.repositories.OrderRepository;
import com.example.Ecommerce_Muebleria.Notificaciones.services.NotificacionAppService;
import com.example.Ecommerce_Muebleria.entities.cart.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor // Esto inyecta automáticamente todos los "private final"
public class AdminOrderController {

    private final OrderRepository orderRepository;

    // Lo pasamos a private final y sacamos el @Autowired
    private final NotificacionAppService notificacionAppService;

    @GetMapping
    public String listAllOrders(Model model) {
        List<Order> allOrders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "dateCreated"));
        model.addAttribute("orders", allOrders);
        return "admin/admin-orders";
    }

    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String newStatus,
            RedirectAttributes redirectAttributes) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        order.setStatus(newStatus);
        orderRepository.save(order);

        String titulo = "";
        String mensaje = "";

        switch (newStatus.toUpperCase()) {
            case "APPROVED":
                titulo = "¡Pago Confirmado!";
                mensaje = "El pago de tu orden ORD-" + id + " fue aprobado. Ya estamos preparando tus muebles.";
                break;
            case "EN_CAMINO":
                titulo = "¡Tu pedido está en camino!";
                mensaje = "La orden ORD-" + id + " ya salió de la mueblería y va hacia tu domicilio. ¡Avisá en casa!";
                break;
            case "ENTREGADO":
                titulo = "Pedido Entregado";
                mensaje = "Tu orden ORD-" + id + " figura como entregada. ¡Esperamos que disfrutes mucho tu compra!";
                break;
            case "CANCELLED":
                titulo = "Pedido Cancelado";
                mensaje = "La orden ORD-" + id + " ha sido cancelada. Si fue un error, comunicate con nosotros.";
                break;
            case "PENDING_TRANSFER":
                titulo = "Esperando Transferencia";
                mensaje = "Recordá enviar el comprobante de transferencia para tu orden ORD-" + id + " así podemos armarla.";
                break;
        }

        if (!titulo.isEmpty()) {
            String emailCliente = order.getUserId();
            // Verificá que esta ruta coincida con el @GetMapping que armaste
            String urlDestino = "/api/notifications/mis-notificaciones";

            notificacionAppService.crearNotificacion(emailCliente, titulo, mensaje, urlDestino);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Estado actualizado y notificación enviada a la campanita del cliente.");

        return "redirect:/admin/orders";
    }
}