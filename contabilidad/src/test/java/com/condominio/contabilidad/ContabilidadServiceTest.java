package com.condominio.contabilidad;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.condominio.contabilidad.Model.CobroMensual;
import com.condominio.contabilidad.Model.EstadoCobro;
import com.condominio.contabilidad.Model.ValorGastoComun;
import com.condominio.contabilidad.Repository.CobroMensualRepository;
import com.condominio.contabilidad.Repository.ValorGastoComunRepository;
import com.condominio.contabilidad.Service.ContabilidadService;

@ExtendWith(MockitoExtension.class)
class ContabilidadServiceTest {

    @Mock
    private CobroMensualRepository cobroMensualRepository;

    @Mock
    private ValorGastoComunRepository valorGastoComunRepository;

    @InjectMocks
    private ContabilidadService contabilidadService;

    @Test
    void generarCobroMensual_Exito() {
        Long idUnidad = 101L;
        Long idTipoUnidad = 1L;
        Integer mes = 5;
        Integer anio = 2026;

        when(cobroMensualRepository.existsByIdUnidadAndMesAndAnio(idUnidad, mes, anio))
                .thenReturn(false);

        ValorGastoComun tarifaMock = new ValorGastoComun(1L, idTipoUnidad, 65000);
        when(valorGastoComunRepository.findByIdTipoUnidad(idTipoUnidad))
                .thenReturn(Optional.of(tarifaMock));

        when(cobroMensualRepository.save(any(CobroMensual.class))).thenAnswer(invocation -> {
            CobroMensual cobroGuardado = invocation.getArgument(0);
            cobroGuardado.setId(1L); // Le asignamos un ID falso como si Postgres lo hubiera hecho
            return cobroGuardado;
        });

        // --- ACT (Actuar) ---
        CobroMensual resultado = contabilidadService.generarCobroMensual(idUnidad, idTipoUnidad, mes, anio);

        // --- ASSERT (Afirmar/Comprobar) ---
        assertNotNull(resultado, "El cobro generado no debe ser nulo");
        assertEquals(65000, resultado.getMontoCobrado(), "El monto debe coincidir con la tarifa configurada");
        assertEquals(EstadoCobro.PENDIENTE, resultado.getEstado(), "El estado inicial debe ser PENDIENTE");
        
        verify(cobroMensualRepository, times(1)).save(any(CobroMensual.class));
    }

    @Test
    void generarCobroMensual_FallaCuandoYaExiste() {
        // --- ARRANGE ---
        Long idUnidad = 101L;
        Long idTipoUnidad = 1L;
        Integer mes = 5;
        Integer anio = 2026;

        when(cobroMensualRepository.existsByIdUnidadAndMesAndAnio(idUnidad, mes, anio))
                .thenReturn(true);

        // --- ACT & ASSERT ---
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            contabilidadService.generarCobroMensual(idUnidad, idTipoUnidad, mes, anio);
        });

        assertTrue(excepcion.getMessage().contains("Ya existe un cobro para la unidad"), "El mensaje de error no es el esperado");
        
        verify(cobroMensualRepository, never()).save(any(CobroMensual.class));
    }
}