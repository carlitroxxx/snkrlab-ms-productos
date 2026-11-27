# Desactivación de productos

Aplicar manualmente productos-activo.sql antes de arrancar Productos actualizado.
No se ejecutó la migración. La columna es NOT NULL DEFAULT TRUE y conserva todas
las filas, precios, stock e identificadores existentes.

DELETE /api/v1/productos/{id} ahora desactiva, de manera idempotente.
PUT /api/v1/productos/{id}/reactivacion reactiva sin reescribir stock ni descripción.
GET /api/v1/productos/administracion incluye activos e inactivos y exige Administrador.
GET público lista solo activos; GET por ID devuelve 404 si está inactivo.
PUT normal conserva el estado activo actual; POST crea activo y no admite ID.

Carrito consulta la disponibilidad antes de agregar y antes de registrar un nuevo
intento de pago, tanto simulado como Webpay. Una respuesta 404 al validar un
checkout sin intento lo cierra RECHAZADO con resultado PRODUCTO_INACTIVO, sin
llamar a Pago. El frontend permite retirar los artículos usando su liberación
verificada existente. Si Productos no responde, se bloquea el inicio, sin
interpretarlo como producto desactivado.

Los intentos de pago ya iniciados (incluidos inciertos y aprobados) conservan su
recuperación. El descuento de stock de esas compras no filtra por activo: hacerlo
podría dejar una compra cobrada sin completar. No se alteran órdenes, snapshots,
comprobantes ni operaciones idempotentes de inventario.

Límite de concurrencia: la comprobación HTTP de activo y el registro del intento
en Carrito no forman una transacción distribuida. Una desactivación concurrente
posterior a una comprobación positiva puede coexistir con ese inicio de pago.
Esta implementación no ofrece un corte atómico de pagos entre servicios.
Garantizarlo exigiría una reserva/admisión persistente coordinada en Productos,
con recuperación de reservas y tratamiento de checkouts anteriores.

Recomendaciones usa el catálogo público filtrado: no cambia su algoritmo ni los
perfiles persistidos. No se modificó ese servicio ni se borraron interacciones.
La administración muestra estado y solicita confirmación antes de cambiarlo.
Los comprobantes preservan textos y precios históricos; la imagen puede quedar
sin disponibilidad al usar el catálogo público.

Pruebas aisladas de Productos y Carrito y compilación frontend; no se ejecutan
contextLoads que requieren infraestructura real. Pendientes migración y prueba
integrada con las bases locales tras actualizar los servicios.
