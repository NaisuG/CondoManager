package condominio.registro.controller;

import condominio.registro.dto.UnidadDTO;
import condominio.registro.dto.UnidadDetalleCompletoDTO;
import condominio.registro.dto.UnidadRequestDTO;
import condominio.registro.service.UnidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
public class UnidadController  {


    private final UnidadService unidadService;

    @GetMapping
    public ResponseEntity<List<UnidadDTO>> listar() {
        return ResponseEntity.ok(unidadService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnidadDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(unidadService.obtenerPorId(id));
    }

    @GetMapping("/torre/{torreId}")
    public ResponseEntity<List<UnidadDTO>> listarPorTorre(@PathVariable Long torreId) {
        return ResponseEntity.ok(unidadService.listarPorTorre(torreId));
    }

    @PostMapping
    public ResponseEntity<UnidadDTO> crear(@RequestBody UnidadRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(unidadService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnidadDTO> actualizar(@PathVariable Long id,
                                                @RequestBody UnidadRequestDTO request) {
        return ResponseEntity.ok(unidadService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        unidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Long>> estadisticasUnidades() {
        return ResponseEntity.ok(unidadService.obtenerEstadisticasUnidades());
    }

    @GetMapping("/{id}/detalle-completo")
    public ResponseEntity<UnidadDetalleCompletoDTO> obtenerDetalleCompleto(@PathVariable Long id) {
        return ResponseEntity.ok(unidadService.obtenerDetalleCompleto(id));
    }
}