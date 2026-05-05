package condominio.registro.service;

import condominio.registro.dto.ResidenteUnidadDTO;
import condominio.registro.dto.ResidenteUnidadRequestDTO;
import condominio.registro.model.Residente;
import condominio.registro.model.ResidenteUnidad;
import condominio.registro.model.Unidad;
import condominio.registro.repository.ResidenteRepository;
import condominio.registro.repository.ResidenteUnidadRepository;
import condominio.registro.repository.UnidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResidenteUnidadService {

    private final ResidenteUnidadRepository residenteUnidadRepository;
    private final ResidenteRepository residenteRepository;
    private final UnidadRepository unidadRepository;

    public List<ResidenteUnidadDTO> listarPorResidente(Long residenteId) {
        return residenteUnidadRepository.findByResidenteId(residenteId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ResidenteUnidadDTO> listarPorUnidad(Long unidadId) {
        return residenteUnidadRepository.findByUnidadId(unidadId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ResidenteUnidadDTO asignar(ResidenteUnidadRequestDTO request) {
        if (residenteUnidadRepository.existsByResidenteIdAndUnidadId(
                request.getResidenteId(), request.getUnidadId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El residente ya está asignado a esa unidad");
        }

        Residente residente = residenteRepository.findById(request.getResidenteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Residente no encontrado con id: " + request.getResidenteId()));

        Unidad unidad = unidadRepository.findById(request.getUnidadId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Unidad no encontrada con id: " + request.getUnidadId()));

        ResidenteUnidad ru = ResidenteUnidad.builder()
                .residente(residente)
                .unidad(unidad)
                .arrienda(request.isArrienda())
                .build();

        return toDTO(residenteUnidadRepository.save(ru));
    }

    public void eliminar(Long id) {
        if (!residenteUnidadRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Asignación no encontrada con id: " + id);
        }
        residenteUnidadRepository.deleteById(id);
    }

    private ResidenteUnidadDTO toDTO(ResidenteUnidad ru) {
        return ResidenteUnidadDTO.builder()
                .id(ru.getId())
                .residenteId(ru.getResidente().getId())
                .residenteNombre(ru.getResidente().getNombre())
                .unidadId(ru.getUnidad().getId())
                .unidadNumero(ru.getUnidad().getNumero())
                .arrienda(ru.isArrienda())
                .build();
    }
}