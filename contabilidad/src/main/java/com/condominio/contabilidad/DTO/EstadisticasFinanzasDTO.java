package com.condominio.contabilidad.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstadisticasFinanzasDTO {
    private Integer totalProyectado; 
    private Integer totalRecaudado;  
    private Double tasaMorosidad;   
    private Long cantidadPagados;
    private Long cantidadPendientes;

}
