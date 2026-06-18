package condominio.registro;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import condominio.registro.dto.onboarding.RegistroOnboardingDTO;
import condominio.registro.dto.onboarding.TorreOnboardingDTO;
import condominio.registro.dto.onboarding.UnidadOnboardingDTO;
import condominio.registro.model.Condominio;
import condominio.registro.model.TipoUnidad;
import condominio.registro.model.Torre;
import condominio.registro.model.Unidad;
import condominio.registro.repository.CondominioRepository;
import condominio.registro.repository.TipoUnidadRepository;
import condominio.registro.repository.TorreRepository;
import condominio.registro.repository.UnidadRepository;
import condominio.registro.service.OnboardingService;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private CondominioRepository condominioRepository;
    @Mock
    private TipoUnidadRepository tipoUnidadRepository;
    @Mock
    private TorreRepository torreRepository;
    @Mock
    private UnidadRepository unidadRepository;

    @InjectMocks
    private OnboardingService onboardingService;

    @Test
    void registrarCondominioCompleto_Exito() {
        // --- ARRANGE ---
        UnidadOnboardingDTO unidadDto = new UnidadOnboardingDTO();
        unidadDto.setNumero(101);
        unidadDto.setTipoNombre("DEPARTAMENTO");
        unidadDto.setM2(50.5);

        TorreOnboardingDTO torreDto = new TorreOnboardingDTO();
        torreDto.setNumero(1);
        torreDto.setUnidades(List.of(unidadDto));

        RegistroOnboardingDTO request = new RegistroOnboardingDTO();
        request.setIdUsuario(1L);
        request.setNombre("Edificio Central");
        request.setDireccion("Av. Providencia 123");
        request.setTiposUnidad(List.of("DEPARTAMENTO"));
        request.setTorres(List.of(torreDto));

        Condominio condominioGuardado = Condominio.builder().id(10L).nombre("Edificio Central").build();
        when(condominioRepository.save(any(Condominio.class))).thenReturn(condominioGuardado);

        when(tipoUnidadRepository.findByNombre("DEPARTAMENTO")).thenReturn(Optional.empty());
        
        TipoUnidad tipoUnidadGuardado = TipoUnidad.builder().id(1L).nombre("DEPARTAMENTO").build();
        when(tipoUnidadRepository.save(any(TipoUnidad.class))).thenReturn(tipoUnidadGuardado);

        Torre torreGuardada = Torre.builder().id(1L).numero(1).condominio(condominioGuardado).build();
        when(torreRepository.save(any(Torre.class))).thenReturn(torreGuardada);

        // --- ACT ---
        Map<String, Object> respuesta = onboardingService.registrarCondominioCompleto(request);

        // --- ASSERT ---
        assertNotNull(respuesta, "La respuesta no debe ser nula");
        assertEquals(10L, respuesta.get("idCondominio"), "El ID del condominio devuelto debe ser 10");
        assertTrue(respuesta.get("mensaje").toString().contains("Edificio Central"), "El mensaje debe contener el nombre");

        verify(condominioRepository, times(1)).save(any(Condominio.class));
        verify(tipoUnidadRepository, times(1)).save(any(TipoUnidad.class));
        verify(torreRepository, times(1)).save(any(Torre.class));
        verify(unidadRepository, times(1)).save(any(Unidad.class));
    }

    @Test
    void registrarCondominioCompleto_FallaPorTipoNoDeclarado() {
        // --- ARRANGE ---
        UnidadOnboardingDTO unidadDto = new UnidadOnboardingDTO();
        unidadDto.setNumero(102);
        unidadDto.setTipoNombre("ESTACIONAMIENTO");

        TorreOnboardingDTO torreDto = new TorreOnboardingDTO();
        torreDto.setNumero(1);
        torreDto.setUnidades(List.of(unidadDto));

        RegistroOnboardingDTO request = new RegistroOnboardingDTO();
        request.setIdUsuario(1L);
        request.setNombre("Edificio Bug");
        request.setTiposUnidad(List.of("DEPARTAMENTO")); 
        request.setTorres(List.of(torreDto));

        Condominio condominioGuardado = Condominio.builder().id(11L).nombre("Edificio Bug").build();
        when(condominioRepository.save(any(Condominio.class))).thenReturn(condominioGuardado);

        TipoUnidad tipoDepto = TipoUnidad.builder().id(1L).nombre("DEPARTAMENTO").build();
        when(tipoUnidadRepository.findByNombre("DEPARTAMENTO")).thenReturn(Optional.of(tipoDepto));
        
        Torre torreGuardada = Torre.builder().id(1L).numero(1).condominio(condominioGuardado).build();
        when(torreRepository.save(any(Torre.class))).thenReturn(torreGuardada);

        // --- ACT & ASSERT ---
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            onboardingService.registrarCondominioCompleto(request);
        });

        assertEquals("El tipo de unidad 'ESTACIONAMIENTO' no fue declarado.", excepcion.getMessage());

        verify(unidadRepository, never()).save(any(Unidad.class));
    }
}