package com.condominio.documentos.Servicio;

import java.util.UUID;
import java.util.Date;
import java.util.List;
import java.net.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.HttpMethod;
import com.condominio.documentos.Modelo.Documento;
import com.condominio.documentos.Repositorio.DocumentoRepositorio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentoServicio {
    private final AmazonS3 s3Client;
    private final DocumentoRepositorio documentoRepositorio;

    @Value("${minio.bucket.name}")
    private String bucketName;

    public Documento subirDocumento(MultipartFile archivo, Long idCondominio, Long idUsuario) {
        try {
            String extension = archivo.getOriginalFilename().substring(archivo.getOriginalFilename().lastIndexOf("."));
            String keyMinio = UUID.randomUUID().toString() + extension;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(archivo.getSize());
            metadata.setContentType(archivo.getContentType());

            s3Client.putObject(new PutObjectRequest(bucketName, keyMinio, archivo.getInputStream(), metadata));

            Documento nuevoDoc = Documento.builder()
                    .nombreOriginal(archivo.getOriginalFilename())
                    .keyMinio(keyMinio)
                    .idCondominio(idCondominio)
                    .idUsuarioSubio(idUsuario)
                    .build();

            return documentoRepositorio.save(nuevoDoc);

        } catch (Exception e) {
            throw new RuntimeException("Error al subir el archivo a MinIO: " + e.getMessage());
        }
    }

    public List<Documento> listarPorCondominio(Long idCondominio) {
        return documentoRepositorio.findByIdCondominioOrderByFechaSubidaDesc(idCondominio);
    }

    public String generarUrlDescarga(Long idDocumento) {
        Documento doc = documentoRepositorio.findById(idDocumento)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado en BD"));

        Date expiracion = new Date();
        long tiempoMilisegundos = expiracion.getTime();
        tiempoMilisegundos += 1000 * 60 * 15; 
        expiracion.setTime(tiempoMilisegundos);

        URL url = s3Client.generatePresignedUrl(bucketName, doc.getKeyMinio(), expiracion, HttpMethod.GET);
        return url.toString();
    }
}
