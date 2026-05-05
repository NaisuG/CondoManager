package condominio.registro.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CondominioDTO {
    private Long id;
    private String nombre;
    private String direccion;
}