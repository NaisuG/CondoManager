package condominio.registro.service;

import condominio.registro.dto.TorreDTO;
import condominio.registro.dto.TorreRequestDTO;
import condominio.registro.model.Condominio;
import condominio.registro.model.Torre;
import condominio.registro.repository.CondominioRepository;
import condominio.registro.dto.UnidadDTO;
import condominio.registro.repository.TorreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TorreService {

    private final TorreRepository torreRepository;
    private final CondominioRepository condominioRepository;

    public List<TorreDTO> listarTodas() {
        return torreRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TorreDTO> listarPorCondominio(Long condominioId) {
        return torreRepository.findByCondominioId(condominioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TorreDTO obtenerPorId(Long id) {
        return toDTO(torreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Torre no encontrada con id: " + id)));
    }

    public TorreDTO crear(TorreRequestDTO request) {
        Condominio condominio = condominioRepository.findById(request.getCondominioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Condominio no encontrado con id: " + request.getCondominioId()));

        Torre torre = Torre.builder()
                .condominio(condominio)
                .numero(request.getNumero())
                .build();

        return toDTO(torreRepository.save(torre));
    }

    public TorreDTO actualizar(Long id, TorreRequestDTO request) {
        Torre torre = torreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Torre no encontrada con id: " + id));

        Condominio condominio = condominioRepository.findById(request.getCondominioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Condominio no encontrado con id: " + request.getCondominioId()));

        torre.setCondominio(condominio);
        torre.setNumero(request.getNumero());

        return toDTO(torreRepository.save(torre));
    }

    public void eliminar(Long id) {
        if (!torreRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Torre no encontrada con id: " + id);
        }
        torreRepository.deleteById(id);
    }

    private TorreDTO toDTO(Torre t) {
        return TorreDTO.builder()
                .id(t.getId())
                .condominioId(t.getCondominio().getId())
                .numero(t.getNumero())
                .unidades(t.getUnidades() == null ? List.of() :
                        t.getUnidades().stream().map(u -> UnidadDTO.builder()
                                .id(u.getId())
                                .numero(u.getNumero())
                                .tipoNombre(u.getTipo().getNombre())
                                .m2(u.getM2())
                                .build()).collect(Collectors.toList()))
                .build();
    }
}