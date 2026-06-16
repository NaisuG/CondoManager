package condominio.auth.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ROL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "NOMBRE_ROL", nullable = false, unique = true)
    private NombreRol nombreRol;

    public enum NombreRol {
        ROL_ADMIN,
        ROL_USER
    }
}
