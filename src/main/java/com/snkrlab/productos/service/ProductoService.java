package com.snkrlab.productos.service;

import com.snkrlab.productos.model.Producto;
import com.snkrlab.productos.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository){
        this.productoRepository = productoRepository;
    }

    public List<Producto> obtenerTodos(){
        return productoRepository.findAll();
    }
    public Optional<Producto> obtenerPorId(Long id){
        return productoRepository.findById(id);
    }
    public Producto guardar(Producto producto){
        return productoRepository.save(producto);
    }
    public Optional<Producto> actualizar(Long id, Producto producto) {
        return productoRepository.findById(id)
                .map(productoExistente -> {
                    productoExistente.setNombre(producto.getNombre());
                    productoExistente.setMarca(producto.getMarca());
                    productoExistente.setModelo(producto.getModelo());
                    productoExistente.setPrecio(producto.getPrecio());
                    productoExistente.setStock(producto.getStock());
                    productoExistente.setDescripcion(producto.getDescripcion());
                    productoExistente.setImagen(producto.getImagen());
                    return productoRepository.save(productoExistente);
                });
    }
    public void eliminar(Long id){
        productoRepository.deleteById(id);
    }

    @Transactional
    public Producto reducirStock(Long id, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad a descontar debe ser mayor a 0.");
        }

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado: " + id));

        if (producto.getStock() < cantidad) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuficiente para " + producto.getNombre()
                            + ". Disponible: " + producto.getStock() + ", solicitado: " + cantidad);
        }

        producto.setStock(producto.getStock() - cantidad);
        return productoRepository.save(producto);
    }
}