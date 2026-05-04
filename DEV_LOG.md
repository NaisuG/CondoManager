Stack Tecnologico
- Java 17 y Spring Boot 4.0.5
- BD PostgreSQL en Docker
- Comunicacion con servicios usando Spring Cloud OpenFeign
- Lombok para evitar redundancia

Decisiones de Arquitectura y Reglas de Negocio
- Enum de EstadoOrden para restringir estados posibles a PENDIENTE, EN_PROCESO, FINALIZADA y CANCELADA.
- Eliminacion logica mediante cambio de estado a CANCELADA para mantener trazabilidad de las solicitudes.
- Desacoplamiento de Datos del Proveedor: esta base de datos solo almacena el identificador numerico del proveedor. Toda la informacion descriptiva se obtiene en tiempo real consultando al microservicio externo.

Funcionalidades y Puntos de Acceso
- POST (/api/mantenimiento/ordenes): Registro de nuevas ordenes. Por defecto, el sistema asigna el estado PENDIENTE.
- GET (/api/mantenimiento/ordenes): Listado de todas las ordenes registradas.
- GET (/api/mantenimiento/ordenes/{id}): Obtiene el detalle de la orden junto con los datos obtenidos del microservicio de proveedor.
- PUT (/api/mantenimiento/ordenes/{id}/cancelar): Cambia el estado de la orden a CANCELADA.

Deuda Tecnica y Mejoras Pendientes
- Seguridad: Quitar el bypass temporal de pruebas (permitAll) e implementar OAuth2 Resource Server con Keycloak.
- Conexion externa: Cambiar el try-catch temporal del FeignClient por un Circuit Breaker (Resilience4j) para tolerancia a fallos.
- Paginacion: Modificar el listado (findAll) para que use paginacion (Pageable) pensando en volumenes grandes de datos.
- Excepciones: Implementar un manejador global de errores (RestControllerAdvice) para devolver JSON limpios en vez de errores 500.
- Credenciales: Mover los datos de conexion de PostgreSQL a variables de entorno antes de subir a produccion.

Despliegue Local
- Iniciar BD: docker compose up -d