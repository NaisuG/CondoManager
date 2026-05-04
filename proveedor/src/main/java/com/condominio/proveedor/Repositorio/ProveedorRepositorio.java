package com.condominio.proveedor.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import com.condominio.proveedor.Modelo.Proveedor;

public interface ProveedorRepositorio extends JpaRepository<Proveedor, Long> {

}
