package com.snkrlab.productos.service;

import com.snkrlab.productos.model.OperacionStock;
import com.snkrlab.productos.repository.*;
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OperacionStockService {
    private final OperacionStockRepository operaciones;
    private final ProductoRepository productos;
    private final TransactionTemplate transaccion;

    public OperacionStockService(OperacionStockRepository operaciones, ProductoRepository productos,
            PlatformTransactionManager manager) {
        this.operaciones = operaciones;
        this.productos = productos;
        transaccion = new TransactionTemplate(manager);
        transaccion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaccion.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public OperacionStock descontar(String operacionId, Long ordenId, Long productoId, Integer cantidad) {
        if (operacionId == null || operacionId.isBlank() || operacionId.length() > 128
                || ordenId == null || ordenId <= 0 || productoId == null || productoId <= 0
                || cantidad == null || cantidad <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operacion de stock invalida.");
        try {
            return transaccion.execute(status -> {
                OperacionStock existente = buscar(operacionId, ordenId, productoId);
                if (existente != null) return validar(existente, operacionId, ordenId, productoId, cantidad);
                OperacionStock operacion = new OperacionStock();
                operacion.setOperacionId(operacionId);
                operacion.setOrdenId(ordenId);
                operacion.setProductoId(productoId);
                operacion.setCantidad(cantidad);
                // Valor provisional invisible fuera de la transaccion.
                operacion.setResultado(OperacionStock.Resultado.RECHAZADA);
                operaciones.saveAndFlush(operacion); // Reserva UNIQUE antes de tocar stock.
                int filas = productos.descontarDisponible(productoId, cantidad);
                if (filas == 1) {
                    operacion.setResultado(OperacionStock.Resultado.APLICADA);
                } else {
                    operacion.setMotivo(productos.existsById(productoId)
                            ? "STOCK_INSUFICIENTE" : "PRODUCTO_INEXISTENTE");
                }
                return operaciones.saveAndFlush(operacion);
            });
        } catch (DataIntegrityViolationException ex) {
            // La transaccion anterior ya se revirtio. Aqui solo se recupera el resultado.
            return transaccion.execute(status -> {
                OperacionStock existente = buscar(operacionId, ordenId, productoId);
                if (existente == null) throw ex;
                return validar(existente, operacionId, ordenId, productoId, cantidad);
            });
        }
    }

    private OperacionStock buscar(String operacionId, Long ordenId, Long productoId) {
        return operaciones.findByOperacionId(operacionId)
                .orElseGet(() -> operaciones.findByOrdenIdAndProductoId(ordenId, productoId).orElse(null));
    }

    private OperacionStock validar(OperacionStock op, String id, Long orden, Long producto, Integer cantidad) {
        if (!Objects.equals(op.getOperacionId(), id) || !Objects.equals(op.getOrdenId(), orden)
                || !Objects.equals(op.getProductoId(), producto) || !Objects.equals(op.getCantidad(), cantidad))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Identidad de stock asociada a otros datos.");
        return op;
    }
}
