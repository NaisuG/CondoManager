package condominio.registro.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidenteUnidadDTO {
    private Long id;
    private Long residenteId;
    private String residenteNombre;
    private Long unidadId;
    private Integer unidadNumero;
    private boolean arrienda;
}