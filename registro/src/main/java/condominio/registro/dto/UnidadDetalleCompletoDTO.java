package condominio.registro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadDetalleCompletoDTO {
    private Integer numeroUnidad;
    private String tipoUnidad;
    private Integer numeroTorre;
    private String nombreCondominio;
    private String nombreResidente;
}
