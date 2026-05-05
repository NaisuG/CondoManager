package condominio.backendforfront.dto;

import lombok.Data;
import java.util.List;

@Data
public class CondominioFullDTO {
    private String nombre;
    private String direccion;
    private List<TorreDTO> torres;

    @Data
    public static class TorreDTO {
        private Integer numero;
        private List<UnidadDTO> unidades;
    }

    @Data
    public static class UnidadDTO {
        private Integer numero;
        private Double m2;
        private String tipo;
    }
}