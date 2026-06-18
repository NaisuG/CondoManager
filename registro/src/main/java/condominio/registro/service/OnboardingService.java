package condominio.registro.service;

import condominio.registro.dto.onboarding.*;
import condominio.registro.model.*;
import condominio.registro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final CondominioRepository condominioRepository;
    private final TipoUnidadRepository tipoUnidadRepository;
    private final TorreRepository torreRepository;
    private final UnidadRepository unidadRepository;

    @Transactional
    public Map<String, Object> registrarCondominioCompleto(RegistroOnboardingDTO request) {
        // 1. Crear Condominio
        Condominio condominio = Condominio.builder()
                .idUsuario(request.getIdUsuario())
                .nombre(request.getNombre())
                .direccion(request.getDireccion())
                .build();
        condominio = condominioRepository.save(condominio);

        // 2. Cargar o Crear Tipos de Unidad (En un Map para acceso rápido)
        Map<String, TipoUnidad> mapaTipos = new HashMap<>();
        if (request.getTiposUnidad() != null) {
            for (String nombreTipo : request.getTiposUnidad()) {
                String nombreLimpio = nombreTipo.toUpperCase().trim();
                TipoUnidad tipo = tipoUnidadRepository.findByNombre(nombreLimpio)
                        .orElseGet(() -> tipoUnidadRepository.save(TipoUnidad.builder().nombre(nombreLimpio).build()));
                mapaTipos.put(nombreLimpio, tipo);
            }
        }

        // 3. Iterar Torres y Unidades
        if (request.getTorres() != null) {
            for (TorreOnboardingDTO torreDto : request.getTorres()) {
                Torre torre = Torre.builder()
                        .condominio(condominio)
                        .numero(torreDto.getNumero())
                        .build();
                torre = torreRepository.save(torre);

                if (torreDto.getUnidades() != null) {
                    for (UnidadOnboardingDTO unidadDto : torreDto.getUnidades()) {
                        TipoUnidad tipoUnidad = mapaTipos.get(unidadDto.getTipoNombre().toUpperCase().trim());
                        if (tipoUnidad == null) {
                            throw new RuntimeException("El tipo de unidad '" + unidadDto.getTipoNombre() + "' no fue declarado.");
                        }

                        Unidad unidad = Unidad.builder()
                                .torre(torre)
                                .numero(unidadDto.getNumero())
                                .tipo(tipoUnidad)
                                .m2(unidadDto.getM2())
                                .build();
                        unidadRepository.save(unidad);
                    }
                }
            }
        }

        return Map.of(
                "mensaje", "Copropiedad " + condominio.getNombre() + " inicializada exitosamente.",
                "idCondominio", condominio.getId()
        );
    }
}