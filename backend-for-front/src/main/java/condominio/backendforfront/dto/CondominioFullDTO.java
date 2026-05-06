package condominio.backendforfront.dto;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CondominioFullDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private List<TorreDTO> torres;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TorreDTO {
        private Long id;
        private Long condominioId;
        private Integer numero;
        private List<UnidadDTO> unidades;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UnidadDTO {
        private Long id;
        private Integer numero;
        private Double m2;
        private String tipoNombre;
    }
}