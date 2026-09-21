package com.snkrlab.productos.rabbitmq;

import com.snkrlab.productos.service.ProductoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StockListener {

    private final ProductoService productoService;

    public StockListener(ProductoService productoService) {
        this.productoService = productoService;
    }

    @RabbitListener(queues = RabbitMQConfig.COLA_STOCK)
    public void recibirReduccionStock(StockReducidoMessage mensaje) {
        try {
            productoService.reducirStock(mensaje.getProductoId(), mensaje.getCantidad());
            System.out.println("[v] Stock reducido por evento: producto " + mensaje.getProductoId()
                    + ", cantidad " + mensaje.getCantidad());
        } catch (Exception e) {
            System.err.println("[x] Error al reducir stock desde evento: " + e.getMessage());
            throw e; // deja que Spring AMQP reintente el mensaje
        }
    }
}