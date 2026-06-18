package condominio.registro.controller;

import condominio.registro.dto.ResidenteDTO;
import condominio.registro.dto.ResidenteRequestDTO;
import condominio.registro.service.ResidenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/residentes")
@RequiredArgsConstructor
public class ResidenteController {

    private final ResidenteService residenteService;

    @GetMapping
    public ResponseEntity<List<ResidenteDTO>> listar() {
        return ResponseEntity.ok(residenteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResidenteDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(residenteService.obtenerPorId(id));
    }

    @GetMapping("/run/{run}")
    public ResponseEntity<ResidenteDTO> obtenerPorRun(@PathVariable String run) {
        return ResponseEntity.ok(residenteService.obtenerPorRun(run));
    }

    @PostMapping("/crear")
    public ResponseEntity<ResidenteDTO> crear(@RequestBody ResidenteRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(residenteService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResidenteDTO> actualizar(@PathVariable Long id,
                                                   @RequestBody ResidenteRequestDTO request) {
        return ResponseEntity.ok(residenteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        residenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}