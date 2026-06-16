package condominio.auth.config;

import condominio.auth.model.Rol;
import condominio.auth.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInit {

    private final RolRepository rolRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initRoles() {
        for (Rol.NombreRol nombreRol : Rol.NombreRol.values()) {
            rolRepository.findByNombreRol(nombreRol)
                    .orElseGet(() -> rolRepository.save(
                            Rol.builder().nombreRol(nombreRol).build()
                    ));
        }
    }
}
