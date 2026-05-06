import { useState } from "react";

export default function PaginaCondominio() {
  const [id, setId] = useState("");
  const [condo, setCondo] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const buscar = () => {
    if (!id) return;
    setLoading(true);
    setCondo(null);
    setError(null);
    fetch(`http://localhost:9000/api/bff/registro/condominio/${id}`)
      .then((r) => {
        if (!r.ok) throw new Error(`HTTP ${r.status}`);
        return r.json();
      })
      .then(setCondo)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  const totalUnidades = condo
    ? condo.torres.reduce((s, t) => s + t.unidades.length, 0)
    : 0;
  const totalM2 = condo
    ? Math.round(condo.torres.reduce((s, t) => s + t.unidades.reduce((ss, u) => ss + u.m2, 0), 0))
    : 0;

  return (
    <div>
      <div className="page-title">Detalle de condominio</div>

      <div style={{ display: "flex", gap: 10, marginBottom: "1.5rem" }}>
        <input
          style={{ flex: 1 }}
          type="number"
          placeholder="ID del condominio..."
          value={id}
          onChange={(e) => setId(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && buscar()}
        />
        <button className="btn btn-primary" onClick={buscar}>Buscar</button>
      </div>

      {error && <div className="error-msg">Error: {error}</div>}
      {loading && <div className="loading">Cargando...</div>}
      {!condo && !loading && !error && (
        <div className="empty-state">Ingresa un ID para buscar un condominio.</div>
      )}

      {condo && (
        <div className="card">
          <div style={{ marginBottom: "1rem", paddingBottom: "1rem", borderBottom: "1px solid #e5e5e5" }}>
            <div style={{ fontSize: 18, fontWeight: 600 }}>{condo.nombre}</div>
            <div style={{ fontSize: 13, color: "#888", marginTop: 4 }}>{condo.direccion}</div>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "repeat(3,1fr)", gap: 10, marginBottom: "1.25rem" }}>
            {[
              { val: condo.torres.length, lbl: "Torres" },
              { val: totalUnidades, lbl: "Unidades" },
              { val: `${totalM2} m²`, lbl: "Total construido" },
            ].map(({ val, lbl }) => (
              <div key={lbl} style={{ background: "#f5f5f3", borderRadius: 8, padding: "0.75rem 1rem" }}>
                <div style={{ fontSize: 22, fontWeight: 600 }}>{val}</div>
                <div style={{ fontSize: 12, color: "#888", marginTop: 2 }}>{lbl}</div>
              </div>
            ))}
          </div>

          <div style={{ fontSize: 11, fontWeight: 500, color: "#888", textTransform: "uppercase", letterSpacing: "0.06em", marginBottom: 10 }}>
            Torres y unidades
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: 10 }}>
            {condo.torres.map((t) => (
              <div key={t.numero} style={{ background: "#f5f5f3", borderRadius: 10, padding: "1rem" }}>
                <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 10 }}>
                  Torre {t.numero}{" "}
                  <span style={{ fontSize: 12, fontWeight: 400, color: "#888" }}>
                    ({t.unidades.length} unidades)
                  </span>
                </div>
                {t.unidades.map((u) => (
                  <div
                    key={u.numero}
                    style={{ display: "flex", justifyContent: "space-between", padding: "5px 0", borderBottom: "1px solid #e5e5e5", fontSize: 13 }}
                  >
                    <span style={{ fontWeight: 500 }}>Unidad {u.numero}</span>
                    <span style={{ color: "#888" }}>{u.tipo} · {u.m2} m²</span>
                  </div>
                ))}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}