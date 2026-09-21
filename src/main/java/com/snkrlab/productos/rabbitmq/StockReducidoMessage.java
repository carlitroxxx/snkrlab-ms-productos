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
}