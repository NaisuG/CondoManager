package condominio.registro.dto.onboarding;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UnidadOnboardingDTO {
    private Integer numero;
    private String tipoNombre;
    private Double m2;
}
