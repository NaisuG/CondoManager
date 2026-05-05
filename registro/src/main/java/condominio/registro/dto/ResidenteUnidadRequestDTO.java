package condominio.registro.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidenteUnidadRequestDTO {
    private Long residenteId;
    private Long unidadId;
    private boolean arrienda;
}