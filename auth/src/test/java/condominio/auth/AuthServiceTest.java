package condominio.auth;

import condominio.auth.service.AuthService;
import condominio.auth.service.JwtService;

import condominio.auth.DTO.AuthResponseDTO;
import condominio.auth.DTO.LoginRequestDTO;
import condominio.auth.DTO.RegistroRequestDTO;
import condominio.auth.model.Rol;
import condominio.auth.model.Usuario;
import condominio.auth.repository.RolRepository;
import condominio.auth.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - pruebas unitarias")
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private Rol rolUser;
    private Usuario usuarioExistente;

    @BeforeEach
    void setUp() {
        rolUser = Rol.builder().id(1L).nombreRol(Rol.NombreRol.ROL_USER).build();
        usuarioExistente = Usuario.builder()
                .id(10L)
                .nombre("Ana")
                .apellido("Soto")
                .email("ana@condominio.cl")
                .password("hash-encriptado")
                .rol(rolUser)
                .build();
    }

    // ---------------- LOGIN ----------------

    @Test
    @DisplayName("login: credenciales válidas retornan token y datos del usuario")
    void login_credencialesValidas_retornaTokenYDatos() {
        LoginRequestDTO request = new LoginRequestDTO("ana@condominio.cl", "1234", null);
        UserDetails userDetails = new User("ana@condominio.cl", "hash-encriptado",
                List.of(() -> "ROL_USER"));

        when(userDetailsService.loadUserByUsername("ana@condominio.cl")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("token-simulado-123");
        when(usuarioRepository.findByEmail("ana@condominio.cl")).thenReturn(Optional.of(usuarioExistente));

        AuthResponseDTO respuesta = authService.login(request);

        assertThat(respuesta.getIdUsuario()).isEqualTo(10L);
        assertThat(respuesta.getToken()).isEqualTo("token-simulado-123");
        assertThat(respuesta.getEmail()).isEqualTo("ana@condominio.cl");
        assertThat(respuesta.getRol()).isEqualTo("ROL_USER");

        // Verifica que efectivamente se delega la validación de credenciales a Spring Security
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("login: si las credenciales son inválidas, propaga la excepción de Spring Security y no genera token")
    void login_credencialesInvalidas_propagaExcepcion() {
        LoginRequestDTO request = new LoginRequestDTO("ana@condominio.cl", "clave-mala", null);
        doThrow(new BadCredentialsException("Credenciales inválidas"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("login: si el usuario autenticado no existe en la BD (caso borde), lanza 404")
    void login_usuarioNoEncontradoTrasAutenticar_lanza404() {
        LoginRequestDTO request = new LoginRequestDTO("fantasma@condominio.cl", "1234", null);
        UserDetails userDetails = new User("fantasma@condominio.cl", "x", List.of());

        when(userDetailsService.loadUserByUsername("fantasma@condominio.cl")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("token");
        when(usuarioRepository.findByEmail("fantasma@condominio.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ---------------- REGISTER ----------------

    @Test
    @DisplayName("register: email ya registrado retorna 409 CONFLICT y no crea usuario")
    void register_emailDuplicado_lanza409() {
        RegistroRequestDTO request = new RegistroRequestDTO(
                "Ana", "Soto", "ana@condominio.cl", "1234", "ROL_USER");
        when(usuarioRepository.existsByEmail("ana@condominio.cl")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("El email ya existe");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: rol inexistente en el enum retorna 400 BAD_REQUEST")
    void register_rolInvalido_lanza400() {
        RegistroRequestDTO request = new RegistroRequestDTO(
                "Ana", "Soto", "ana@condominio.cl", "1234", "ROL_SUPERADMIN_NO_EXISTE");
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Rol invalido");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: rol válido pero no cargado en la BD retorna 404")
    void register_rolNoEncontradoEnBD_lanza404() {
        RegistroRequestDTO request = new RegistroRequestDTO(
                "Ana", "Soto", "ana@condominio.cl", "1234", "ROL_ADMIN");
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(rolRepository.findByNombreRol(Rol.NombreRol.ROL_ADMIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Rol no encontrado");
    }

    @Test
    @DisplayName("register: datos válidos crean el usuario con password encriptada y retornan token")
    void register_datosValidos_creaUsuarioYRetornaToken() {
        RegistroRequestDTO request = new RegistroRequestDTO(
                "Juan", "Pérez", "juan@condominio.cl", "clave-plana", "ROL_USER");

        when(usuarioRepository.existsByEmail("juan@condominio.cl")).thenReturn(false);
        when(rolRepository.findByNombreRol(Rol.NombreRol.ROL_USER)).thenReturn(Optional.of(rolUser));
        when(passwordEncoder.encode("clave-plana")).thenReturn("clave-encriptada-bcrypt");
        // Simula el comportamiento real de JPA: al guardar, la entidad recibe su ID autogenerado
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(20L);
            return u;
        });
        UserDetails userDetails = new User("juan@condominio.cl", "clave-encriptada-bcrypt",
                List.of(() -> "ROL_USER"));
        when(userDetailsService.loadUserByUsername("juan@condominio.cl")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("token-nuevo-usuario");

        AuthResponseDTO respuesta = authService.register(request);

        assertThat(respuesta.getIdUsuario()).isEqualTo(20L);
        assertThat(respuesta.getEmail()).isEqualTo("juan@condominio.cl");
        assertThat(respuesta.getRol()).isEqualTo("ROL_USER");
        assertThat(respuesta.getToken()).isEqualTo("token-nuevo-usuario");

        // La contraseña NUNCA debe guardarse en texto plano: se verifica que se persiste la versión encriptada
        verify(usuarioRepository).save(argThat(u -> u.getPassword().equals("clave-encriptada-bcrypt")));
    }
}