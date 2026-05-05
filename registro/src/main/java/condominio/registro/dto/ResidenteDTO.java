package condominio.registro.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidenteDTO {
    private Long id;
    private String run;
    private String nombre;
    private String correo;
}