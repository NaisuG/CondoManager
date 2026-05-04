package com.condominio.proveedor.Modelo;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "proveedor")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_empresa", nullable = false)
    private String nombreEmpresa;

    @Column(nullable = false, unique = true)
    private String rut;

    private String correo;
    private String fono;

    @ManyToOne
    @JoinColumn(name = "id_categoria_servicio")
    private CategoriaServicio categoria;

    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL)
    private List<Contrato> contratos;
}
