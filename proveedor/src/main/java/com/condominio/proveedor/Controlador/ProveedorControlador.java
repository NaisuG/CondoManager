package com.condominio.proveedor.Controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.condominio.proveedor.Modelo.Proveedor;
import com.condominio.proveedor.Servicio.ProveedorServicio;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorControlador {
    @Autowired
    private ProveedorServicio service;

    @PostMapping
    public Proveedor crear(@RequestBody Proveedor p) { return service.guardar(p); }

    @GetMapping
    public List<Proveedor> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Proveedor obtenerPorId(@PathVariable Long id) { return service.buscarPorId(id); }
}
