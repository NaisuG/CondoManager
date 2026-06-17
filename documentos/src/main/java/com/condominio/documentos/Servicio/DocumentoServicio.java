package com.condominio.documentos.Servicio;

import java.util.UUID;
import java.util.Date;
import java.util.List;
import java.net.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
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

    @Value("${minio.access.key}")
    private String accessKey;

    @Value("${minio.secret.key}")
    private String secretKey;

    public Documento subirDocumento(MultipartFile archivo, Long idCondominio, String nombreCarpeta, Long idUsuario, String categoria, String periodo) {
    try {
        String extension = archivo.getOriginalFilename().substring(archivo.getOriginalFilename().lastIndexOf("."));
        
        // Usamos el nombre sanitizado para la ruta de MinIO
        String folder = (categoria.equals("COMPROBANTE")) 
            ? nombreCarpeta + "/comprobantes/" + periodo + "/" 
            : nombreCarpeta + "/general/";
            
        String keyMinio = folder + UUID.randomUUID().toString() + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(archivo.getSize());
        metadata.setContentType(archivo.getContentType());

        s3Client.putObject(new PutObjectRequest(bucketName, keyMinio, archivo.getInputStream(), metadata));

        Documento nuevoDoc = Documento.builder()
                .nombreOriginal(archivo.getOriginalFilename())
                .keyMinio(keyMinio)
                .idCondominio(idCondominio) // Mantenemos la integridad referencial en Postgres
                .idUsuarioSubio(idUsuario)
                .categoria(categoria)
                .build();

        return documentoRepositorio.save(nuevoDoc);

    } catch (Exception e) {
        throw new RuntimeException("Error al subir el archivo a MinIO: " + e.getMessage());
    }
}

    public List<Documento> listarPorCondominioYCategoria(Long idCondominio, String categoria) {
    return documentoRepositorio.findByIdCondominioAndCategoriaOrderByFechaSubidaDesc(idCondominio, categoria);
    }

    public String generarUrlDescarga(Long idDocumento) {
        Documento doc = documentoRepositorio.findById(idDocumento)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado en BD"));

        Date expiracion = new Date();
        long tiempoMilisegundos = expiracion.getTime();
        tiempoMilisegundos += 1000 * 60 * 15; 
        expiracion.setTime(tiempoMilisegundos);

        com.amazonaws.auth.BasicAWSCredentials creds = new com.amazonaws.auth.BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3Publico = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration("http://localhost:9005", "us-east-1"))
                .withPathStyleAccessEnabled(true)
                .withCredentials(new com.amazonaws.auth.AWSStaticCredentialsProvider(creds))
                .build();

        URL url = s3Publico.generatePresignedUrl(bucketName, doc.getKeyMinio(), expiracion, HttpMethod.GET);
        
        return url.toString();
    }
}