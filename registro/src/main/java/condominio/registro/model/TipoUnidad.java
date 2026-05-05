package condominio.registro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TIPO_UNIDAD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoUnidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;
}