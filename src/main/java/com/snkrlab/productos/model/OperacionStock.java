package com.snkrlab.productos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "operaciones_stock", uniqueConstraints = {
    @UniqueConstraint(name = "uk_stock_operacion", columnNames = "operacion_id"),
    @UniqueConstraint(name = "uk_stock_orden_producto", columnNames = {"orden_id", "producto_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class OperacionStock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "operacion_id", nullable = false, length = 128)
    private String operacionId;
    @Column(name = "orden_id", nullable = false)
    private Long ordenId;
    @Column(name = "producto_id", nullable = false)
    private Long productoId;
    @Column(nullable = false)
    private Integer cantidad;
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(nullable = false, length = 16)
    private Resultado resultado;
    @Column(length = 40)
    private String motivo;

    public enum Resultado { APLICADA, RECHAZADA }
}
