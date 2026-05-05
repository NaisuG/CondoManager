package condominio.registro.controller;

import condominio.registro.dto.ResidenteUnidadDTO;
import condominio.registro.dto.ResidenteUnidadRequestDTO;
import condominio.registro.service.ResidenteUnidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/residente-unidad")
@RequiredArgsConstructor
public class ResidenteUnidadController {

    private final ResidenteUnidadService residenteUnidadService;

    @GetMapping("/residente/{residenteId}")
    public ResponseEntity<List<ResidenteUnidadDTO>> listarPorResidente(@PathVariable Long residenteId) {
        return ResponseEntity.ok(residenteUnidadService.listarPorResidente(residenteId));
    }

    @GetMapping("/unidad/{unidadId}")
    public ResponseEntity<List<ResidenteUnidadDTO>> listarPorUnidad(@PathVariable Long unidadId) {
        return ResponseEntity.ok(residenteUnidadService.listarPorUnidad(unidadId));
    }

    @PostMapping
    public ResponseEntity<ResidenteUnidadDTO> asignar(@RequestBody ResidenteUnidadRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(residenteUnidadService.asignar(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        residenteUnidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}