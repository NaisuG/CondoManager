package condominio.backendforfront.controller;

import condominio.backendforfront.dto.*;
import condominio.backendforfront.service.BffService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/bff")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class BffController {
    private final BffService bffService;

    public BffController(BffService bffService) {
        this.bffService = bffService;
    }

    @GetMapping("/mantenimientos")
    public Flux<OrdenResumenDTO> getOrdenes() {
        return bffService.listarOrdenesDetalladas();
    }

    @GetMapping("/registro/condominio/{id}")
    public Mono<CondominioFullDTO> getCondominio(@PathVariable Long id) {
        return bffService.obtenerCondominioCompleto(id);
    }

    @GetMapping("/registro/estadisticas/unidades")
    public Mono<Map> getEstadisticasUnidades() {
        return bffService.obtenerEstadisticasUnidades();
    }

    @GetMapping("/registro/condominios")
    public Flux<CondominioFullDTO> getCondominios() {
        return bffService.listarCondominios();
    }

    @PostMapping("/auth/register")
    public Mono<ResponseEntity<Map>> registrarUsuario(@RequestBody Map<String, Object> registroDto) {
        return bffService.registrarAuth(registroDto);
    }

    @PostMapping("/auth/login")
    public Mono<ResponseEntity<Map>> loginUsuario(@RequestBody Map<String, Object> loginDto) {
        return bffService.loginAuth(loginDto);
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
    public Mono<Object> generarCobrosMasivos(@RequestParam Integer mes, @RequestParam Integer anio) {
        return bffService.generarCobrosMasivos(mes, anio);
    }

    @PatchMapping("/contabilidad/cobros/{id}/estado")
    public Mono<Object> actualizarEstadoCobro(@PathVariable Long id, @RequestParam String estado) {
        return bffService.actualizarEstadoCobro(id, estado);
    }
}