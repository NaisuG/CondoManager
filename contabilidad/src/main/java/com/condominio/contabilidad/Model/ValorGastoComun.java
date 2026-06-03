package com.condominio.contabilidad.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "VALOR_GASTO_COMUN")
public class ValorGastoComun {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_tipo_unidad", nullable = false, unique = true)
    private Long idTipoUnidad;

    @Column(nullable = false)
    private Integer monto;
}
