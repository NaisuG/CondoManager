package com.condominio.contabilidad.DTO;

import lombok.Data;

@Data
public class GenerarCobroRequestDTO {
    private Long idUnidad;
    private Long idTipoUnidad;
    private Integer mes;
    private Integer anio;
}