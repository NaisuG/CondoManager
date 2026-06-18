package condominio.registro.controller;

import condominio.registro.dto.onboarding.RegistroOnboardingDTO;
import condominio.registro.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/crear-completo")
    public ResponseEntity<Map<String, Object>> crearCompleto(@RequestBody RegistroOnboardingDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(onboardingService.registrarCondominioCompleto(request));
    }
}