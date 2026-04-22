package com.condominio.mantenimiento.Controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.condominio.mantenimiento.DTO.OrdenDetalleDTO;
import com.condominio.mantenimiento.Modelo.OrdenMantenimiento;
import com.condominio.mantenimiento.Servicio.MantenimientoService;

@RestController
@RequestMapping("/api/mantenimiento/ordenes")
public class MantenimientoControlador {

    @Autowired
    private MantenimientoService service;

    @PostMapping
    public ResponseEntity<OrdenMantenimiento> crearOrden(@Validated @RequestBody OrdenMantenimiento orden) {
        OrdenMantenimiento nuevaOrden = service.crearOrden(orden);
        return new ResponseEntity<>(nuevaOrden, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrdenMantenimiento>> listarOrdenes() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenDetalleDTO> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerDetalleOrden(id));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<OrdenMantenimiento> cancelarOrden(@PathVariable Long id) {
        OrdenMantenimiento ordenCancelada = service.cancelarOrden(id);
        return ResponseEntity.ok(ordenCancelada);
    }
}
