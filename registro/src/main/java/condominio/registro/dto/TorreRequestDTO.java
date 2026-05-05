package condominio.registro.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TorreRequestDTO {
    private Long condominioId;
    private Integer numero;
}