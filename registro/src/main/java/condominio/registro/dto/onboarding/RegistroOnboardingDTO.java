package condominio.registro.dto.onboarding;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegistroOnboardingDTO {
    private Long idUsuario;
    private String nombre;
    private String direccion;
    private List<String> tiposUnidad;
    private List<TorreOnboardingDTO> torres;
}