package condominio.registro.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TorreDTO {
    private Long id;
    private Long condominioId;
    private Integer numero;
    private List<UnidadDTO> unidades;
}