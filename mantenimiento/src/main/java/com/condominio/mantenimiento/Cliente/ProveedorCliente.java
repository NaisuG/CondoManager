package com.condominio.mantenimiento.Cliente;

import com.condominio.mantenimiento.DTO.ProveedorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "proveedor-service", url = "http://localhost:8081/api/proveedores")
public interface ProveedorCliente {

    @GetMapping("/{id}")
    public ProveedorDTO obtenerProveedorPorId(@PathVariable("id") Long id);
}