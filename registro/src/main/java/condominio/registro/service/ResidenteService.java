package condominio.registro.service;

import condominio.registro.dto.ResidenteDTO;
import condominio.registro.dto.ResidenteRequestDTO;
import condominio.registro.model.Residente;
import condominio.registro.repository.ResidenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResidenteService {

    private final ResidenteRepository residenteRepository;

    public List<ResidenteDTO> listarTodos() {
        return residenteRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ResidenteDTO obtenerPorId(Long id) {
        Residente residente = residenteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Residente no encontrado con id: " + id));
        return toDTO(residente);
    }

    public ResidenteDTO obtenerPorRun(String run) {
        Residente residente = residenteRepository.findByRun(run)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Residente no encontrado con RUN: " + run));
        return toDTO(residente);
    }

    public ResidenteDTO crear(ResidenteRequestDTO request) {
        if (residenteRepository.existsByRun(request.getRun())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un residente con el RUN: " + request.getRun());
        }
        if (residenteRepository.existsByCorreo(request.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un residente con el correo: " + request.getCorreo());
        }

        // Patrón Builder aplicado explícitamente
        Residente residente = Residente.builder()
                .run(request.getRun())
                .nombre(request.getNombre())
                .correo(request.getCorreo())
                .build();

        return toDTO(residenteRepository.save(residente));
    }

    public ResidenteDTO actualizar(Long id, ResidenteRequestDTO request) {
        Residente residente = residenteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Residente no encontrado con id: " + id));

        residente.setNombre(request.getNombre());
        residente.setCorreo(request.getCorreo());
        residente.setRun(request.getRun());

        return toDTO(residenteRepository.save(residente));
    }

    public void eliminar(Long id) {
        if (!residenteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Residente no encontrado con id: " + id);
        }
        residenteRepository.deleteById(id);
    }

    // Patrón DTO: conversión entidad → DTO (nunca expone la entidad directamente)
    private ResidenteDTO toDTO(Residente residente) {
        return ResidenteDTO.builder()
                .id(residente.getId())
                .run(residente.getRun())
                .nombre(residente.getNombre())
                .correo(residente.getCorreo())
                .build();
    }
}