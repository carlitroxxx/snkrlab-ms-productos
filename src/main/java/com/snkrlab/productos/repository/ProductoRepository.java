package com.snkrlab.productos.repository;

import com.snkrlab.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    java.util.List<Producto> findByActivoTrue();
    @org.springframework.data.jpa.repository.Modifying(flushAutomatically = true, clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("update Producto p set p.stock = p.stock - :cantidad where p.id = :id and p.stock >= :cantidad")
    int descontarDisponible(@org.springframework.data.repository.query.Param("id") Long id,
            @org.springframework.data.repository.query.Param("cantidad") Integer cantidad);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Producto p where p.id = :id")
    java.util.Optional<Producto> bloquear(@org.springframework.data.repository.query.Param("id") Long id);

}
