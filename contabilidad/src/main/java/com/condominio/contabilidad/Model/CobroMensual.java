package com.condominio.contabilidad.Model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "COBRO_MENSUAL")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CobroMensual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_unidad", nullable = false)
    private Long idUnidad;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer anio;

    @Column(name = "monto_cobrado", nullable = false)
    private Integer montoCobrado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCobro estado;

    @Column(name = "id_documento", nullable = true)
    private Long idDocumento;
}
