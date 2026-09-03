package com.snkrlab.productos.service;

import com.snkrlab.productos.model.Producto;
import com.snkrlab.productos.repository.ProductoRepository;
import org.springframework.stereotype.Service;

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
}
