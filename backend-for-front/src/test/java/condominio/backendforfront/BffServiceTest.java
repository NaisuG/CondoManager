package condominio.backendforfront;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import condominio.backendforfront.service.BffService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier; // Herramienta Élite para testear código reactivo

@ExtendWith(MockitoExtension.class)
class BffServiceTest {

    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClientMock;
    @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpecMock;
    @Mock private WebClient.RequestBodySpec requestBodySpecMock;
    @Mock private WebClient.RequestHeadersSpec requestHeadersSpecMock;
    @Mock private WebClient.ResponseSpec responseSpecMock;

    private BffService bffService;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClientMock);
        
        bffService = new BffService(
            webClientBuilder,
            "http://mantenimiento", "http://proveedor", "http://registro",
            "http://auth", "http://contabilidad", "http://documentos"
        );
    }

    @Test
    void registrarCondominioCompleto_Devuelve201CuandoRegistroRespondeBien() {
        // --- ARRANGE ---
        Map<String, Object> payloadFrontend = Map.of("nombre", "Edificio Test");
        Map<String, Object> respuestaMsRegistro = Map.of("mensaje", "Copropiedad creada", "idCondominio", 15);

        when(webClientMock.post()).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(anyString())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(any())).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        
        when(responseSpecMock.bodyToMono(Map.class)).thenReturn(Mono.just(respuestaMsRegistro));

        // --- ACT ---
        Mono<ResponseEntity<Map>> resultadoMono = bffService.registrarCondominioCompleto(payloadFrontend);

        // --- ASSERT ---
        StepVerifier.create(resultadoMono)
            .assertNext(response -> {
                assertEquals(201, response.getStatusCode().value(), "El status HTTP debe ser 201 CREATED");
                assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
                assertEquals(15, response.getBody().get("idCondominio"), "Debe propagar el ID del condominio");
            })
            .verifyComplete();
    }

    @Test
    void registrarCondominioCompleto_Devuelve500CuandoRegistroFalla() {
        // --- ARRANGE ---
        Map<String, Object> payloadFrontend = Map.of("nombre", "Edificio Test");

        when(webClientMock.post()).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(anyString())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(any())).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        
        when(responseSpecMock.bodyToMono(Map.class))
                .thenReturn(Mono.error(new RuntimeException("Base de datos no disponible")));

        // --- ACT ---
        Mono<ResponseEntity<Map>> resultadoMono = bffService.registrarCondominioCompleto(payloadFrontend);

        // --- ASSERT ---
        StepVerifier.create(resultadoMono)
            .assertNext(response -> {
                assertEquals(500, response.getStatusCode().value());
                assertTrue(response.getBody().get("error").toString().contains("Fallo BFF: Base de datos no disponible"));
            })
            .verifyComplete();
    }
}