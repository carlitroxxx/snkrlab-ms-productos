package com.snkrlab.productos.controller;

import com.snkrlab.productos.model.Producto;
import com.snkrlab.productos.service.ProductoService;
import com.snkrlab.productos.kafka.NavegacionPublisher;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("api/v1/productos")
public class ProductoController {
    private final ProductoService productoService;
    private final NavegacionPublisher navegacion;
    @Value("${app.seguridad.cognito-issuer}")
    private String cognitoIssuer;

    public ProductoController(ProductoService productoService, NavegacionPublisher navegacion){
        this.productoService = productoService;
        this.navegacion = navegacion;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listar(){
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        return productoService.obtenerPorId(id)
                .map(producto -> {
                    String usuario = jwt == null ? null : jwt.getClaimAsString("oid");
                    if (jwt != null && cognitoIssuer.equals(jwt.getClaimAsString("iss"))) {
                        String sub = jwt.getSubject();
                        usuario = sub == null || sub.isBlank() ? null : "cognito:" + sub;
                    }
                    navegacion.productoVisto(producto.getId(), usuario);
                    return ResponseEntity.ok(producto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto){
        Producto producto1 = productoService.guardar(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto1);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return productoService.actualizar(id, producto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/administracion")
    public List<Producto> administracion() { return productoService.obtenerAdministracion(); }

    @PutMapping("/{id}/reactivacion")
    public Producto reactivar(@PathVariable Long id) { return productoService.cambiarActivo(id, true); }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Producto> reducirStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        Producto actualizado = productoService.reducirStock(id, cantidad);
        return ResponseEntity.ok(actualizado);
    }
}
