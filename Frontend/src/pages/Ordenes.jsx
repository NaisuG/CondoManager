import { useState, useEffect } from "react";
import "../App.css";
import "../css/Ordenes.css"; // Importación obligatoria del nuevo CSS

const ESTADO_LABELS = {
  PENDIENTE: "Pendiente",
  EN_PROCESO: "En proceso",
  COMPLETADO: "Completado",
  CANCELADO: "Cancelado",
};

const BADGE_CLASS = {
  PENDIENTE: "badge-pending",
  EN_PROCESO: "badge-progress",
  COMPLETADO: "badge-done",
  CANCELADO: "badge-cancelled",
};

export default function PaginaOrdenes() {
  const [ordenes, setOrdenes] = useState(null);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState("");
  const [estadoFiltro, setEstadoFiltro] = useState("TODOS");

  useEffect(() => {
    fetch("/api/bff/mantenimientos")
        .then((r) => {
          if (!r.ok) throw new Error(`HTTP ${r.status}`);
          return r.json();
        })
        .then(setOrdenes)
        .catch((e) => setError(e.message));
  }, []);

  const filtered = (ordenes || []).filter((o) => {
    const matchText =
        !search ||
        o.descripcion.toLowerCase().includes(search.toLowerCase()) ||
        o.nombreEmpresa.toLowerCase().includes(search.toLowerCase());
    const matchEstado = estadoFiltro === "TODOS" || o.estado === estadoFiltro;
    return matchText && matchEstado;
  });

  return (
      <div className="ordenes-contenedor">
        <div className="page-title" style={{ fontSize: "1.5rem", fontWeight: "700", color: "#0c447c", marginBottom: "1.5rem" }}>
          Órdenes de Mantenimiento
        </div>

        <div className="ordenes-toolbar">
          {/* Buscador */}
          <div className="search-input-wrapper">
            <span className="input-icon-left">🔍</span>
            <input
                type="text"
                placeholder="Buscar por descripción o empresa..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="ordenes-input-text"
            />
          </div>

          {/* Filtro de Estado */}
          <div className="select-input-wrapper">
            <select
                value={estadoFiltro}
                onChange={(e) => setEstadoFiltro(e.target.value)}
                className="ordenes-select"
            >
              <option value="TODOS">Todos los estados</option>
              <option value="PENDIENTE">Pendiente</option>
              <option value="EN_PROCESO">En proceso</option>
              <option value="COMPLETADO">Completado</option>
              <option value="CANCELADO">Cancelado</option>
            </select>
            <span className="select-chevron-right">▼</span>
          </div>
        </div>

        {error && <div className="error-msg">Error al cargar órdenes: {error}</div>}
        {!ordenes && !error && <div className="loading">Cargando órdenes de la base de datos...</div>}
        {ordenes && filtered.length === 0 && (
            <div className="empty-state">No hay órdenes que coincidan con la búsqueda.</div>
        )}

        <div className="ordenes-lista">
          {filtered.map((o) => (
              <div key={o.idOrden} className="orden-card">
                <div>
                  <div className="orden-titulo">{o.descripcion}</div>
                  <div className="orden-subtexto">
                    🏢 {o.nombreEmpresa} · 📅 {o.fecha || "—"} · <span className="orden-id-tag">#{o.idOrden}</span>
                  </div>
                </div>
                <span className={`badge ${BADGE_CLASS[o.estado]}`}>
              {ESTADO_LABELS[o.estado]}
            </span>
              </div>
          ))}
        </div>
      </div>
  );
}