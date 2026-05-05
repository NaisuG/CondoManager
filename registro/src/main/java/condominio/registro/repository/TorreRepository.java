package condominio.registro.repository;

import condominio.registro.model.Torre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TorreRepository extends JpaRepository<Torre, Long> {
    List<Torre> findByCondominioId(Long condominioId);
}