package com.condominio.mantenimiento.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import com.condominio.mantenimiento.Modelo.OrdenMantenimiento;

public interface OrdenMantenimientoRepository extends JpaRepository<OrdenMantenimiento, Long> {

}
