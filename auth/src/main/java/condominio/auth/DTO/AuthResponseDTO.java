package condominio.auth.DTO;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private Long idUsuario;
    private String token;
    private String email;
    private String nombre;
    private String apellido;
    private String rol;
}
