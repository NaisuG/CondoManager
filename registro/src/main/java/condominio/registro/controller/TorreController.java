package condominio.registro.controller;

import condominio.registro.dto.TorreDTO;
import condominio.registro.dto.TorreRequestDTO;
import condominio.registro.service.TorreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/torres")
@RequiredArgsConstructor
public class TorreController {

    private final TorreService torreService;

    @GetMapping
    public ResponseEntity<List<TorreDTO>> listar() {
        return ResponseEntity.ok(torreService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TorreDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(torreService.obtenerPorId(id));
    }

    @GetMapping("/condominio/{condominioId}")
    public ResponseEntity<List<TorreDTO>> listarPorCondominio(@PathVariable Long condominioId) {
        return ResponseEntity.ok(torreService.listarPorCondominio(condominioId));
    }

    @PostMapping
    public ResponseEntity<TorreDTO> crear(@RequestBody TorreRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(torreService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TorreDTO> actualizar(@PathVariable Long id,
                                               @RequestBody TorreRequestDTO request) {
        return ResponseEntity.ok(torreService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        torreService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}