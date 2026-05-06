package condominio.backendforfront.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class OrdenResumenDTO {
    private Long idOrden;
    private String descripcion;
    private String estado;
    private LocalDate fechaProgramada;
    private String nombreEmpresa; 
}