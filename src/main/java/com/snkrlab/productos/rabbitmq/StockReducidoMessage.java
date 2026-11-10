package com.snkrlab.productos.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReducidoMessage {
    private Long productoId;
    private Integer cantidad;
    private String operacionId;
    private Long ordenId;

    public StockReducidoMessage(Long productoId, Integer cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }
}
