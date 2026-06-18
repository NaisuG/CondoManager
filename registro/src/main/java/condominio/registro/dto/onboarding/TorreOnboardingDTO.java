package condominio.registro.dto.onboarding;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TorreOnboardingDTO {
    private Integer numero;
    private List<UnidadOnboardingDTO> unidades;
}