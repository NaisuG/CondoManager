package condominio.registro.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidenteRequestDTO {
    private String run;
    private String nombre;
    private String correo;
}