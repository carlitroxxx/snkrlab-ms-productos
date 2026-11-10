package com.snkrlab.productos.repository;

import com.snkrlab.productos.model.OperacionStock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperacionStockRepository extends JpaRepository<OperacionStock, Long> {
    Optional<OperacionStock> findByOperacionId(String operacionId);
    Optional<OperacionStock> findByOrdenIdAndProductoId(Long ordenId, Long productoId);
}
