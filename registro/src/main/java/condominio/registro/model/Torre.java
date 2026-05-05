package condominio.registro.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "TORRE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Torre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONDOMINIO", nullable = false)
    private Condominio condominio;

    @Column(nullable = false)
    private Integer numero;

    @OneToMany(mappedBy = "torre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Unidad> unidades;
}