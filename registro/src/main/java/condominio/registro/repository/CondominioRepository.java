package condominio.registro.repository;

import condominio.registro.model.Condominio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CondominioRepository extends JpaRepository<Condominio, Long> {
    Optional<Condominio> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
    List<Condominio> findByIdUsuario(Long idUsuario);
}