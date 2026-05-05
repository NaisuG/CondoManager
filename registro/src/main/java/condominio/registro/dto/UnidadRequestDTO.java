package condominio.registro.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadRequestDTO {
    private Long torreId;
    private Integer numero;
    private Long tipoId;
    private Double m2;
}