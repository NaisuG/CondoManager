package condominio.backendforfront.service;

import condominio.backendforfront.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
public class BffService {

    private final WebClient webClient;
    private final String urlMantenimiento;
    private final String urlProveedor;
    private final String urlRegistro;

    public BffService(WebClient.Builder webClientBuilder,
                      @Value("${URL_MANTENIMIENTO:http://app-mantenimiento:8080}") String urlMantenimiento,
                      @Value("${URL_PROVEEDOR:http://app-proveedor:8081}") String urlProveedor,
                      @Value("${URL_REGISTRO:http://app-registro:8082}") String urlRegistro) {
        this.webClient = webClientBuilder.build();
        this.urlMantenimiento = urlMantenimiento;
        this.urlProveedor = urlProveedor;
        this.urlRegistro = urlRegistro;
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

        return webClient.get()
                .uri(urlFinal)
                .retrieve()
                .bodyToMono(CondominioFullDTO.class)
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

}