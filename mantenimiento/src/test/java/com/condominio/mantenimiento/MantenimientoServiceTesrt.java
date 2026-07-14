package com.condominio.mantenimiento;

import com.condominio.mantenimiento.Cliente.ProveedorCliente;
import com.condominio.mantenimiento.DTO.OrdenDetalleDTO;
import com.condominio.mantenimiento.DTO.ProveedorDTO;
import com.condominio.mantenimiento.Modelo.EstadoOrden;
import com.condominio.mantenimiento.Modelo.OrdenMantenimiento;
import com.condominio.mantenimiento.Repositorio.OrdenMantenimientoRepository;
import com.condominio.mantenimiento.Servicio.MantenimientoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MantenimientoService - pruebas unitarias")
class MantenimientoServiceTest {

    @Mock
    private OrdenMantenimientoRepository repository;

    @Mock
    private ProveedorCliente proveedorClient;

    @InjectMocks
    private MantenimientoService service;

    private OrdenMantenimiento ordenPendiente;

    @BeforeEach
    void setUp() {
        ordenPendiente = new OrdenMantenimiento(
                1L,
                100L,
                "Reparación de ascensor torre B",
                EstadoOrden.PENDIENTE,
                LocalDate.of(2026, 8, 1)
        );
    }

    // ---------- crearOrden ----------

    @Test
    @DisplayName("crearOrden: siempre fuerza el estado inicial a PENDIENTE, sin importar lo que venga en el request")
    void crearOrden_fuerzaEstadoPendiente() {
        OrdenMantenimiento nueva = new OrdenMantenimiento(
                null, 200L, "Fuga de agua", EstadoOrden.FINALIZADA, LocalDate.now());

        when(repository.save(any(OrdenMantenimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenMantenimiento resultado = service.crearOrden(nueva);

        assertThat(resultado.getEstado()).isEqualTo(EstadoOrden.PENDIENTE);
        verify(repository, times(1)).save(nueva);
    }

    // ---------- listarTodas ----------

    @Test
    @DisplayName("listarTodas: retorna todas las órdenes que entrega el repositorio")
    void listarTodas_retornaListaCompleta() {
        OrdenMantenimiento otra = new OrdenMantenimiento(
                2L, 101L, "Pintura fachada", EstadoOrden.EN_PROCESO, LocalDate.now());
        when(repository.findAll()).thenReturn(Arrays.asList(ordenPendiente, otra));

        List<OrdenMantenimiento> resultado = service.listarTodas();

        assertThat(resultado).hasSize(2).containsExactly(ordenPendiente, otra);
    }

    @Test
    @DisplayName("listarTodas: retorna lista vacía cuando no hay órdenes registradas")
    void listarTodas_listaVacia() {
        when(repository.findAll()).thenReturn(List.of());

        List<OrdenMantenimiento> resultado = service.listarTodas();

        assertThat(resultado).isEmpty();
    }

    // ---------- obtenerDetalleOrden ----------

    @Test
    @DisplayName("obtenerDetalleOrden: combina la orden con los datos reales del proveedor (Feign OK)")
    void obtenerDetalleOrden_proveedorDisponible() {
        ProveedorDTO proveedor = new ProveedorDTO();
        proveedor.setId(100L);
        proveedor.setNombreEmpresa("Ascensores Ltda.");

        when(repository.findById(1L)).thenReturn(Optional.of(ordenPendiente));
        when(proveedorClient.obtenerProveedorPorId(100L)).thenReturn(proveedor);

        OrdenDetalleDTO detalle = service.obtenerDetalleOrden(1L);

        assertThat(detalle.getOrden()).isEqualTo(ordenPendiente);
        assertThat(detalle.getProveedor().getNombreEmpresa()).isEqualTo("Ascensores Ltda.");
        verify(proveedorClient, times(1)).obtenerProveedorPorId(100L);
    }

    @Test
    @DisplayName("obtenerDetalleOrden: si el microservicio de proveedores falla, degrada con un proveedor placeholder en vez de romper la respuesta")
    void obtenerDetalleOrden_proveedorNoDisponible_degradaGraciosamente() {
        when(repository.findById(1L)).thenReturn(Optional.of(ordenPendiente));
        when(proveedorClient.obtenerProveedorPorId(100L))
                .thenThrow(new RuntimeException("Connection refused"));

        OrdenDetalleDTO detalle = service.obtenerDetalleOrden(1L);

        assertThat(detalle.getProveedor()).isNotNull();
        assertThat(detalle.getProveedor().getId()).isEqualTo(100L);
        assertThat(detalle.getProveedor().getNombreEmpresa())
                .isEqualTo("Proveedor No Disponible (MS Apagado)");
    }

    @Test
    @DisplayName("obtenerDetalleOrden: lanza excepción si la orden no existe")
    void obtenerDetalleOrden_ordenInexistente_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerDetalleOrden(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Orden no encontrada");

        verifyNoInteractions(proveedorClient);
    }

    // ---------- cancelarOrden ----------

    @Test
    @DisplayName("cancelarOrden: cambia el estado a CANCELADA y persiste el cambio")
    void cancelarOrden_cambiaEstadoYGuarda() {
        when(repository.findById(1L)).thenReturn(Optional.of(ordenPendiente));
        when(repository.save(any(OrdenMantenimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenMantenimiento resultado = service.cancelarOrden(1L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoOrden.CANCELADA);
        verify(repository).save(ordenPendiente);
    }

    @Test
    @DisplayName("cancelarOrden: lanza excepción con mensaje claro si la orden no existe")
    void cancelarOrden_ordenInexistente_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelarOrden(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");

        verify(repository, never()).save(any());
    }
}