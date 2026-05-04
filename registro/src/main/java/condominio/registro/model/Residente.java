package condominio.registro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name="Residentes")

public class Residente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column (nullable=false)
    private String nombre;
    @Column (nullable=false)
    private int run;
    @Column (nullable=false)
    private String cod_verificador;
    @Column (nullable=false)
    private String email;

}