package com.snkrlab.productos.kafka;

import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NavegacionPublisher {
    private static final Logger log = LoggerFactory.getLogger(NavegacionPublisher.class);
    private static final String TOPICO = "snkrlab-navegacion";
    private final KafkaTemplate<String, Object> kafka;
    // Kafka.send puede esperar metadatos: nunca ejecutarlo en el hilo HTTP.
    private final ThreadPoolExecutor envios = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(100),
            tarea -> {
                Thread hilo = new Thread(tarea, "kafka-navegacion");
                hilo.setDaemon(true);
                return hilo;
            }, new ThreadPoolExecutor.AbortPolicy());

    public NavegacionPublisher(KafkaTemplate<String, Object> kafka) {
        this.kafka = kafka;
    }

    public void productoVisto(Long productoId, String usuarioId) {
        NavegacionEvento evento = new NavegacionEvento("PRODUCTO_VISTO", productoId,
                usuarioId == null || usuarioId.isBlank() ? null : usuarioId, Instant.now().toString());
        try {
            envios.execute(() -> enviar(evento));
        } catch (RejectedExecutionException ex) {
            log.warn("Evento de navegacion omitido: emisor ocupado o detenido, producto={}", productoId);
        }
    }

    private void enviar(NavegacionEvento evento) {
        try {
            kafka.send(TOPICO, evento.productoId().toString(), evento)
                    .whenComplete((resultado, error) -> {
                        if (error != null)
                            log.warn("No se pudo publicar navegacion: producto={}", evento.productoId());
                    });
        } catch (RuntimeException ex) {
            log.warn("No se pudo iniciar publicacion de navegacion: producto={}", evento.productoId());
        }
    }

    @PreDestroy
    public void cerrar() {
        envios.shutdownNow();
    }
}
