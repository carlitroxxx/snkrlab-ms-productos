package com.snkrlab.productos;
import com.snkrlab.productos.model.Producto;
import com.snkrlab.productos.repository.ProductoRepository;
import com.snkrlab.productos.service.ProductoService;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ProductoActivoTest {
    @Test void desactivarYReactivarConservaDatos() {
        var repo=mock(ProductoRepository.class); var service=new ProductoService(repo);
        var p=Producto.builder().id(1L).nombre("Producto").stock(5).precio(1000).build();
        assertTrue(p.getActivo());
        when(repo.bloquear(1L)).thenReturn(Optional.of(p));
        service.eliminar(1L); service.eliminar(1L);
        assertFalse(p.getActivo()); assertEquals(5,p.getStock()); assertEquals(1000,p.getPrecio());
        service.cambiarActivo(1L,true); assertTrue(p.getActivo());
        verify(repo,never()).deleteById(any());
    }
    @Test void publicoOcultaInactivosYAdminConservaListado() {
        var repo=mock(ProductoRepository.class); var service=new ProductoService(repo);
        var p=Producto.builder().id(1L).activo(false).build();
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.findByActivoTrue()).thenReturn(List.of());
        when(repo.findAll()).thenReturn(List.of(p));
        assertTrue(service.obtenerPorId(1L).isEmpty());
        assertTrue(service.obtenerTodos().isEmpty());
        assertEquals(1,service.obtenerAdministracion().size());
    }
}
