package condominio.auth.DTO;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroRequestDTO {
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String rol;

}
