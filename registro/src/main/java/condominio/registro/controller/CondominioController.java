package condominio.registro.controller;

import condominio.registro.dto.CondominioDTO;
import condominio.registro.dto.CondominioDetalleDTO;
import condominio.registro.dto.CondominioRequestDTO;
import condominio.registro.service.CondominioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/condominios")
@RequiredArgsConstructor
public class CondominioController {

    private final CondominioService condominioService;

    @GetMapping
    public ResponseEntity<List<CondominioDTO>> listar() {
        return ResponseEntity.ok(condominioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CondominioDetalleDTO> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(condominioService.obtenerDetallePorId(id));
    }

    @PostMapping
    public ResponseEntity<CondominioDTO> crear(@RequestBody CondominioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(condominioService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CondominioDTO> actualizar(@PathVariable Long id,
                                                    @RequestBody CondominioRequestDTO request) {
        return ResponseEntity.ok(condominioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        condominioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}