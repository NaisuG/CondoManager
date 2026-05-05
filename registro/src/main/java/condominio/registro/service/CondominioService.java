package condominio.registro.service;

import condominio.registro.dto.CondominioDTO;
import condominio.registro.dto.CondominioDetalleDTO;
import condominio.registro.dto.CondominioRequestDTO;
import condominio.registro.dto.TorreDTO;
import condominio.registro.dto.UnidadDTO;
import condominio.registro.model.Condominio;
import condominio.registro.repository.CondominioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CondominioService {

    private final CondominioRepository condominioRepository;

    public List<CondominioDTO> listarTodos() {
        return condominioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CondominioDetalleDTO obtenerDetallePorId(Long id) {
        Condominio c = condominioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Condominio no encontrado con id: " + id));
        return toDetalleDTO(c);
    }

    public CondominioDTO crear(CondominioRequestDTO request) {
        if (condominioRepository.existsByNombre(request.getNombre())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un condominio con el nombre: " + request.getNombre());
        }
        Condominio condominio = Condominio.builder()
                .nombre(request.getNombre())
                .direccion(request.getDireccion())
                .build();
        return toDTO(condominioRepository.save(condominio));
    }

    public CondominioDTO actualizar(Long id, CondominioRequestDTO request) {
        Condominio condominio = condominioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Condominio no encontrado con id: " + id));
        condominio.setNombre(request.getNombre());
        condominio.setDireccion(request.getDireccion());
        return toDTO(condominioRepository.save(condominio));
    }

    public void eliminar(Long id) {
        if (!condominioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Condominio no encontrado con id: " + id);
        }
        condominioRepository.deleteById(id);
    }

    private CondominioDTO toDTO(Condominio c) {
        return CondominioDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .direccion(c.getDireccion())
                .build();
    }

    private CondominioDetalleDTO toDetalleDTO(Condominio c) {
        List<TorreDTO> torres = c.getTorres() == null ? List.of() :
                c.getTorres().stream().map(t -> TorreDTO.builder()
                        .id(t.getId())
                        .numero(t.getNumero())
                        .unidades(t.getUnidades() == null ? List.of() :
                                t.getUnidades().stream().map(u -> UnidadDTO.builder()
                                        .id(u.getId())
                                        .numero(u.getNumero())
                                        .tipoNombre(u.getTipo().getNombre())
                                        .m2(u.getM2())
                                        .build()).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList());

        return CondominioDetalleDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .direccion(c.getDireccion())
                .torres(torres)
                .build();
    }
}