package condominio.registro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "UNIDAD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TORRE", nullable = false)
    private Torre torre;

    @Column(nullable = false)
    private Integer numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO", nullable = false)
    private TipoUnidad tipo;

    @Column(nullable = false)
    private Double m2;
}