package com.condominio.contabilidad.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.condominio.contabilidad.DTO.EstadisticasFinanzasDTO;
import com.condominio.contabilidad.DTO.GenerarCobroRequestDTO;
import com.condominio.contabilidad.Model.CobroMensual;
import com.condominio.contabilidad.Model.EstadoCobro;
import com.condominio.contabilidad.Repository.CobroMensualRepository;
import com.condominio.contabilidad.Repository.ValorGastoComunRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContabilidadService {

    private final CobroMensualRepository cobroMensualRepository;
    
    private final ValorGastoComunRepository valorGastoComunRepository;

    public CobroMensual generarCobroMensual(Long idUnidad, Long idTipoUnidad, Integer mes, Integer anio) {
        
        if (cobroMensualRepository.existsByIdUnidadAndMesAndAnio(idUnidad, mes, anio)) {
            throw new RuntimeException("Error: Ya existe un cobro para la unidad " + idUnidad + " en el periodo " + mes + "/" + anio);
        }

        var tarifa = valorGastoComunRepository.findByIdTipoUnidad(idTipoUnidad)
                .orElseThrow(() -> new RuntimeException("No hay tarifa configurada para este tipo de unidad"));

        CobroMensual nuevoCobro = CobroMensual.builder()
                .idUnidad(idUnidad)
                .mes(mes)
                .anio(anio)
                .montoCobrado(tarifa.getMonto())
                .estado(EstadoCobro.PENDIENTE)
                .build();

        return cobroMensualRepository.save(nuevoCobro);
    }

    public com.condominio.contabilidad.Model.ValorGastoComun configurarTarifa(Long idTipoUnidad, Integer monto) {
        com.condominio.contabilidad.Model.ValorGastoComun tarifa = valorGastoComunRepository.findByIdTipoUnidad(idTipoUnidad)
                .orElse(new com.condominio.contabilidad.Model.ValorGastoComun(null, idTipoUnidad, 0));
        
        tarifa.setMonto(monto);
        return valorGastoComunRepository.save(tarifa);
    }

    // Cambiar estado de un cobro (El Checkbox del Admin)
    public CobroMensual cambiarEstadoPago(Long idCobro, EstadoCobro nuevoEstado) {
        CobroMensual cobro = cobroMensualRepository.findById(idCobro)
                .orElseThrow(() -> new RuntimeException("Cobro no encontrado"));
        cobro.setEstado(nuevoEstado);
        return cobroMensualRepository.save(cobro);
    }

    public List<CobroMensual> listarCobrosMes(Integer mes, Integer anio) {
        return cobroMensualRepository.findByMesAndAnio(mes, anio);
    }

    public EstadisticasFinanzasDTO obtenerEstadisticasMes(Integer mes, Integer anio) {
        Integer proyectado = cobroMensualRepository.sumarProyeccionMes(mes, anio);
        Integer recaudado = cobroMensualRepository.sumarRecaudacionMes(mes, anio);
        
        Long pagados = cobroMensualRepository.contarPorEstadoYMes(mes, anio, EstadoCobro.PAGADO);
        Long pendientes = cobroMensualRepository.contarPorEstadoYMes(mes, anio, EstadoCobro.PENDIENTE);
        Long vencidos = cobroMensualRepository.contarPorEstadoYMes(mes, anio, EstadoCobro.VENCIDO);

        Long totalImpagos = pendientes + vencidos;
        
        // Calcular tasa de morosidad (Monto Impago / Monto Total) * 100
        double tasaMorosidad = 0.0;
        if (proyectado > 0) {
            tasaMorosidad = ((double) (proyectado - recaudado) / proyectado) * 100;
        }

        return EstadisticasFinanzasDTO.builder()
                .totalProyectado(proyectado)
                .totalRecaudado(recaudado)
                .cantidadPagados(pagados)
                .cantidadPendientes(totalImpagos)
                .tasaMorosidad(Math.round(tasaMorosidad * 100.0) / 100.0)
                .build();
    }

    public List<CobroMensual> generarCobrosMasivos(List<GenerarCobroRequestDTO> peticiones) {
        List<CobroMensual> nuevosCobros = new ArrayList<>();

        for (GenerarCobroRequestDTO peticion : peticiones) {
            // Validamos que no exista para no cobrar el doble
            if (!cobroMensualRepository.existsByIdUnidadAndMesAndAnio(peticion.getIdUnidad(), peticion.getMes(), peticion.getAnio())) {
                
                var tarifa = valorGastoComunRepository.findByIdTipoUnidad(peticion.getIdTipoUnidad())
                        .orElseThrow(() -> new RuntimeException("No hay tarifa para el tipo de unidad: " + peticion.getIdTipoUnidad()));

                CobroMensual nuevoCobro = CobroMensual.builder()
                        .idUnidad(peticion.getIdUnidad())
                        .mes(peticion.getMes())
                        .anio(peticion.getAnio())
                        .montoCobrado(tarifa.getMonto())
                        .estado(EstadoCobro.PENDIENTE)
                        .build();

                nuevosCobros.add(nuevoCobro);
            }
        }
        
        // Guardamos todos de un solo golpe
        return cobroMensualRepository.saveAll(nuevosCobros);
    }

    public List<com.condominio.contabilidad.Model.ValorGastoComun> listarTarifas() {
        return valorGastoComunRepository.findAll();
    }
}
