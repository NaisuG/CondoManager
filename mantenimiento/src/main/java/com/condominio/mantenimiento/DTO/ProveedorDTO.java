package com.condominio.mantenimiento.DTO;

import lombok.Data;

@Data
public class ProveedorDTO {
    private Long id;
    private String nombreEmpresa;
    private String rut;
    private String correo;
    private String fono;
}
