package condominio.backendforfront.service;

import condominio.backendforfront.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public BffService(WebClient webClient,
                      @Value("${URL_MANTENIMIENTO:http://app-mantenimiento:8080}") String urlMantenimiento,
                      @Value("${URL_PROVEEDOR:http://app-proveedor:8081}") String urlProveedor,
                      @Value("${URL_REGISTRO:http://app-registro:8082}") String urlRegistro,
                      @Value("${URL_AUTH:http://app-auth:8085}") String urlAuth,
                      @Value("${URL_CONTABILIDAD:http://app-contabilidad:8083}") String urlContabilidad ) {
        this.webClient = webClientBuilder.build();
        this.urlMantenimiento = urlMantenimiento;
        this.urlProveedor = urlProveedor;
        this.urlRegistro = urlRegistro;
        this.urlAuth = urlAuth;
        this.urlContabilidad = urlContabilidad;
    }
    public Mono<ResponseEntity<Map>> registrarAuth(Map<String, Object> registroDto) {
        return webClient.post()
                .uri("http://app-auth:8085/api/auth/register")
                .header("Content-Type", "application/json")
                .bodyValue(registroDto)
                .retrieve()
                .toEntity(Map.class);
    }

    public Mono<ResponseEntity<Map>> loginAuth(Map<String, Object> loginDto) {
        return this.webClient.post()
                .uri(urlAuth + "/api/auth/login")
                .bodyValue(loginDto)
                .retrieve()
                .toEntity(Map.class)
                .doOnError(e -> System.err.println("Error en BFF-Auth Login: " + e.getMessage()));
    }


    public Flux<OrdenResumenDTO> listarOrdenesDetalladas() {
        return webClient.get()
                .uri(urlMantenimiento + "/api/mantenimiento/ordenes")
                .retrieve()
                .bodyToFlux(Map.class)
                .flatMap(orden -> {
                    String provId = orden.get("idProveedor") != null ? orden.get("idProveedor").toString() : "0";
                    return webClient.get()
                            .uri(urlProveedor + "/api/proveedores/" + provId)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .map(prov -> {
                                OrdenResumenDTO dto = new OrdenResumenDTO();
                                dto.setIdOrden(Long.valueOf(orden.get("id").toString()));
                                dto.setDescripcion(orden.get("descripcion").toString());
                                dto.setNombreEmpresa(prov.get("nombreEmpresa").toString());
                                dto.setEstado(orden.get("estado") != null ? orden.get("estado").toString() : "PENDIENTE");
                                return dto;
                            })
                            .onErrorReturn(crearDTOGenerico(orden));
                })
                .onErrorResume(e -> Flux.empty());
    }

    public Mono<CondominioFullDTO> obtenerCondominioCompleto(Long id) {
        String urlFinal = urlRegistro + "/api/condominios/" + id;
        
        System.out.println("Llamando a Registro en: " + urlFinal);
        System.out.println("Llamando a Registro en: " + urlFinal);

        return webClient.get()
                .uri(urlFinal)
                .retrieve()
                .bodyToMono(CondominioFullDTO.class)
                .doOnNext(res -> System.out.println("Datos recibidos para: " + res.getNombre()))
                .doOnError(e -> System.err.println("Error llamando a Registro: " + e.getMessage()))
                .doOnNext(res -> System.out.println("Datos recibidos para: " + res.getNombre()))
                .doOnError(e -> System.err.println("Error llamando a Registro: " + e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    private OrdenResumenDTO crearDTOGenerico(Map orden) {
        OrdenResumenDTO dto = new OrdenResumenDTO();
        dto.setIdOrden(Long.valueOf(orden.get("id").toString()));
        dto.setDescripcion(orden.get("descripcion").toString());
        dto.setNombreEmpresa("Proveedor no disponible");
        dto.setEstado(orden.get("estado") != null ? orden.get("estado").toString() : "PENDIENTE");
        return dto;
    }

    public Mono<Map> obtenerEstadisticasUnidades() {
        String urlFinal = urlRegistro + "/api/unidades/estadisticas";
        return webClient.get()
            .uri(urlFinal)
            .retrieve()
            .bodyToMono(Map.class);
    }

    public Flux<CondominioFullDTO> listarCondominios() {
    return webClient.get()
        .uri(urlRegistro + "/api/condominios")
        .retrieve()
        .bodyToFlux(Map.class)
        .flatMap(c -> {
            Long id = Long.valueOf(c.get("id").toString());
            return webClient.get()
                .uri(urlRegistro + "/api/condominios/" + id)
                .retrieve()
                .bodyToMono(CondominioFullDTO.class)
                .onErrorResume(e -> Mono.empty());
        });
    }

    public Mono<Map> obtenerEstadisticasFinanzas(Integer mes, Integer anio) {
        String urlFinal = urlContabilidad + "/api/contabilidad/estadisticas?mes=" + mes + "&anio=" + anio;
        return webClient.get()
            .uri(urlFinal)
            .retrieve()
            .bodyToMono(Map.class);
    }
    public Flux<CobroDetalleDTO> listarCobrosConDetalles(Integer mes, Integer anio) {
        String urlCobros = urlContabilidad + "/api/contabilidad/cobros?mes=" + mes + "&anio=" + anio;

        return webClient.get()
                .uri(urlCobros)
                .retrieve()
                .bodyToFlux(Map.class) // Traemos los cobros de contabilidad
                .flatMap(cobro -> {
                    Long idUnidad = Long.valueOf(cobro.get("idUnidad").toString());
                    
                    // Aquí llamamos al microservicio de Registro para traer los detalles de esa unidad específica
                    // Asumimos que tienes un endpoint en Registro que devuelve la unidad completa
                    return webClient.get()
                            .uri(urlRegistro + "/api/unidades/" + idUnidad + "/detalle-completo")
                            .retrieve()
                            .bodyToMono(Map.class)
                            .map(detalleRegistro -> {
                                CobroDetalleDTO dto = new CobroDetalleDTO();
                                
                                // Mapeamos lo de Contabilidad
                                dto.setIdCobro(Long.valueOf(cobro.get("id").toString()));
                                dto.setMes(Integer.valueOf(cobro.get("mes").toString()));
                                dto.setAnio(Integer.valueOf(cobro.get("anio").toString()));
                                dto.setMonto(Integer.valueOf(cobro.get("montoCobrado").toString()));
                                dto.setEstado(cobro.get("estado").toString());

                                // Mapeamos lo de Registro (con validaciones por si viene vacío)
                                dto.setNumeroUnidad(Integer.valueOf(detalleRegistro.getOrDefault("numeroUnidad", 0).toString()));
                                dto.setTipoUnidad(detalleRegistro.getOrDefault("tipoUnidad", "Sin Tipo").toString());
                                dto.setNumeroTorre(Integer.valueOf(detalleRegistro.getOrDefault("numeroTorre", 0).toString()));
                                dto.setNombreCondominio(detalleRegistro.getOrDefault("nombreCondominio", "Desconocido").toString());
                                
                                // El inquilino (lo que mencionaste de los "blanks")
                                dto.setNombreInquilino(detalleRegistro.getOrDefault("nombreResidente", "Sin asignar").toString());

                                return dto;
                            })
                            // Si Registro falla o no encuentra la unidad, devolvemos el cobro con datos en blanco
                            .onErrorReturn(crearCobroEnBlanco(cobro)); 
                });
    }

    // Método auxiliar (ponlo debajo) para cuando la unidad no existe en Registro
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

    public Mono<Object> generarCobrosMasivos(Integer mes, Integer anio) {
        // 1. Buscamos todas las unidades en el MS de Registro
        return webClient.get()
            .uri(urlRegistro + "/api/unidades")
            .retrieve()
            .bodyToFlux(Map.class)
            // 2. Las transformamos al formato que Contabilidad espera
            .map(unidad -> {
                Map<String, Object> peticion = new HashMap<>();
                peticion.put("idUnidad", unidad.get("id"));
                peticion.put("idTipoUnidad", unidad.get("tipoId")); // Ahora sí lo tenemos
                peticion.put("mes", mes);
                peticion.put("anio", anio);
                return peticion;
            })
            .collectList()
            // 3. Enviamos la lista completa al MS de Contabilidad
            .flatMap(listaPeticiones -> {
                if (listaPeticiones.isEmpty()) {
                    return Mono.just("No hay unidades registradas para cobrar");
                }
                return webClient.post()
                    .uri(urlContabilidad + "/api/contabilidad/cobros/masivo")
                    .bodyValue(listaPeticiones)
                    .retrieve()
                    .bodyToMono(Object.class);
            });
    }

    public Mono<Object> actualizarEstadoCobro(Long id, String estado) {
        return webClient.patch()
                .uri(urlContabilidad + "/api/contabilidad/cobros/" + id + "/estado?estado=" + estado)
                .retrieve()
                .bodyToMono(Object.class);
    }
}