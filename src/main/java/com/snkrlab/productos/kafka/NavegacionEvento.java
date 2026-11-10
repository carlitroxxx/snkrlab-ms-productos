package com.snkrlab.productos.kafka;

public record NavegacionEvento(String tipo, Long productoId, String usuarioId, String fechaHora) {}
