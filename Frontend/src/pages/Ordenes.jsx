import { useState, useEffect } from "react";

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
    fetch("http://localhost:9000/api/bff/mantenimientos")
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
    const matchEstado =
      estadoFiltro === "TODOS" || o.estado === estadoFiltro;
    return matchText && matchEstado;
  });

  return (
    <div>
      <div className="page-title">Órdenes de mantenimiento</div>

      <div style={{ display: "flex", gap: 10, marginBottom: "1.25rem" }}>
        <input
          style={{ flex: 1 }}
          type="text"
          placeholder="Buscar por descripción o empresa..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <select value={estadoFiltro} onChange={(e) => setEstadoFiltro(e.target.value)}>
          <option value="TODOS">Todos los estados</option>
          <option value="PENDIENTE">Pendiente</option>
          <option value="EN_PROCESO">En proceso</option>
          <option value="COMPLETADO">Completado</option>
          <option value="CANCELADO">Cancelado</option>
        </select>
      </div>

      {error && <div className="error-msg">Error al cargar órdenes: {error}</div>}
      {!ordenes && !error && <div className="loading">Cargando órdenes...</div>}
      {ordenes && filtered.length === 0 && (
        <div className="empty-state">No hay órdenes que coincidan.</div>
      )}

      <div style={{ display: "grid", gap: 10 }}>
        {filtered.map((o) => (
          <div
            key={o.idOrden}
            className="card"
            style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: "1rem" }}
          >
            <div>
              <div style={{ fontSize: 14, marginBottom: 4 }}>{o.descripcion}</div>
              <div style={{ fontSize: 12, color: "#888" }}>
                {o.nombreEmpresa} · {o.fecha || "—"} · #{o.idOrden}
              </div>
            </div>
            <span className={`badge ${BADGE_CLASS[o.estado] || "badge-pending"}`}>
              {ESTADO_LABELS[o.estado] || o.estado}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}