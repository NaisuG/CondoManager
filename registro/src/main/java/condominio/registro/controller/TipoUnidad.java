package condominio.registro.controller;

import condominio.registro.dto.TipoUnidadDTO;
import condominio.registro.dto.TipoUnidadRequestDTO;
import condominio.registro.service.TipoUnidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-unidad")
@RequiredArgsConstructor
public class TipoUnidadController {

    private final TipoUnidadService tipoUnidadService;

    @GetMapping
    public ResponseEntity<List<TipoUnidadDTO>> listar() {
        return ResponseEntity.ok(tipoUnidadService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoUnidadDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tipoUnidadService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<TipoUnidadDTO> crear(@RequestBody TipoUnidadRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoUnidadService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoUnidadDTO> actualizar(@PathVariable Long id,
                                                    @RequestBody TipoUnidadRequestDTO request) {
        return ResponseEntity.ok(tipoUnidadService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tipoUnidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}