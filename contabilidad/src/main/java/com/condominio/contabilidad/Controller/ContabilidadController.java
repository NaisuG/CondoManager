package com.condominio.contabilidad.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.condominio.contabilidad.DTO.EstadisticasFinanzasDTO;
import com.condominio.contabilidad.DTO.GenerarCobroRequestDTO;
import com.condominio.contabilidad.DTO.TarifaRequestDTO;
import com.condominio.contabilidad.Model.CobroMensual;
import com.condominio.contabilidad.Model.EstadoCobro;
import com.condominio.contabilidad.Service.ContabilidadService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contabilidad")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor

public class ContabilidadController {

    private final ContabilidadService service;

    @GetMapping("/cobros")
    public ResponseEntity<List<CobroMensual>> listarCobros(@RequestParam Integer mes, @RequestParam Integer anio) {
        return ResponseEntity.ok(service.listarCobrosMes(mes, anio));
    }

    @PatchMapping("/cobros/{id}/estado")
    public ResponseEntity<CobroMensual> actualizarEstado(@PathVariable Long id, @RequestParam EstadoCobro estado) {
        return ResponseEntity.ok(service.cambiarEstadoPago(id, estado));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasFinanzasDTO> estadisticas(@RequestParam Integer mes, @RequestParam Integer anio) {
        return ResponseEntity.ok(service.obtenerEstadisticasMes(mes, anio));
    }

    @PostMapping("/cobros")
    public ResponseEntity<CobroMensual> generarCobro(@RequestBody GenerarCobroRequestDTO request) {
        CobroMensual nuevoCobro = service.generarCobroMensual(
                request.getIdUnidad(), 
                request.getIdTipoUnidad(), 
                request.getMes(), 
                request.getAnio()
        );
        return ResponseEntity.ok(nuevoCobro);
    }

    @PostMapping("/tarifas")
    public ResponseEntity<com.condominio.contabilidad.Model.ValorGastoComun> configurarTarifa(@RequestBody TarifaRequestDTO request) {
        return ResponseEntity.ok(service.configurarTarifa(request.getIdTipoUnidad(), request.getMonto()));
    }

    @PostMapping("/cobros/masivo")
    public ResponseEntity<List<CobroMensual>> generarCobrosMasivos(@RequestBody List<GenerarCobroRequestDTO> peticiones) {
        return ResponseEntity.ok(service.generarCobrosMasivos(peticiones));
    }
}
