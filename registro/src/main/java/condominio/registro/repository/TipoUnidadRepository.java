package condominio.registro.repository;

import condominio.registro.model.TipoUnidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoUnidadRepository extends JpaRepository<TipoUnidad, Long> {
    Optional<TipoUnidad> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}