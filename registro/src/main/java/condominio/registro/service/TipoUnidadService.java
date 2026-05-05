package condominio.registro.service;

import condominio.registro.dto.TipoUnidadDTO;
import condominio.registro.dto.TipoUnidadRequestDTO;
import condominio.registro.model.TipoUnidad;
import condominio.registro.repository.TipoUnidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TipoUnidadService {

    private final TipoUnidadRepository tipoUnidadRepository;

    public List<TipoUnidadDTO> listarTodos() {
        return tipoUnidadRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TipoUnidadDTO obtenerPorId(Long id) {
        return toDTO(tipoUnidadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tipo de unidad no encontrado con id: " + id)));
    }

    public TipoUnidadDTO crear(TipoUnidadRequestDTO request) {
        if (tipoUnidadRepository.existsByNombre(request.getNombre())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un tipo de unidad con el nombre: " + request.getNombre());
        }
        TipoUnidad tipo = TipoUnidad.builder()
                .nombre(request.getNombre())
                .build();
        return toDTO(tipoUnidadRepository.save(tipo));
    }

    public TipoUnidadDTO actualizar(Long id, TipoUnidadRequestDTO request) {
        TipoUnidad tipo = tipoUnidadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tipo de unidad no encontrado con id: " + id));
        tipo.setNombre(request.getNombre());
        return toDTO(tipoUnidadRepository.save(tipo));
    }

    public void eliminar(Long id) {
        if (!tipoUnidadRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Tipo de unidad no encontrado con id: " + id);
        }
        tipoUnidadRepository.deleteById(id);
    }

    private TipoUnidadDTO toDTO(TipoUnidad t) {
        return TipoUnidadDTO.builder()
                .id(t.getId())
                .nombre(t.getNombre())
                .build();
    }
}