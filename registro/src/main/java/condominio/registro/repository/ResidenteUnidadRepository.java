package condominio.registro.repository;

import condominio.registro.model.ResidenteUnidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidenteUnidadRepository extends JpaRepository<ResidenteUnidad, Long> {
    List<ResidenteUnidad> findByResidenteId(Long residenteId);
    List<ResidenteUnidad> findByUnidadId(Long unidadId);
    boolean existsByResidenteIdAndUnidadId(Long residenteId, Long unidadId);
}