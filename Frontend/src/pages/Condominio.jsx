import { useState } from "react";
import "../css/Condominio.css"; // Importación obligatoria del nuevo CSS

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

        fetch(`/api/bff/registro/condominio/${id}`)
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
        <div className="condo-contenedor">
            <div className="page-title" style={{ fontSize: "1.5rem", fontWeight: "700", color: "#0c447c", marginBottom: "1.5rem" }}>
                Buscador de Copropiedades
            </div>

            <div className="condo-search-bar">
                <div className="condo-input-wrapper">
                    <input
                        type="number"
                        placeholder="Ingrese el ID único del condominio..."
                        value={id}
                        onChange={(e) => setId(e.target.value)}
                        className="condo-input-number"
                    />
                </div>
                <button onClick={buscar} className="condo-btn-submit">
                    Consultar
                </button>
            </div>

            {loading && <div className="loading">Consultando registros en la base de datos...</div>}
            {error && <div className="error-msg">No se encontró ningún condominio con el ID ingresado.</div>}

            {condo && (
                <div className="condo-result-card">
                    <h2 className="condo-title">{condo.nombre}</h2>
                    <div className="condo-subtitle">{condo.direccion || "Dirección no registrada"}</div>

                    <div className="condo-mini-metrics-grid">
                        <div className="condo-mini-card">
                            <div className="condo-mini-value">{condo.torres.length}</div>
                            <div className="condo-mini-label">Torres Edificadas</div>
                        </div>
                        <div className="condo-mini-card">
                            <div className="condo-mini-value">{totalUnidades}</div>
                            <div className="condo-mini-label">Unidades Habitables</div>
                        </div>
                        <div className="condo-mini-card">
                            <div className="condo-mini-value">{totalM2} m²</div>
                            <div className="condo-mini-label">Superficie Construida</div>
                        </div>
                    </div>

                    <div className="condo-section-header">
                        Estructura de Torres y Departamentos
                    </div>

                    <div className="condo-torres-grid">
                        {condo.torres.map((t) => (
                            <div key={t.numero} className="condo-torre-card">
                                <div className="condo-torre-title">
                                    Torre {t.numero} <span className="condo-torre-count">({t.unidades.length} u.)</span>
                                </div>
                                {t.unidades.map((u) => (
                                    <div key={u.numero} className="condo-unidad-item">
                                        <span className="condo-unidad-number">N° {u.numero}</span>
                                        <span className="condo-unidad-desc">{u.tipo} ({u.m2} m²)</span>
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