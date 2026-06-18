package condominio.backendforfront.service;

import condominio.backendforfront.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class BffService {

    private final WebClient webClient;
    private final String urlMantenimiento;
    private final String urlProveedor;
    private final String urlRegistro;
    private final String urlAuth;
    private final String urlContabilidad;
    private final String urlDocumentos;

    public BffService(WebClient.Builder webClientBuilder,
                      @Value("${URL_MANTENIMIENTO:http://app-mantenimiento:8080}") String urlMantenimiento,
                      @Value("${URL_PROVEEDOR:http://app-proveedor:8081}") String urlProveedor,
                      @Value("${URL_REGISTRO:http://app-registro:8082}") String urlRegistro,
                      @Value("${URL_AUTH:http://app-auth:8085}") String urlAuth,
                      @Value("${URL_CONTABILIDAD:http://app-contabilidad:8083}") String urlContabilidad,
                      @Value("${URL_DOCUMENTOS:http://app-documentos:8084}") String urlDocumentos) {
        this.webClient = webClientBuilder.build();
        this.urlMantenimiento = urlMantenimiento;
        this.urlProveedor = urlProveedor;
        this.urlRegistro = urlRegistro;
        this.urlAuth = urlAuth;
        this.urlContabilidad = urlContabilidad;
        this.urlDocumentos = urlDocumentos;
    }

    // --- AUTH ---
    public Mono<ResponseEntity<Map>> registrarAuth(Map<String, Object> registroDto) {
        return webClient.post().uri(urlAuth + "/api/auth/register")
                .header("Content-Type", "application/json")
                .bodyValue(registroDto).retrieve().toEntity(Map.class);
    }

    public Mono<ResponseEntity<Map>> loginAuth(Map<String, Object> loginDto) {
        return webClient.post().uri(urlAuth + "/api/auth/login")
                .bodyValue(loginDto).retrieve().toEntity(Map.class)
                .doOnError(e -> System.err.println("Error en BFF-Auth Login: " + e.getMessage()));
    }

    // --- REGISTRO ---
    public Mono<ResponseEntity<Map>> crearCondominio(Map<String, Object> condominioDto) {
    // Reconstruimos el mapa garantizando que las llaves sean las que el microservicio espera
    Map<String, Object> payloadParaMicroservicio = new HashMap<>();
    
    // Obtenemos el ID de usuario tolerando tanto camelCase como snake_case desde el front
    Object idUsuario = condominioDto.get("id_usuario") != null ? 
                       condominioDto.get("id_usuario") : condominioDto.get("idUsuario");
                       
    payloadParaMicroservicio.put("id_usuario", idUsuario); //
    payloadParaMicroservicio.put("nombre", condominioDto.get("nombre"));
    payloadParaMicroservicio.put("direccion", condominioDto.get("direccion"));

    return webClient.post()
            .uri(urlRegistro + "/api/registro/condominios/crear")
            .bodyValue(payloadParaMicroservicio) // Envia el payload limpio
            .retrieve()
            .toEntity(Map.class);
    }

    public Flux<CondominioFullDTO> listarCondominios() {
        return webClient.get().uri(urlRegistro + "/api/condominios").retrieve().bodyToFlux(Map.class)
                .flatMap(c -> {
                    Long id = Long.valueOf(c.get("id").toString());
                    return webClient.get().uri(urlRegistro + "/api/condominios/" + id).retrieve()
                            .bodyToMono(CondominioFullDTO.class).onErrorResume(e -> Mono.empty());
                });
    }

    public Mono<ResponseEntity<Map>> crearTipoUnidad(Map<String, Object> dto) {
    return webClient.post().uri(urlRegistro + "/api/registro/tipos-unidad/crear").bodyValue(dto).retrieve().toEntity(Map.class);
    }

    public Mono<ResponseEntity<Map>> crearTorre(Map<String, Object> dto) {
        return webClient.post().uri(urlRegistro + "/api/registro/torres/crear").bodyValue(dto).retrieve().toEntity(Map.class);
    }

    public Mono<ResponseEntity<Map>> crearUnidad(Map<String, Object> dto) {
        return webClient.post().uri(urlRegistro + "/api/registro/unidades/crear").bodyValue(dto).retrieve().toEntity(Map.class);
    }

    public Mono<ResponseEntity<Map>> crearResidente(Map<String, Object> dto) {
        return webClient.post().uri(urlRegistro + "/api/registro/residentes/crear").bodyValue(dto).retrieve().toEntity(Map.class);
    }

    public Mono<ResponseEntity<Map>> unirResidenteAUnidad(Long idResidente, Long idUnidad) {
        return webClient.post().uri("/api/residente-unidad/asignar")
                .retrieve().toEntity(Map.class);
    }

    public Mono<CondominioFullDTO> obtenerCondominioCompleto(Long id) {
        return webClient.get().uri(urlRegistro + "/api/condominios/" + id).retrieve()
                .bodyToMono(CondominioFullDTO.class).onErrorResume(e -> Mono.empty());
    }

    public Mono<Map> obtenerEstadisticasUnidades() {
        return webClient.get().uri(urlRegistro + "/api/unidades/estadisticas").retrieve().bodyToMono(Map.class);
    }

    public Flux<Map> listarTiposUnidad() {
        return webClient.get().uri(urlRegistro + "/api/tipos-unidad").retrieve().bodyToFlux(Map.class)
                .onErrorResume(e -> Flux.empty());
    }

    // --- MANTENIMIENTO ---
    public Flux<OrdenResumenDTO> listarOrdenesDetalladas() {
        return webClient.get().uri(urlMantenimiento + "/api/mantenimiento/ordenes").retrieve().bodyToFlux(Map.class)
                .flatMap(orden -> {
                    String provId = orden.get("idProveedor") != null ? orden.get("idProveedor").toString() : "0";
                    return webClient.get().uri(urlProveedor + "/api/proveedores/" + provId).retrieve().bodyToMono(Map.class)
                            .map(prov -> {
                                OrdenResumenDTO dto = new OrdenResumenDTO();
                                dto.setIdOrden(Long.valueOf(orden.get("id").toString()));
                                dto.setDescripcion(orden.get("descripcion").toString());
                                dto.setNombreEmpresa(prov.get("nombreEmpresa").toString());
                                dto.setEstado(orden.get("estado") != null ? orden.get("estado").toString() : "PENDIENTE");
                                return dto;
                            }).onErrorReturn(crearDTOGenerico(orden));
                }).onErrorResume(e -> Flux.empty());
    }

    private OrdenResumenDTO crearDTOGenerico(Map orden) {
        OrdenResumenDTO dto = new OrdenResumenDTO();
        dto.setIdOrden(Long.valueOf(orden.get("id").toString()));
        dto.setDescripcion(orden.get("descripcion").toString());
        dto.setNombreEmpresa("Proveedor no disponible");
        dto.setEstado(orden.get("estado") != null ? orden.get("estado").toString() : "PENDIENTE");
        return dto;
    }

    // --- CONTABILIDAD ---
    public Mono<Map> obtenerEstadisticasFinanzas(Integer mes, Integer anio) {
        return webClient.get().uri(urlContabilidad + "/api/contabilidad/estadisticas?mes=" + mes + "&anio=" + anio)
                .retrieve().bodyToMono(Map.class);
    }

    public Flux<Map> listarTarifas() {
        return webClient.get().uri(urlContabilidad + "/api/contabilidad/tarifas").retrieve().bodyToFlux(Map.class)
                .onErrorResume(e -> Flux.empty());
    }

    public Mono<Object> guardarTarifa(Map<String, Object> tarifaDto) {
        return webClient.post().uri(urlContabilidad + "/api/contabilidad/tarifas")
                .header("Content-Type", "application/json").bodyValue(tarifaDto).retrieve().bodyToMono(Object.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("No se pudo guardar la tarifa: " + e.getMessage())));
    }

    public Mono<Object> actualizarEstadoCobro(Long id, String estado) {
        return webClient.patch().uri(urlContabilidad + "/api/contabilidad/cobros/" + id + "/estado?estado=" + estado)
                .retrieve().bodyToMono(Object.class);
    }

    public Mono<Object> generarCobrosMasivos(Integer mes, Integer anio) {
        return webClient.get().uri(urlRegistro + "/api/unidades").retrieve().bodyToFlux(Map.class)
                .map(unidad -> {
                    Map<String, Object> peticion = new HashMap<>();
                    peticion.put("idUnidad", unidad.get("id"));
                    Object tipoId = unidad.get("tipoId");
                    if (tipoId == null) tipoId = unidad.get("idTipoUnidad");
                    peticion.put("idTipoUnidad", tipoId != null ? tipoId : 1);
                    peticion.put("mes", mes);
                    peticion.put("anio", anio);
                    return peticion;
                }).collectList()
                .flatMap(listaPeticiones -> {
                    if (listaPeticiones.isEmpty()) return Mono.just(Map.of("mensaje", "No hay unidades registradas"));
                    return webClient.post().uri(urlContabilidad + "/api/contabilidad/cobros/masivo")
                            .header("Content-Type", "application/json").bodyValue(listaPeticiones).retrieve()
                            .bodyToMono(Object.class)
                            .onErrorResume(e -> Mono.error(new RuntimeException("Rechazo de contabilidad: " + e.getMessage())));
                });
    }

    public Flux<CobroDetalleDTO> listarCobrosConDetalles(Integer mes, Integer anio) {
    return webClient.get().uri(urlContabilidad + "/api/contabilidad/cobros?mes=" + mes + "&anio=" + anio)
            .retrieve().bodyToFlux(Map.class).flatMap(cobro -> {
                Long idUnidad = Long.valueOf(cobro.get("idUnidad").toString());
                
                return webClient.get().uri(urlRegistro + "/api/unidades/" + idUnidad + "/detalle-completo")
                        .retrieve()
                        .bodyToMono(Map.class)
                        // SONDA 1: Si responde bien, imprimimos el JSON que nos entregó ms-registro
                        .doOnNext(map -> System.out.println("EXITO ms-registro (Unidad " + idUnidad + "): " + map))
                        .map(detalleRegistro -> {
                            CobroDetalleDTO dto = new CobroDetalleDTO();
                            dto.setIdCobro(Long.valueOf(cobro.get("id").toString()));
                            dto.setMes(Integer.valueOf(cobro.get("mes").toString()));
                            dto.setAnio(Integer.valueOf(cobro.get("anio").toString()));
                            dto.setMonto(Integer.valueOf(cobro.get("montoCobrado").toString()));
                            dto.setEstado(cobro.get("estado").toString());
                            dto.setNumeroUnidad(Integer.valueOf(detalleRegistro.getOrDefault("numeroUnidad", 0).toString()));
                            dto.setTipoUnidad(detalleRegistro.getOrDefault("tipoUnidad", "Sin Tipo").toString());
                            dto.setNumeroTorre(Integer.valueOf(detalleRegistro.getOrDefault("numeroTorre", 0).toString()));
                            dto.setNombreCondominio(detalleRegistro.getOrDefault("nombreCondominio", "Desconocido").toString());
                            dto.setNombreInquilino(detalleRegistro.getOrDefault("nombreResidente", "Sin asignar").toString());
                            
                            Object idCondoObj = detalleRegistro.get("idCondominio");
                            if (idCondoObj == null) {
                                idCondoObj = detalleRegistro.get("condominioId");
                            }
                            if (idCondoObj == null && detalleRegistro.get("condominio") != null) {
                                idCondoObj = ((java.util.Map<?, ?>) detalleRegistro.get("condominio")).get("id");
                            }
                            
                            dto.setIdCondominio(idCondoObj != null ? Long.valueOf(idCondoObj.toString()) : null);
                            return dto;
                        })
                        // SONDA 2: Si falla la conexión HTTP, imprimimos el error exacto
                        .doOnError(e -> System.err.println("ERROR crítico llamando a ms-registro para Unidad " + idUnidad + ": " + e.getMessage()))
                        .onErrorReturn(crearCobroEnBlanco(cobro));
            });
}   

    private CobroDetalleDTO crearCobroEnBlanco(Map cobro) {
    CobroDetalleDTO dto = new CobroDetalleDTO();
    dto.setIdCobro(Long.valueOf(cobro.get("id").toString()));
    dto.setMes(Integer.valueOf(cobro.get("mes").toString()));
    dto.setAnio(Integer.valueOf(cobro.get("anio").toString()));
    dto.setMonto(Integer.valueOf(cobro.get("montoCobrado").toString()));
    dto.setEstado(cobro.get("estado").toString());
    dto.setNumeroUnidad(0);
    dto.setTipoUnidad("N/A");
    dto.setNumeroTorre(0);
    dto.setNombreCondominio("N/A");
    dto.setNombreInquilino("Sin asignar");
    // Eliminamos el dto.setIdCondominio(1L); para evitar el guardado en Edificio Central
    dto.setIdCondominio(null); 
    return dto;
    }

    // --- DOCUMENTOS ---
    public Flux<Map> listarDocumentosPorCondominio(Long idCondominio) {
        return webClient.get().uri(urlDocumentos + "/api/documentos/condominio/" + idCondominio)
                .retrieve().bodyToFlux(Map.class).onErrorResume(e -> Flux.empty());
    }

    public Mono<Map> obtenerLinkDescarga(Long idDocumento) {
        return webClient.get().uri(urlDocumentos + "/api/documentos/" + idDocumento + "/descargar")
                .retrieve().bodyToMono(Map.class);
    }

    // --- SUBIDA DE DOCUMENTOS ---
    public Mono<Map> subirDocumento(MultipartFile archivo, Long idCondominio, String nombreCarpeta, Long idUsuarioSubio, String categoria, String periodo) {
    try {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("archivo", new ByteArrayResource(archivo.getBytes()) {
            @Override
            public String getFilename() {
                return archivo.getOriginalFilename();
            }
        });
        builder.part("idCondominio", idCondominio); // Envía el Long para la Base de Datos
        builder.part("nombreCarpeta", nombreCarpeta); // Envía el String para MinIO
        builder.part("idUsuarioSubio", idUsuarioSubio);
        builder.part("categoria", categoria);
        builder.part("periodo", periodo);

        return webClient.post()
                .uri(urlDocumentos + "/api/documentos/subir")
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(Map.class);
    } catch (Exception e) {
        return Mono.error(new RuntimeException("Error en BFF al procesar multipart: " + e.getMessage()));
    }
}

    public Flux<Map> listarDocumentosPorCategoria(Long idCondominio, String categoria) {
    return webClient.get()
            .uri(urlDocumentos + "/api/documentos/condominio/" + idCondominio + "/categoria/" + categoria)
            .retrieve()
            .bodyToFlux(Map.class)
            .onErrorResume(e -> Flux.empty());
    }

   public Mono<Object> procesarPagoConComprobante(Long idCobro, MultipartFile archivo, Long idCondominio, String nombreCarpeta, Long idUsuario, String periodo) {
    return subirDocumento(archivo, idCondominio, nombreCarpeta, idUsuario, "COMPROBANTE", periodo)
            .flatMap(docGuardado -> {
                Long idDoc = Long.valueOf(docGuardado.get("id").toString());
                return webClient.patch()
                        .uri(urlContabilidad + "/api/contabilidad/cobros/" + idCobro + "/registrar-pago?idDocumento=" + idDoc)
                        .retrieve()
                        .bodyToMono(Object.class);
            });
}

// Reversión de pago: Limpia el vínculo del documento en contabilidad y vuelve a PENDIENTE
    public Mono<Object> revertirPagoAPendiente(Long idCobro) {
    return webClient.patch()
            .uri(urlContabilidad + "/api/contabilidad/cobros/" + idCobro + "/revertir")
            .retrieve()
            .bodyToMono(Object.class);
}
}