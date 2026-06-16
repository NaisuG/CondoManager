package condominio.registro.service;

import condominio.registro.dto.UnidadDTO;
import condominio.registro.dto.UnidadDetalleCompletoDTO;
import condominio.registro.dto.UnidadRequestDTO;
import condominio.registro.model.ResidenteUnidad;
import condominio.registro.model.TipoUnidad;
import condominio.registro.model.Torre;
import condominio.registro.model.Unidad;
import condominio.registro.repository.ResidenteUnidadRepository;
import condominio.registro.repository.TipoUnidadRepository;
import condominio.registro.repository.TorreRepository;
import condominio.registro.repository.UnidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UnidadService {

    private final UnidadRepository unidadRepository;
    private final TorreRepository torreRepository;
    private final TipoUnidadRepository tipoUnidadRepository;
    private final ResidenteUnidadRepository residenteUnidadRepository;

    public List<UnidadDTO> listarTodas() {
        return unidadRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UnidadDTO> listarPorTorre(Long torreId) {
        return unidadRepository.findByTorreId(torreId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UnidadDTO obtenerPorId(Long id) {
        return toDTO(unidadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Unidad no encontrada con id: " + id)));
    }

    public UnidadDTO crear(UnidadRequestDTO request) {
        Torre torre = torreRepository.findById(request.getTorreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Torre no encontrada con id: " + request.getTorreId()));

        TipoUnidad tipo = tipoUnidadRepository.findById(request.getTipoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tipo de unidad no encontrado con id: " + request.getTipoId()));

        Unidad unidad = Unidad.builder()
                .torre(torre)
                .numero(request.getNumero())
                .tipo(tipo)
                .m2(request.getM2())
                .build();

        return toDTO(unidadRepository.save(unidad));
    }

    public UnidadDTO actualizar(Long id, UnidadRequestDTO request) {
        Unidad unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Unidad no encontrada con id: " + id));

        Torre torre = torreRepository.findById(request.getTorreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Torre no encontrada con id: " + request.getTorreId()));

        TipoUnidad tipo = tipoUnidadRepository.findById(request.getTipoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tipo de unidad no encontrado con id: " + request.getTipoId()));

        unidad.setTorre(torre);
        unidad.setNumero(request.getNumero());
        unidad.setTipo(tipo);
        unidad.setM2(request.getM2());

        return toDTO(unidadRepository.save(unidad));
    }

    public void eliminar(Long id) {
        if (!unidadRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Unidad no encontrada con id: " + id);
        }
        unidadRepository.deleteById(id);
    }

    private UnidadDTO toDTO(Unidad u) {
        return UnidadDTO.builder()
                .id(u.getId())
                .numero(u.getNumero())
                .tipoId(u.getTipo().getId())
                .tipoNombre(u.getTipo().getNombre())
                .m2(u.getM2())
                .build();
    }

    public Map<String, Long> obtenerEstadisticasUnidades() {
        List<Object[]> resultados = unidadRepository.contarUnidadesPorTipo();
        Map<String, Long> estadisticas = new HashMap<>();
        
        for (Object[] fila : resultados) {
            estadisticas.put(fila[0].toString(), (Long) fila[1]);
        }
        return estadisticas;
    }

    public UnidadDetalleCompletoDTO obtenerDetalleCompleto(Long id) {
        Unidad unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Unidad no encontrada con id: " + id));

        String nombreInquilino = "Sin asignar";
        
        List<ResidenteUnidad> relaciones = residenteUnidadRepository.findByUnidadId(id);
        if (relaciones != null && !relaciones.isEmpty()) {
            nombreInquilino = relaciones.get(0).getResidente().getNombre();
        }

        return UnidadDetalleCompletoDTO.builder()
                .numeroUnidad(unidad.getNumero())
                .tipoUnidad(unidad.getTipo().getNombre())
                .numeroTorre(unidad.getTorre().getNumero())
                .nombreCondominio(unidad.getTorre().getCondominio().getNombre())
                .nombreResidente(nombreInquilino)
                .build();
    }
}