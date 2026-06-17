import { useState, useEffect } from 'react';
import '../css/Finanzas.css'; 

export default function Documentos() {
  const [condominios, setCondominios] = useState([]);
  const [condominioActivo, setCondominioActivo] = useState('');
  const [loading, setLoading] = useState(false);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [archivoSubir, setArchivoSubir] = useState(null);
  const [condominioSubir, setCondominioSubir] = useState('');
  const [uploading, setUploading] = useState(false);
  
  const [docsGenerales, setDocsGenerales] = useState([]);
  const [comprobantes, setComprobantes] = useState([]);
  
  // ESTADO PARA LA VISTA DE CARPETAS
  const [carpetaActiva, setCarpetaActiva] = useState(null);

  useEffect(() => {
    fetch('/api/bff/registro/condominios')
      .then(res => res.json())
      .then(data => setCondominios(data))
      .catch(err => console.error("Error cargando condominios:", err));
  }, []);

  const cargarDocumentos = async (idCondominio) => {
    setLoading(true);
    setCarpetaActiva(null); 
    try {
      const resDocs = await fetch(`/api/bff/documentos/condominio/${idCondominio}/categoria/GENERAL`);
      if (resDocs.ok) setDocsGenerales(await resDocs.json());

      const resComprobantes = await fetch(`/api/bff/documentos/condominio/${idCondominio}/categoria/COMPROBANTE`);
      if (resComprobantes.ok) setComprobantes(await resComprobantes.json());
    } catch (error) {
      console.error("Error al cargar archivos:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!condominioActivo) {
      setDocsGenerales([]);
      setComprobantes([]);
      return;
    }
    cargarDocumentos(condominioActivo);
  }, [condominioActivo]);

  const handleDownload = async (idDocumento) => {
    try {
      const res = await fetch(`/api/bff/documentos/${idDocumento}/descargar`);
      const data = await res.json();
      if (data.url) {
        window.open(data.url, '_blank');
      } else {
        alert("No se pudo generar el enlace de descarga.");
      }
    } catch (error) {
      console.error("Error al descargar:", error);
      alert("Error de conexión al servidor.");
    }
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!archivoSubir || !condominioSubir) {
      alert("Por favor, selecciona un archivo y asigna un condominio.");
      return;
    }

    const idUsuario = localStorage.getItem("idUsuario") || 1; 
    const formData = new FormData();
    formData.append("archivo", archivoSubir);
    formData.append("idCondominio", condominioSubir);
    formData.append("idUsuarioSubio", idUsuario);
    formData.append("categoria", "GENERAL");
    formData.append("periodo", "general");

    setUploading(true);
      try {
      const res = await fetch('/api/bff/documentos/subir', {
        method: 'POST',
        body: formData
      });

      if (res.ok) {
        alert("¡Documento subido con éxito!");
        setIsModalOpen(false);
        setArchivoSubir(null);
        setCondominioSubir('');
        
        if (condominioActivo === condominioSubir) {
          cargarDocumentos(condominioActivo);
        }
      } else if (res.status === 413) {
        alert("El archivo es demasiado pesado. El tamaño máximo permitido es de 15MB.");
      } else {
        const errData = await res.json().catch(() => ({ error: "Error desconocido" }));
        alert(`Error al subir: ${errData.error}`);
      }
    } catch (error) {
      console.error("Error al subir archivo:", error);
      if (error.message.includes('Failed to fetch') || error.message === 'Network Error') {
        alert("Error de red: El archivo excede el límite de 15MB o el servidor no responde.");
      } else {
        alert("Error inesperado en la red.");
      }
    } finally {
      setUploading(false);
    }
  };

  // --- EXTRACCIÓN DE CARPETAS DESDE MINIO ---
  const extraerCarpetas = () => {
    const carpetasSet = new Set();
    comprobantes.forEach(doc => {
      const partes = doc.keyMinio.split('/');
      if (partes.length >= 3) {
        carpetasSet.add(partes[2]);
      }
    });
    return Array.from(carpetasSet).sort((a, b) => b.localeCompare(a));
  };

  const carpetasDisponibles = extraerCarpetas();
  const comprobantesFiltrados = comprobantes.filter(doc => doc.keyMinio.includes(`/${carpetaActiva}/`));

  return (
    <div className="finanzas-page-container">
      <div className="finanzas-header-row">
        <h1 className="finanzas-main-title">Bóveda de Documentos</h1>
        
        <div className="period-selector-card">
          <label>Filtrar por Condominio:</label>
          <select 
            className="modal-select" 
            style={{ width: '250px', margin: 0, padding: '5px' }}
            value={condominioActivo} 
            onChange={(e) => setCondominioActivo(e.target.value)}
          >
            <option value="">-- Selecciona para ver --</option>
            {condominios.map(c => (
              <option key={c.id} value={c.id}>{c.nombre}</option>
            ))}
          </select>
        </div>

        <button className="btn-finanzas btn-finanzas-primary" onClick={() => setIsModalOpen(true)}>
          + Subir Documento
        </button>
      </div>

      {/* --- TABLA 1: DOCUMENTOS GENERALES --- */}
      <div className="finanzas-dashboard-card table-card">
        <h3 className="card-section-title">Documentos Generales</h3>
        <p className="card-section-subtitle">Visualiza y descarga contratos, reglamentos y actas de asambleas.</p>

        {!condominioActivo ? (
          <div className="table-empty-fallback">Selecciona un condominio en la parte superior.</div>
        ) : loading ? (
          <div className="table-loading-spinner">Desencriptando archivos desde MinIO...</div>
        ) : docsGenerales.length === 0 ? (
          <div className="table-empty-fallback">No hay documentos generales registrados.</div>
        ) : (
          <div className="finanzas-table-responsive">
            <table className="finanzas-data-table">
              <thead>
                <tr>
                  <th>Nombre del Documento</th>
                  <th>Fecha de Subida</th>
                  <th>Operaciones</th>
                </tr>
              </thead>
              <tbody>
                {docsGenerales.map((doc) => (
                  <tr key={doc.id}>
                    <td className="fw-bold">{doc.nombreOriginal}</td>
                    <td>{new Date(doc.fechaSubida).toLocaleString('es-CL')}</td>
                    <td>
                      <button className="btn-table-action btn-pay-now" onClick={() => handleDownload(doc.id)}>
                        ↓ Descargar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* --- SECCIÓN 2: CARPETAS DE COMPROBANTES --- */}
      <div className="finanzas-dashboard-card table-card" style={{ marginTop: '20px' }}>
        <h3 className="card-section-title">Comprobantes de Pago</h3>
        <p className="card-section-subtitle">Explora los respaldos financieros organizados por periodo mensual.</p>

        {!condominioActivo ? (
          <div className="table-empty-fallback">Selecciona un condominio en la parte superior.</div>
        ) : loading ? (
          <div className="table-loading-spinner">Buscando comprobantes financieros...</div>
        ) : comprobantes.length === 0 ? (
          <div className="table-empty-fallback">No hay comprobantes de pago asociados a este condominio.</div>
        ) : carpetaActiva === null ? (
          
          /* VISTA DE CARPETAS */
          <div style={{ display: 'flex', gap: '15px', flexWrap: 'wrap', marginTop: '20px', padding: '10px' }}>
            {carpetasDisponibles.map(carpeta => (
              <div 
                key={carpeta}
                onClick={() => setCarpetaActiva(carpeta)}
                style={{
                  background: '#f7fafc', border: '1px solid #e2e8f0', borderRadius: '8px', 
                  padding: '20px', width: '180px', textAlign: 'center', cursor: 'pointer',
                  transition: 'transform 0.2s', boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
                }}
                onMouseOver={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
                onMouseOut={(e) => e.currentTarget.style.transform = 'scale(1)'}
              >
                <div style={{ fontSize: '40px', marginBottom: '10px' }}>📁</div>
                <div style={{ fontWeight: '600', color: '#2d3748' }}>Periodo {carpeta}</div>
              </div>
            ))}
          </div>

        ) : (

          /* VISTA DE ARCHIVOS */
          <div className="finanzas-table-responsive">
            <button 
              onClick={() => setCarpetaActiva(null)} 
              className="btn-finanzas btn-finanzas-secondary"
              style={{ marginBottom: '15px' }}
            >
              ⬅ Volver a Carpetas
            </button>
            <table className="finanzas-data-table">
              <thead>
                <tr>
                  <th>Archivo (Comprobante)</th>
                  <th>Fecha de Subida</th>
                  <th>Operaciones</th>
                </tr>
              </thead>
              <tbody>
                {comprobantesFiltrados.map((doc) => (
                  <tr key={doc.id}>
                    <td className="fw-bold" style={{ color: '#2b6cb0' }}>{doc.nombreOriginal}</td>
                    <td>{new Date(doc.fechaSubida).toLocaleString('es-CL')}</td>
                    <td>
                      <button className="btn-table-action btn-pay-now" onClick={() => handleDownload(doc.id)}>
                        ↓ Descargar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* MODAL DE SUBIDA GENERAL */}
      {isModalOpen && (
        <div className="modal-finanzas-overlay">
          <div className="modal-finanzas-box animate-pop">
            <h3 className="modal-title">Subir Nuevo Documento</h3>
            <p className="modal-description">El archivo será almacenado de forma segura en el bucket del sistema.</p>

            <form onSubmit={handleUpload}>
              <div className="modal-form-group">
                <label>Condominio Destino:</label>
                <select 
                  className="modal-select" 
                  value={condominioSubir} 
                  onChange={(e) => setCondominioSubir(e.target.value)}
                  required
                >
                  <option value="">Seleccione a qué condominio pertenece...</option>
                  {condominios.map(c => (
                    <option key={c.id} value={c.id}>{c.nombre}</option>
                  ))}
                </select>
              </div>

              <div className="modal-form-group">
                <label>Archivo (PDF, Word, JPG, etc):</label>
                <input 
                  type="file" 
                  className="modal-input" 
                  onChange={(e) => setArchivoSubir(e.target.files[0])}
                  required
                />
              </div>

              <div className="modal-actions-row">
                <button type="button" className="btn-finanzas btn-finanzas-secondary" onClick={() => setIsModalOpen(false)} disabled={uploading}>
                  Cancelar
                </button>
                <button type="submit" className="btn-finanzas btn-finanzas-primary" disabled={uploading}>
                  {uploading ? 'Transfiriendo...' : 'Subir y Guardar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}