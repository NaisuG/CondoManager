package com.condominio.contabilidad.Repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.condominio.contabilidad.Model.ValorGastoComun;

public interface ValorGastoComunRepository extends JpaRepository<ValorGastoComun, Long> {
    Optional<ValorGastoComun> findByIdTipoUnidad(Long idTipoUnidad);
}