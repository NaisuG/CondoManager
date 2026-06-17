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
    public Flux<CondominioFullDTO> listarCondominios() {
        return webClient.get().uri(urlRegistro + "/api/condominios").retrieve().bodyToFlux(Map.class)
                .flatMap(c -> {
                    Long id = Long.valueOf(c.get("id").toString());
                    return webClient.get().uri(urlRegistro + "/api/condominios/" + id).retrieve()
                            .bodyToMono(CondominioFullDTO.class).onErrorResume(e -> Mono.empty());
                });
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
                            .retrieve().bodyToMono(Map.class).map(detalleRegistro -> {
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
                                return dto;
                            }).onErrorReturn(crearCobroEnBlanco(cobro));
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
    public Mono<Map> subirDocumento(MultipartFile archivo, Long idCondominio, Long idUsuarioSubio) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            
            builder.part("archivo", new ByteArrayResource(archivo.getBytes()) {
                @Override
                public String getFilename() {
                    return archivo.getOriginalFilename();
                }
            });
            builder.part("idCondominio", idCondominio);
            builder.part("idUsuarioSubio", idUsuarioSubio);

            return webClient.post()
                    .uri(urlDocumentos + "/api/documentos/subir")
                    .bodyValue(builder.build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> Mono.error(new RuntimeException("Error en ms-documentos: " + e.getMessage())));
                    
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error procesando el archivo en el BFF: " + e.getMessage()));
        }
    }
}