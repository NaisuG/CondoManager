import { useState } from "react";
import "../css/Alta.css";

export default function PaginaAlta() {
  const [step, setStep] = useState(1);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const usuarioSesion = JSON.parse(localStorage.getItem("usuario_sesion") || "null");
  const idUsuarioActual = usuarioSesion?.idUsuario ?? 1;

  // ESTADO MAESTRO (El JSON gigante que enviaremos al final)
  const [condominioData, setCondominioData] = useState({
    nombre: "",
    direccion: "",
    tiposUnidad: [],
    torres: [],
  });

  // --- MANEJADORES DE ESTADO ---
  const handleBasicos = (e) => {
    setCondominioData({ ...condominioData, [e.target.name]: e.target.value });
  };

  const handleAddTipo = (e) => {
    if (e.key === "Enter" && e.target.value.trim() !== "") {
      e.preventDefault();
      const nuevoTipo = e.target.value.toUpperCase().trim();
      if (!condominioData.tiposUnidad.includes(nuevoTipo)) {
        setCondominioData({
          ...condominioData,
          tiposUnidad: [...condominioData.tiposUnidad, nuevoTipo],
        });
      }
      e.target.value = "";
    }
  };

  const handleRemoveTipo = (tipoToRemove) => {
    setCondominioData({
      ...condominioData,
      tiposUnidad: condominioData.tiposUnidad.filter((t) => t !== tipoToRemove),
    });
  };

  const handleAddTorre = () => {
    setCondominioData({
      ...condominioData,
      torres: [
        ...condominioData.torres,
        { numero: condominioData.torres.length + 1, unidades: [] },
      ],
    });
  };

  const handleAddUnidad = (torreIndex) => {
    const nuevasTorres = [...condominioData.torres];
    nuevasTorres[torreIndex].unidades.push({ numero: "", tipoNombre: "", m2: "" });
    setCondominioData({ ...condominioData, torres: nuevasTorres });
  };

  const handleUnidadChange = (torreIndex, unidadIndex, field, value) => {
    const nuevasTorres = [...condominioData.torres];
    nuevasTorres[torreIndex].unidades[unidadIndex][field] = value;
    setCondominioData({ ...condominioData, torres: nuevasTorres });
  };

  const handleRemoveUnidad = (torreIndex, unidadIndex) => {
    const nuevasTorres = [...condominioData.torres];
    nuevasTorres[torreIndex].unidades.splice(unidadIndex, 1);
    setCondominioData({ ...condominioData, torres: nuevasTorres });
  };

  // --- ENVÍO FINAL AL BFF ---
  const procesarAltaMasiva = async () => {
    setIsSubmitting(true);
    
    // Armamos el payload final inyectando el ID del usuario
    const payloadFinal = {
      ...condominioData,
      idUsuario: idUsuarioActual
    };

    try {
      const response = await fetch("/api/bff/registro/onboarding/crear-completo", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payloadFinal),
      });

      if (response.ok) {
        const data = await response.json();
        alert(`¡Éxito! ${data.mensaje}`);
        // Limpiamos el formulario para un nuevo registro
        setCondominioData({ nombre: "", direccion: "", tiposUnidad: [], torres: [] });
        setStep(1);
      } else {
        const errData = await response.json().catch(() => ({}));
        alert(`Error: ${errData.error || "La transacción falló y se revirtió."}`);
      }
    } catch (err) {
      alert("Error crítico: El BFF no responde.");
    } finally {
      setIsSubmitting(false);
    }
  };

  // --- VALIDACIONES DE PASOS ---
  const canGoNext = () => {
    if (step === 1) return condominioData.nombre && condominioData.direccion;
    if (step === 2) return condominioData.tiposUnidad.length > 0;
    if (step === 3) return condominioData.torres.length > 0 && condominioData.torres.every(t => t.unidades.length > 0 && t.unidades.every(u => u.numero && u.tipoNombre && u.m2));
    return true;
  };

  return (
    <div className="alta-wizard-container">
      <div className="modulo-registros-header">
        <h1 className="modulo-registros-title">Onboarding de Copropiedad</h1>
        <p className="modulo-registros-subtitle">
          Declare la estructura completa de un nuevo condominio en una sola transacción segura.
        </p>
      </div>

      {/* INDICADOR DE PASOS */}
      <div className="wizard-stepper">
        {[1, 2, 3, 4].map((num) => (
          <div key={num} className={`wizard-step ${step === num ? "active" : step > num ? "completed" : ""}`}>
            <div className="step-circle">{num}</div>
            <span>
              {num === 1 ? "Básicos" : num === 2 ? "Tipologías" : num === 3 ? "Estructura" : "Confirmación"}
            </span>
          </div>
        ))}
      </div>

      <div className="registros-content-card">
        {/* PASO 1: DATOS BÁSICOS */}
        {step === 1 && (
          <div className="wizard-step-content fade-in">
            <h3 className="form-section-heading">1. Identidad del Condominio</h3>
            <div className="reg-input-group">
              <label>Nombre del Complejo:</label>
              <input type="text" name="nombre" placeholder="Ej: Condominio Las Palmas" value={condominioData.nombre} onChange={handleBasicos} />
            </div>
            <div className="reg-input-group">
              <label>Dirección Oficial:</label>
              <input type="text" name="direccion" placeholder="Ej: Av. Central 404" value={condominioData.direccion} onChange={handleBasicos} />
            </div>
          </div>
        )}

        {/* PASO 2: TIPOLOGÍAS */}
        {step === 2 && (
          <div className="wizard-step-content fade-in">
            <h3 className="form-section-heading">2. Definición de Tipologías</h3>
            <p className="step-instruction">Escriba el nombre del tipo de unidad y presione <strong>Enter</strong>.</p>
            
            <div className="reg-input-group">
              <input type="text" placeholder="Ej: DEPARTAMENTO TIPO A, ESTACIONAMIENTO..." onKeyDown={handleAddTipo} />
            </div>
            
            <div className="tags-container">
              {condominioData.tiposUnidad.length === 0 && <span className="empty-state">No hay tipologías definidas.</span>}
              {condominioData.tiposUnidad.map((tipo, idx) => (
                <div key={idx} className="tag-pill">
                  {tipo}
                  <button onClick={() => handleRemoveTipo(tipo)}>×</button>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* PASO 3: TORRES Y UNIDADES */}
        {step === 3 && (
          <div className="wizard-step-content fade-in">
            <h3 className="form-section-heading">3. Arquitectura Física</h3>
            <button className="btn-add-torre" onClick={handleAddTorre}>+ Construir Nueva Torre</button>

            {condominioData.torres.map((torre, tIdx) => (
              <div key={tIdx} className="torre-card">
                <div className="torre-header">
                  <h4>Torre {torre.numero}</h4>
                  <button className="btn-add-unidad" onClick={() => handleAddUnidad(tIdx)}>+ Añadir Unidad</button>
                </div>
                
                {torre.unidades.length === 0 ? (
                  <p className="empty-state-small">Torre vacía. Añada unidades.</p>
                ) : (
                  <div className="unidades-list">
                    {torre.unidades.map((unidad, uIdx) => (
                      <div key={uIdx} className="unidad-row">
                        <input type="number" placeholder="N° (Ej: 101)" value={unidad.numero} onChange={(e) => handleUnidadChange(tIdx, uIdx, "numero", Number(e.target.value))} />
                        
                        <select value={unidad.tipoNombre} onChange={(e) => handleUnidadChange(tIdx, uIdx, "tipoNombre", e.target.value)}>
                          <option value="">-- Seleccionar Tipo --</option>
                          {condominioData.tiposUnidad.map((t, i) => (
                            <option key={i} value={t}>{t}</option>
                          ))}
                        </select>
                        
                        <input type="number" placeholder="m² (Ej: 60.5)" value={unidad.m2} onChange={(e) => handleUnidadChange(tIdx, uIdx, "m2", parseFloat(e.target.value))} />
                        
                        <button className="btn-remove-unidad" onClick={() => handleRemoveUnidad(tIdx, uIdx)}>🗑️</button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}

        {/* PASO 4: CONFIRMACIÓN */}
        {step === 4 && (
          <div className="wizard-step-content fade-in">
            <h3 className="form-section-heading">4. Revisión Final</h3>
            <div className="summary-box">
              <p><strong>Condominio:</strong> {condominioData.nombre}</p>
              <p><strong>Dirección:</strong> {condominioData.direccion}</p>
              <p><strong>Tipologías:</strong> {condominioData.tiposUnidad.join(" | ")}</p>
              <p><strong>Estructura:</strong> {condominioData.torres.length} Torres registradas.</p>
            </div>
            <p className="warning-text">⚠️ Al confirmar, el sistema inicializará toda la estructura en la base de datos de forma transaccional.</p>
          </div>
        )}

        {/* NAVEGACIÓN DEL WIZARD */}
        <div className="wizard-navigation">
          <button className="btn-wizard secondary" disabled={step === 1} onClick={() => setStep(step - 1)}>
            ← Anterior
          </button>
          
          {step < 4 ? (
            <button className="btn-wizard primary" disabled={!canGoNext()} onClick={() => setStep(step + 1)}>
              Siguiente →
            </button>
          ) : (
            <button className="btn-wizard success" disabled={isSubmitting} onClick={procesarAltaMasiva}>
              {isSubmitting ? "Procesando..." : "✅ Confirmar e Inicializar"}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}