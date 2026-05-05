package condominio.registro.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CondominioDetalleDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private List<TorreDTO> torres;
}