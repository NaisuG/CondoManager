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
            @RequestParam("idCondominio") Long idCondominio,
            @RequestParam("idUsuarioSubio") Long idUsuarioSubio) {
        
        Documento guardado = documentoService.subirDocumento(archivo, idCondominio, idUsuarioSubio);
        return ResponseEntity.ok(guardado);
    }

    @GetMapping("/condominio/{idCondominio}")
    public ResponseEntity<List<Documento>> listar(@PathVariable Long idCondominio) {
        return ResponseEntity.ok(documentoService.listarPorCondominio(idCondominio));
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<Map<String, String>> obtenerUrlDescarga(@PathVariable Long id) {
        String url = documentoService.generarUrlDescarga(id);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
