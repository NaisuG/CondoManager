package com.condominio.documentos.Controlador;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.condominio.documentos.Modelo.Documento;
import com.condominio.documentos.Servicio.DocumentoServicio;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documentos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DocumentoControlador {

    private final DocumentoServicio documentoService;

    @PostMapping("/subir")
    public ResponseEntity<Documento> subir(
    @RequestParam("archivo") MultipartFile archivo,
    @RequestParam("idCondominio") Long idCondominio, // El ID numérico para la Base de Datos
    @RequestParam("nombreCarpeta") String nombreCarpeta, // El texto sanitizado para MinIO
    @RequestParam("idUsuarioSubio") Long idUsuarioSubio,
    @RequestParam("categoria") String categoria,
    @RequestParam(value = "periodo", required = false, defaultValue = "general") String periodo) {

    // Llamada corregida sin comas huérfanas
    Documento guardado = documentoService.subirDocumento(archivo, idCondominio, nombreCarpeta, idUsuarioSubio, categoria, periodo);
    return ResponseEntity.ok(guardado);
}

    @GetMapping("/condominio/{idCondominio}/categoria/{categoria}")
    public ResponseEntity<List<Documento>> listarPorCategoria(
        @PathVariable Long idCondominio, 
        @PathVariable String categoria) {
    return ResponseEntity.ok(documentoService.listarPorCondominioYCategoria(idCondominio, categoria));
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<Map<String, String>> obtenerUrlDescarga(@PathVariable Long id) {
        String url = documentoService.generarUrlDescarga(id);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
