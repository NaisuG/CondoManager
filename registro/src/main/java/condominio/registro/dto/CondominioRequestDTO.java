package condominio.registro.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CondominioRequestDTO {
    private Long idUsuario;
    private String nombre;
    private String direccion;
}