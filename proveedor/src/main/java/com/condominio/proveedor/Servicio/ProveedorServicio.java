package com.condominio.proveedor.Servicio;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.condominio.proveedor.Modelo.Proveedor;
import com.condominio.proveedor.Repositorio.ProveedorRepositorio;

@Service
public class ProveedorServicio {
    @Autowired
    private ProveedorRepositorio repository;

    public Proveedor guardar(Proveedor p) { return repository.save(p); }
    public List<Proveedor> listar() { return repository.findAll(); }
    public Proveedor buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
    }
}
