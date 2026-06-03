package com.condominio.contabilidad.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.condominio.contabilidad.Model.CobroMensual;
import com.condominio.contabilidad.Model.EstadoCobro;

public interface CobroMensualRepository extends JpaRepository<CobroMensual, Long> {
    List<CobroMensual> findByMesAndAnio(Integer mes, Integer anio);
    boolean existsByIdUnidadAndMesAndAnio(Long idUnidad, Integer mes, Integer anio);
    // Suma cobros de un mes (Proyección)
    @Query("SELECT COALESCE(SUM(c.montoCobrado), 0) FROM CobroMensual c WHERE c.mes = :mes AND c.anio = :anio")
    Integer sumarProyeccionMes(@Param("mes") Integer mes, @Param("anio") Integer anio);

    // Suma pagados de un mes (Recaudación)
    @Query("SELECT COALESCE(SUM(c.montoCobrado), 0) FROM CobroMensual c WHERE c.mes = :mes AND c.anio = :anio AND c.estado = 'PAGADO'")
    Integer sumarRecaudacionMes(@Param("mes") Integer mes, @Param("anio") Integer anio);

    // Cobros en un estado específico en un mes
    @Query("SELECT COUNT(c) FROM CobroMensual c WHERE c.mes = :mes AND c.anio = :anio AND c.estado = :estado")
    Long contarPorEstadoYMes(@Param("mes") Integer mes, @Param("anio") Integer anio, @Param("estado") EstadoCobro estado);
}
