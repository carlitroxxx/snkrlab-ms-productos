-- Revisar primero si ddl-auto=update ya creo la tabla y sus restricciones.
-- Ejecutar solo si la tabla no existe.
CREATE TABLE operaciones_stock (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    operacion_id VARCHAR(128) NOT NULL,
    orden_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    resultado VARCHAR(16) NOT NULL,
    motivo VARCHAR(40) NULL,
    CONSTRAINT uk_stock_operacion UNIQUE (operacion_id),
    CONSTRAINT uk_stock_orden_producto UNIQUE (orden_id, producto_id)
) ENGINE=InnoDB;
