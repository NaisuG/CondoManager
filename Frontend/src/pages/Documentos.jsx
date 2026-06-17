import { useState, useEffect } from 'react';
import '../css/Finanzas.css'; // Reutilizamos tu CSS corporativo

export default function Documentos() {
  const [condominios, setCondominios] = useState([]);
  const [condominioActivo, setCondominioActivo] = useState('');
  const [documentos, setDocumentos] = useState([]);
  const [loading, setLoading] = useState(false);

  // Estados del Modal de Subida
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [archivoSubir, setArchivoSubir] = useState(null);
  const [condominioSubir, setCondominioSubir] = useState('');
  const [uploading, setUploading] = useState(false);

  // 1. Al cargar la página, traemos todos los condominios disponibles
  useEffect(() => {
    fetch('/api/bff/registro/condominios')
      .then(res => res.json())
      .then(data => setCondominios(data))
      .catch(err => console.error("Error cargando condominios:", err));
  }, []);

  // 2. Cada vez que el admin selecciona un condominio, buscamos sus documentos
  useEffect(() => {
    if (!condominioActivo) {
      setDocumentos([]);
      return;
    }
    
    setLoading(true);
    fetch(`/api/bff/documentos/condominio/${condominioActivo}`)
      .then(res => res.json())
      .then(data => setDocumentos(data))
      .catch(err => console.error("Error cargando documentos:", err))
      .finally(() => setLoading(false));
  }, [condominioActivo]);

  // 3. Lógica para descargar archivo (Usa la URL pre-firmada de MinIO)
  const handleDownload = async (idDocumento) => {
    try {
      const res = await fetch(`/api/bff/documentos/${idDocumento}/descargar`);
      const data = await res.json();
      if (data.url) {
        window.open(data.url, '_blank'); // Abre la descarga en una pestaña nueva
      } else {
        alert("No se pudo generar el enlace de descarga.");
      }
    } catch (error) {
      console.error("Error al descargar:", error);
      alert("Error de conexión al servidor.");
    }
  };

  // 4. Lógica para subir un archivo nuevo
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

    setUploading(true);
    try {
      const res = await fetch('/api/bff/documentos/subir', {
        method: 'POST',
        body: formData
      });

      if (res.ok) {
        alert("¡Documento subido y asegurado en MinIO con éxito!");
        setIsModalOpen(false);
        setArchivoSubir(null);
        setCondominioSubir('');
        
        if (condominioActivo === condominioSubir) {
          const refresh = await fetch(`/api/bff/documentos/condominio/${condominioActivo}`);
          setDocumentos(await refresh.json());
        }
      } else {
        const errData = await res.json();
        alert(`Error al subir: ${errData.error}`);
      }
    } catch (error) {
      console.error("Error al subir archivo:", error);
      alert("Error de red.");
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="finanzas-page-container">
      <div className="finanzas-header-row">
        <h1 className="finanzas-main-title">Bóveda de Documentos</h1>
        
        {/* Selector de condominio para ver la tabla */}
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

      <div className="finanzas-dashboard-card table-card">
        <h3 className="card-section-title">Archivos Registrados</h3>
        <p className="card-section-subtitle">Visualiza y descarga contratos, reglamentos y actas de asambleas.</p>

        {!condominioActivo ? (
          <div className="table-empty-fallback">Selecciona un condominio en la parte superior para cargar sus documentos.</div>
        ) : loading ? (
          <div className="table-loading-spinner">Desencriptando archivos desde MinIO...</div>
        ) : documentos.length === 0 ? (
          <div className="table-empty-fallback">No hay documentos registrados para este condominio.</div>
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
                {documentos.map((doc) => (
                  <tr key={doc.id}>
                    <td className="fw-bold">{doc.nombreOriginal}</td>
                    <td>{new Date(doc.fechaSubida).toLocaleString('es-CL')}</td>
                    <td>
                      <button 
                        className="btn-table-action btn-pay-now" 
                        onClick={() => handleDownload(doc.id)}
                      >
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

      {/* --- MODAL DE SUBIDA --- */}
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
                <button 
                  type="button" 
                  className="btn-finanzas btn-finanzas-secondary" 
                  onClick={() => setIsModalOpen(false)}
                  disabled={uploading}
                >
                  Cancelar
                </button>
                <button 
                  type="submit" 
                  className="btn-finanzas btn-finanzas-primary"
                  disabled={uploading}
                >
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