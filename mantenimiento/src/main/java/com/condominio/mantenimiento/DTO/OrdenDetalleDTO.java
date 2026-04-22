package com.condominio.mantenimiento.DTO;

import lombok.Data;
import com.condominio.mantenimiento.Modelo.OrdenMantenimiento;

@Data
public class OrdenDetalleDTO {
    private OrdenMantenimiento orden;
    private ProveedorDTO proveedor;
}