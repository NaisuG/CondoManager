package com.condominio.documentos.Repositorio;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.condominio.documentos.Modelo.Documento;

public interface DocumentoRepositorio extends JpaRepository<Documento, Long> {
    List<Documento> findByIdCondominioOrderByFechaSubidaDesc(Long idCondominio);
    
    List<Documento> findByIdUsuarioSubioOrderByFechaSubidaDesc(Long idUsuarioSubio);
    
    List<Documento> findByIdCondominioAndIdUsuarioSubio(Long idCondominio, Long idUsuarioSubio);

}
