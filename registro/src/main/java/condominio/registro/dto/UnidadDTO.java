package condominio.registro.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadDTO {
    private Long id;
    private Integer numero;
    private String tipoNombre;
    private Double m2;
}