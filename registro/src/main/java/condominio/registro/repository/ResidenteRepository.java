package condominio.registro.repository;

import condominio.registro.model.Residente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResidenteRepository extends JpaRepository<Residente, Long> {
    Optional<Residente> findByRun(String run);
    Optional<Residente> findByCorreo(String correo);
    boolean existsByRun(String run);
    boolean existsByCorreo(String correo);
}