package condominio.backendforfront.service;

import condominio.backendforfront.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
public class BffService {
    private final WebClient webClient;

    public BffService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    public Flux<OrdenResumenDTO> listarOrdenesDetalladas() {
        return webClient.get().uri("http://ms_mantenimiento:8080/api/ordenes").retrieve().bodyToFlux(Map.class)
                .flatMap(orden -> {
                    String provId = orden.get("idProveedor").toString();
                    return webClient.get().uri("http://ms_proveedor:8081/api/proveedores/" + provId).retrieve().bodyToMono(Map.class)
                            .map(prov -> {
                                OrdenResumenDTO dto = new OrdenResumenDTO();
                                dto.setIdOrden(Long.valueOf(orden.get("id").toString()));
                                dto.setDescripcion(orden.get("descripcion").toString());
                                dto.setNombreEmpresa(prov.get("nombreEmpresa").toString());
                                return dto;
                            });
                });
    }

    public Mono<CondominioFullDTO> obtenerCondominioCompleto(Long id) {
        return webClient.get().uri("http://ms_registro:8082/api/condominios/" + id + "/completo")
                .retrieve().bodyToMono(CondominioFullDTO.class);
    }
}