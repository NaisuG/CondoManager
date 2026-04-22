package com.condominio.mantenimiento.Servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.condominio.mantenimiento.Cliente.ProveedorCliente;
import com.condominio.mantenimiento.DTO.OrdenDetalleDTO;
import com.condominio.mantenimiento.DTO.ProveedorDTO;
import com.condominio.mantenimiento.Modelo.EstadoOrden;
import com.condominio.mantenimiento.Modelo.OrdenMantenimiento;
import com.condominio.mantenimiento.Repositorio.OrdenMantenimientoRepository;

@Service
public class MantenimientoService {

    @Autowired
    private OrdenMantenimientoRepository repository;

    @Autowired
    private ProveedorCliente proveedorClient;

    public OrdenMantenimiento crearOrden(OrdenMantenimiento orden) {
        orden.setEstado(EstadoOrden.PENDIENTE);
        return repository.save(orden);
    }

    public List<OrdenMantenimiento> listarTodas() {
        return repository.findAll();
    }

    public OrdenDetalleDTO obtenerDetalleOrden(Long idOrden) {
        OrdenMantenimiento orden = repository.findById(idOrden)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        ProveedorDTO proveedor = null;
        try {
            // Intenta llamar al microservicio
            proveedor = proveedorClient.obtenerProveedorPorId(orden.getIdProveedor());
        } catch (Exception e) {
            // Si el microservicio está apagado o no existe aún, creamos un proveedor "falso" temporal
            System.out.println("Microservicio de proveedores no disponible.");
            proveedor = new ProveedorDTO();
            proveedor.setId(orden.getIdProveedor());
            proveedor.setNombreEmpresa("Proveedor No Disponible (MS Apagado)");
        }

        OrdenDetalleDTO detalle = new OrdenDetalleDTO();
        detalle.setOrden(orden);
        detalle.setProveedor(proveedor);

        return detalle;
    }

    public OrdenMantenimiento cancelarOrden(Long idOrden) {
        OrdenMantenimiento orden = repository.findById(idOrden)
                .orElseThrow(() -> new RuntimeException("No se puede cancelar. La orden con ID " + idOrden + " no existe."));

        orden.setEstado(EstadoOrden.CANCELADA);
        return repository.save(orden);
    }
}
