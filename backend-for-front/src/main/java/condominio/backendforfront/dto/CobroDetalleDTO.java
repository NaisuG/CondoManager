package condominio.backendforfront.dto;

import lombok.Data;

@Data
public class CobroDetalleDTO {
    // Datos de Contabilidad
    private Long idCobro;
    private Integer mes;
    private Integer anio;
    private Integer monto;
    private String estado;
    private Long idCondominio;

    // Datos de Registro
    private String nombreCondominio;
    private Integer numeroTorre;
    private Integer numeroUnidad;
    private String tipoUnidad;
    private String nombreInquilino;
}
