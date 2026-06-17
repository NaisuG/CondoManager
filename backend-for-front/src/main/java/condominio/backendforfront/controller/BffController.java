package condominio.backendforfront.controller;

import condominio.backendforfront.dto.*;
import condominio.backendforfront.service.BffService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/bff")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.DELETE})
public class BffController {
    private final BffService bffService;

    public BffController(BffService bffService) {
        this.bffService = bffService;
    }

    @PostMapping("/auth/register")
    public Mono<ResponseEntity<Map>> registrarUsuario(@RequestBody Map<String, Object> registroDto) {
        return bffService.registrarAuth(registroDto);
    }

    @PostMapping("/auth/login")
    public Mono<ResponseEntity<Map>> loginUsuario(@RequestBody Map<String, Object> loginDto) {
        return bffService.loginAuth(loginDto);
    }

    @GetMapping("/mantenimientos")
    public Flux<OrdenResumenDTO> getOrdenes() {
        return bffService.listarOrdenesDetalladas();
    }

    @GetMapping("/registro/condominios")
    public Flux<CondominioFullDTO> getCondominios() {
        return bffService.listarCondominios();
    }

    @GetMapping("/registro/condominio/{id}")
    public Mono<CondominioFullDTO> getCondominio(@PathVariable Long id) {
        return bffService.obtenerCondominioCompleto(id);
    }

    @GetMapping("/registro/estadisticas/unidades")
    public Mono<Map> getEstadisticasUnidades() {
        return bffService.obtenerEstadisticasUnidades();
    }

    @GetMapping("/registro/tipos-unidad")
    public Flux<Map> getTiposUnidad() {
        return bffService.listarTiposUnidad();
    }

    @GetMapping("/contabilidad/estadisticas")
    public Mono<Map> getEstadisticasFinanzas(@RequestParam Integer mes, @RequestParam Integer anio) {
        return bffService.obtenerEstadisticasFinanzas(mes, anio);
    }

    @GetMapping("/contabilidad/cobros-detallados")
    public Flux<CobroDetalleDTO> getCobrosDetallados(@RequestParam Integer mes, @RequestParam Integer anio) {
        return bffService.listarCobrosConDetalles(mes, anio);
    }

    @PostMapping("/contabilidad/generar-mes")
    public Mono<ResponseEntity<Object>> generarCobrosMasivos(@RequestParam Integer mes, @RequestParam Integer anio) {
        return bffService.generarCobrosMasivos(mes, anio)
                .map(resultado -> ResponseEntity.ok().body(resultado))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(Map.of("error", e.getMessage()))));
    }

    @PatchMapping("/contabilidad/cobros/{id}/estado")
    public Mono<ResponseEntity<Object>> actualizarEstadoCobro(@PathVariable Long id, @RequestParam String estado) {
        return bffService.actualizarEstadoCobro(id, estado)
                .map(resultado -> ResponseEntity.ok().body(resultado))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(Map.of("error", e.getMessage()))));
    }

    @PostMapping("/contabilidad/tarifas")
    public Mono<ResponseEntity<Object>> guardarTarifa(@RequestBody Map<String, Object> tarifaDto) {
        return bffService.guardarTarifa(tarifaDto)
                .map(resultado -> ResponseEntity.ok().body(resultado))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(Map.of("error", e.getMessage()))));
    }

    @GetMapping("/contabilidad/tarifas")
    public Flux<Map> getTarifas() {
        return bffService.listarTarifas();
    }

    // --- NUEVOS ENDPOINTS DE DOCUMENTOS ---
    @GetMapping("/documentos/condominio/{idCondominio}")
    public Flux<Map> getDocumentos(@PathVariable Long idCondominio) {
        return bffService.listarDocumentosPorCondominio(idCondominio);
    }

    @GetMapping("/documentos/{id}/descargar")
    public Mono<Map> getLinkDescarga(@PathVariable Long id) {
        return bffService.obtenerLinkDescarga(id);
    }

    @PostMapping(value = "/documentos/subir", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map>> subirDocumento(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("idCondominio") Long idCondominio,
            @RequestParam("idUsuarioSubio") Long idUsuarioSubio) {
        
        return bffService.subirDocumento(archivo, idCondominio, idUsuarioSubio)
                .map(resultado -> ResponseEntity.ok().body(resultado))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(Map.of("error", e.getMessage()))));
    }
}