package condominio.auth;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import condominio.auth.service.JwtService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService - pruebas unitarias")
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Mismo valor de ejemplo que trae application.properties, para pruebas deterministas
        ReflectionTestUtils.setField(jwtService, "secret",
                "clave-secreta-muy-larga-para-firmar-tokens-jwt-condominio-2026");
        ReflectionTestUtils.setField(jwtService, "expiration", 86_400_000L); // 24 horas

        userDetails = new User("residente@condominio.cl", "hash",
                List.of(() -> "ROL_USER"));
    }

    @Test
    @DisplayName("generateToken: produce un JWT no nulo del cual se puede extraer el username original")
    void generateToken_yExtractUsername_consistentes() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("residente@condominio.cl");
    }

    @Test
    @DisplayName("isTokenValid: retorna true para un token recién emitido con el mismo usuario")
    void isTokenValid_tokenFrescoMismoUsuario_esValido() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: retorna false si el token pertenece a otro usuario")
    void isTokenValid_usuarioDistinto_noEsValido() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otroUsuario = new User("otro@condominio.cl", "hash", List.of());

        assertThat(jwtService.isTokenValid(token, otroUsuario)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid: un token expirado lanza ExpiredJwtException al intentar leerlo")
    void token_expirado_lanzaExcepcionAlValidar() {
        // Expiración de -1 ms -> el token nace ya vencido
        ReflectionTestUtils.setField(jwtService, "expiration", -1L);
        String tokenExpirado = jwtService.generateToken(userDetails);

        assertThatThrownBy(() -> jwtService.isTokenValid(tokenExpirado, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }
}