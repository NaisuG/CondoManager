package condominio.registro.repository;

import condominio.registro.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Long> {
    List<Unidad> findByTorreId(Long torreId);
    List<Unidad> findByTipoId(Long tipoId);
    @Query("SELECT u.tipo.nombre, COUNT(u) FROM Unidad u GROUP BY u.tipo.nombre")
    List<Object[]> contarUnidadesPorTipo();
}