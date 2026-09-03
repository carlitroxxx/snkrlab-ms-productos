package com.snkrlab.productos.config;

import com.snkrlab.productos.model.Producto;
import com.snkrlab.productos.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    public DataInitializer(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String ... args) {
        if (productoRepository.count() == 0) {
            Producto p1 = Producto.builder()
                    .nombre("Air Force 1 '07")
                    .marca("Nike")
                    .modelo("AF1-07")
                    .precio(109990)
                    .stock(15)
                    .descripcion("Zapatilla clasica de cuero blanco.")
                    .imagen("https://example.com/nike-af1.jpg")
                    .build();

            Producto p2 = Producto.builder()
                    .nombre("Forum Low")
                    .marca("Adidas")
                    .modelo("FL-2024")
                    .precio(89990)
                    .stock(10)
                    .descripcion("Estilo retro urbano de corte bajo.")
                    .imagen("https://example.com/adidas-forum.jpg")
                    .build();

            Producto p3 = Producto.builder()
                    .nombre("Air Jordan 1 Retro High")
                    .marca("Jordan")
                    .modelo("AJ1-RH")
                    .precio(159990)
                    .stock(5)
                    .descripcion("Edicion clasica de basquetbol.")
                    .imagen("https://example.com/jordan1.jpg")
                    .build();

            productoRepository.saveAll(List.of(p1, p2, p3));
            System.out.println(">>> Datos iniciales de zapatillas cargados correctamente.");
        }
    }
}