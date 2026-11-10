package com.snkrlab.productos.rabbitmq;

import com.snkrlab.productos.service.ProductoService;
import com.snkrlab.productos.service.OperacionStockService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StockListener {
    private static final Logger log = LoggerFactory.getLogger(StockListener.class);
    private final OperacionStockService operaciones;

    private final ProductoService productoService;

    public StockListener(ProductoService productoService, OperacionStockService operaciones) {
        this.productoService = productoService;
        this.operaciones = operaciones;
    }

    @RabbitListener(queues = RabbitMQConfig.COLA_STOCK)
    public void recibirReduccionStock(StockReducidoMessage mensaje) {
        validar(mensaje);
        try {
            if (mensaje.getOperacionId() == null && mensaje.getOrdenId() == null) {
                productoService.reducirStock(mensaje.getProductoId(), mensaje.getCantidad());
                log.info("Descuento historico aplicado: producto={}", mensaje.getProductoId());
            } else {
                var resultado = operaciones.descontar(mensaje.getOperacionId(), mensaje.getOrdenId(),
                        mensaje.getProductoId(), mensaje.getCantidad());
                // Tambien reconoce rechazos y duplicados: no vuelve a solicitar el descuento.
                log.info("Operacion de stock resuelta: operacion={}, resultado={}, motivo={}",
                        resultado.getOperacionId(), resultado.getResultado(), resultado.getMotivo());
            }
        } catch (ResponseStatusException ex) {
            int estado = ex.getStatusCode().value();
            if (estado == 400 || estado == 404 || estado == 409) {
                log.warn("Mensaje de stock rechazado definitivamente: producto={}, estado={}",
                        mensaje.getProductoId(), estado);
                throw new AmqpRejectAndDontRequeueException("Mensaje de stock no recuperable.", ex);
            }
            throw ex;
        }
        // Los fallos de infraestructura se propagan para reentregar el mensaje original.
    }

    private void validar(StockReducidoMessage mensaje) {
        if (mensaje == null || mensaje.getProductoId() == null || mensaje.getProductoId() <= 0
                || mensaje.getCantidad() == null || mensaje.getCantidad() <= 0)
            throw invalido();
        if (mensaje.getOperacionId() == null && mensaje.getOrdenId() == null) return;
        if (mensaje.getOrdenId() == null || mensaje.getOrdenId() <= 0
                || !("stock:orden:" + mensaje.getOrdenId() + ":producto:" + mensaje.getProductoId())
                    .equals(mensaje.getOperacionId()))
            throw invalido();
    }

    private AmqpRejectAndDontRequeueException invalido() {
        log.warn("Mensaje de stock invalido: identidad incoherente o datos invalidos.");
        return new AmqpRejectAndDontRequeueException("Mensaje de stock invalido.");
    }
}
