import { useState, useEffect } from "react";
import "../css/Condominio.css";

export default function Condominio() {
  const [listaCondominios, setListaCondominios] = useState([]);
  const [condoActivo, setCondoActivo] = useState(null);
  const [loadingList, setLoadingList] = useState(false);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [error, setError] = useState(null);

  // Al cargar el componente, obtenemos todos los condominios disponibles (Endpoint GET masivo)
  useEffect(() => {
    fetchCondominios();
  }, []);

  const fetchCondominios = async () => {
    setLoadingList(true);
    setError(null);
    try {
      const response = await fetch("/api/bff/registro/condominios");
      if (!response.ok) throw new Error(`Error en servidor (HTTP ${response.status})`);
      const data = await response.json();
      setListaCondominios(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Error al listar condominios:", err);
      setError("No se pudo conectar con el servicio de registros inmobiliarios.");
    } finally {
      setLoadingList(false);
    }
  };

  // Trae el condominio completo con sus colecciones internas anidadas (Torres y Unidades)
  const cargarDetalleCondominio = async (idCondo) => {
    setLoadingDetail(true);
    try {
      const response = await fetch(`/api/bff/registro/condominio/${idCondo}`);
      if (!response.ok) throw new Error("No se pudo obtener el desglose de la propiedad.");
      const data = await response.json();
      setCondoActivo(data);
    } catch (err) {
      console.error("Error al cargar detalle estructural:", err);
      alert(err.message);
    } finally {
      setLoadingDetail(false);
    }
  };

  // Cálculos dinámicos en caliente para los KPI de la copropiedad seleccionada
  const totalUnidades = condoActivo
    ? condoActivo.torres?.reduce((acc, t) => acc + (t.unidades?.length || 0), 0)
    : 0;

  const totalM2 = condoActivo
    ? Math.round(condoActivo.torres?.reduce((acc, t) => acc + t.unidades?.reduce((sub, u) => sub + u.m2, 0), 0))
    : 0;

  return (
    <div className="modulo-condominio-container">
      
      {/* HEADER DE CONTEXTO */}
      <div className="modulo-condominio-header">
        <h1 className="modulo-condominio-title">Infraestructura y Copropiedades</h1>
        <p className="modulo-condominio-subtitle">
          Audite el estado de los bienes raíces, valide la distribución de unidades habitacionales y supervise las superficies declaradas en el sistema.
        </p>
      </div>

      {/* DISEÑO DISTRIBUIDO: LISTADO IZQUIERDO -> DETALLE DERECHO */}
      <div className="modulo-condominio-layout">
        
        {/* PANEL LATERAL DE SELECCIÓN */}
        <div className="workspace-left-panel">
          <h2 className="panel-section-title">🏢 Copropiedades Activas</h2>
          
          {loadingList && <div className="feedback-state loading-box">Sincronizando registros...</div>}
          {error && <div className="feedback-state error-box">⚠️ {error}</div>}
          {!loadingList && listaCondominios.length === 0 && !error && (
            <div className="feedback-state empty-box">No hay condominios registrados en la base de datos.</div>
          )}

          <div className="lista-condos-scroll">
            {listaCondominios.map((c) => (
              <div 
                key={c.id} 
                className={`condo-selection-card ${condoActivo?.id === c.id ? "card-is-active" : ""}`}
                onClick={() => cargarDetalleCondominio(c.id)}
              >
                <div className="card-info-group">
                  <span className="card-info-name">🏢 {c.nombre}</span>
                  <span className="card-info-address">📍 {c.direccion || "Sin dirección física"}</span>
                </div>
                <div className="card-info-id">ID: {c.id}</div>
              </div>
            ))}
          </div>
        </div>

        {/* WORKSPACE DETALLADO */}
        <div className="workspace-right-panel">
          {loadingDetail ? (
            <div className="detail-loading-placeholder">
              <div className="spinner-mock"></div>
              <p>Mapeando topología estructural del condominio...</p>
            </div>
          ) : condoActivo ? (
            <div className="active-condo-view">
              
              {/* ENCABEZADO PROPIEDAD */}
              <div className="active-condo-header">
                <div>
                  <h2 className="active-title">{condoActivo.nombre}</h2>
                  <span className="active-subtitle">Dirección Legal: {condoActivo.direccion || "No especificada"}</span>
                </div>
                <div className="active-badge">Copropiedad N° {condoActivo.id}</div>
              </div>

              {/* RETÍCULA DE MÉTRICAS GLOBALES */}
              <div className="metrics-summary-grid">
                <div className="metric-item-card">
                  <span className="metric-value">{condoActivo.torres?.length || 0}</span>
                  <span className="metric-label">Torres / Bloques</span>
                </div>
                <div className="metric-item-card">
                  <span className="metric-value">{totalUnidades}</span>
                  <span className="metric-label">Unidades Totales</span>
                </div>
                <div className="metric-item-card">
                  <span className="metric-value">{totalM2.toLocaleString('es-CL')} m²</span>
                  <span className="metric-label">Área Construida</span>
                </div>
              </div>

              {/* MAPA ARBÓREO DE DEPARTAMENTOS */}
              <div className="structural-tree-section">
                <h3 className="tree-heading">🗺️ Distribución Física de Bienes Raíces</h3>
                
                {condoActivo.torres && condoActivo.torres.length > 0 ? (
                  <div className="torres-grid-container">
                    {condoActivo.torres.map((torre) => (
                      <div key={torre.id || torre.numero} className="torre-structure-card">
                        <div className="torre-header-bar">
                          🗼 Torre {torre.numero}
                          <span className="torre-badge-count">{torre.unidades?.length || 0} u.</span>
                        </div>
                        
                        <div className="unidades-flex-wrapper">
                          {torre.unidades && torre.unidades.length > 0 ? (
                            torre.unidades.map((u) => (
                              <div key={u.id || u.numero} className="unidad-item-pill">
                                <div className="pill-primary">N° {u.numero}</div>
                                <div className="pill-secondary">
                                  {u.tipo || "Unidad"} • {u.m2}m²
                                </div>
                              </div>
                            ))
                          ) : (
                            <div className="empty-torre-text">Sin unidades asignadas</div>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="tree-empty-alert">
                    Esta propiedad no cuenta con subdivisiones de bloques o torres asociadas en los registros.
                  </div>
                )}
              </div>

            </div>
          ) : (
            <div className="workspace-empty-state">
              <div className="empty-state-icon">🏢</div>
              <h3>Consola Estructural</h3>
              <p>Seleccione un condominio del catálogo de la izquierda para desglosar su topología, consultar número de unidades habitacionales y metros cuadrados.</p>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}