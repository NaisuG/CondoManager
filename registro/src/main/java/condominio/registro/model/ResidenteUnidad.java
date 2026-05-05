package condominio.registro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RESIDENTE_UNIDAD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidenteUnidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RESIDENTE", nullable = false)
    private Residente residente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNIDAD", nullable = false)
    private Unidad unidad;

    @Column(name = "ARRIENDA", nullable = false)
    private boolean arrienda;
}