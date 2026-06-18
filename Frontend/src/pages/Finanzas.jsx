import { useState, useEffect } from 'react';
import '../css/Finanzas.css';

export default function Finanzas() {
  const [mesActual, setMesActual] = useState(new Date().getMonth() + 1);
  const [anioActual, setAnioActual] = useState(new Date().getFullYear());

  // Estados unificados para datos del Backend
  const [cobros, setCobros] = useState([]);
  const [tarifas, setTarifas] = useState([]);
  const [tiposUnidad, setTiposUnidad] = useState([]);

  // Estados de carga (UI)
  const [loading, setLoading] = useState(false);
  const [generando, setGenerando] = useState(false);

  // Estados para el Modal de Tarifas
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [tarifaEditando, setTarifaEditando] = useState({ idTipoUnidad: '', monto: '' });

  useEffect(() => {
    cargarDatos();
  }, [mesActual, anioActual]);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const resCobros = await fetch(`/api/bff/contabilidad/cobros-detallados?mes=${mesActual}&anio=${anioActual}`);
      if (resCobros.ok) setCobros(await resCobros.json());

      const resTipos = await fetch(`/api/bff/registro/tipos-unidad`);
      if (resTipos.ok) setTiposUnidad(await resTipos.json());

      const resTarifas = await fetch(`/api/bff/contabilidad/tarifas`);
      if (resTarifas.ok) setTarifas(await resTarifas.json());

    } catch (error) {
      console.error("Error cargando datos financieros:", error);
    } finally {
      setLoading(false);
    }
  };

  const generarBoletasMes = async () => {
    if (!window.confirm(`¿Estás seguro de generar todas las deudas para el mes ${mesActual}/${anioActual}?`)) return;

    setGenerando(true);
    try {
      const response = await fetch(`/api/bff/contabilidad/generar-mes?mes=${mesActual}&anio=${anioActual}`, {
        method: 'POST'
      });

      if (response.ok) {
        alert("¡Boletas generadas con éxito!");
        cargarDatos(); // Recargar la tabla para ver los nuevos cobros
      } else {
        const errorData = await response.json().catch(() => ({}));
        alert(`Hubo un problema: ${errorData.error || "Error desconocido del servidor"}`);
      }
    } catch (error) {
      console.error("Error al generar boletas:", error);
      alert("Error de conexión al servidor.");
    } finally {
      setGenerando(false);
    }
  };

  const marcarComoPagado = async (idCobro) => {
    try {
      const response = await fetch(`/api/bff/contabilidad/cobros/${idCobro}/estado?estado=PAGADO`, {
        method: 'PATCH'
      });

      if (response.ok) {
        setCobros(cobros.map(c =>
            c.idCobro === idCobro ? { ...c, estado: 'PAGADO' } : c
        ));
      }
    } catch (error) {
      console.error("Error al registrar pago:", error);
    }
  };

  const abrirModalTarifa = () => {
    setTarifaEditando({ idTipoUnidad: '', monto: '' });
    setIsModalOpen(true);
  };

  const guardarTarifa = async () => {
    if (!tarifaEditando.idTipoUnidad || !tarifaEditando.monto) {
      alert("Por favor selecciona un tipo de unidad e ingresa un monto.");
      return;
    }

    try {
      const response = await fetch('/api/bff/contabilidad/tarifas', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          idTipoUnidad: parseInt(tarifaEditando.idTipoUnidad),
          monto: parseFloat(tarifaEditando.monto)
        })
      });

      if (response.ok) {
        alert("¡Tarifa actualizada correctamente!");
        setIsModalOpen(false);
        cargarDatos();
      } else {
        const errorData = await response.json().catch(() => ({}));
        alert(`Error al guardar tarifa: ${errorData.error || "Error del servidor"}`);
      }
    } catch (error) {
      console.error("Error al guardar tarifa:", error);
      alert("Error de conexión al servidor.");
    }
  };

return (
    <div className="finanzas-container">
      <div className="page-title">Gestión de Finanzas y Cobros</div>

      {/* --- SECCIÓN 1: CONFIGURACIÓN DE TARIFAS --- */}
      <div className="finanzas-dashboard-card tarifas-section-card">
        <h3 className="tarifas-section-title">1. Tarifas Base (Gastos Comunes)</h3>
        <p className="tarifas-section-subtitle">Configura el valor a cobrar por cada tipo de unidad antes de generar las boletas.</p>

        <div className="tarifas-chips-row">

          {tiposUnidad.length === 0 ? (
            <span className="tarifas-loading-placeholder">Cargando tipos de unidad...</span>
          ) : (
            tiposUnidad.map((tipo) => {
              // Buscamos si el microservicio nos mandó una tarifa para este tipo
              const tarifaEncontrada = tarifas.find(t => t.idTipoUnidad === tipo.id);
              // Si no hay, le ponemos 0
              const montoMostrar = tarifaEncontrada ? tarifaEncontrada.monto : 0;

              return (
                <div key={tipo.id} className="tarifa-chip">
                  <strong>{tipo.nombre}:</strong> ${montoMostrar.toLocaleString('es-CL')}
                </div>
              );
            })
          )}

          <button className="btn-finanzas btn-finanzas-success btn-finanzas-blue btn-add-tarifa" onClick={abrirModalTarifa}>
            + Modificar Tarifas
          </button>
        </div>
      </div>

      {/* --- SECCIÓN 2: GENERACIÓN DE BOLETAS MASIVAS --- */}
      <div className="finanzas-dashboard-card action-trigger-card">
        <div className="action-card-text">
          <h3 className="card-section-title">2. Emisión en Lote de Gastos Comunes</h3>
          <p className="card-section-subtitle">
            Dispara el cobro automático para todas las unidades correspondientes al periodo <strong>{mesActual}/{anioActual}</strong>.
          </p>
        </div>
        <button
          className="btn-finanzas btn-finanzas-success massive-action-btn"
          onClick={generarBoletasMes}
          disabled={generando}
        >
          {generando ? 'Emitiendo Boletas...' : 'Generar Boletas del Mes'}
        </button>
      </div>

      {/* --- SECCIÓN 3: REGISTRO GENERAL DE COBROS --- */}
      <div className="finanzas-dashboard-card table-card">
        <h3 className="card-section-title">3. Estado Analítico de Cuentas</h3>

        {loading ? (
          <div className="table-loading-spinner">Sincronizando deudas con el libro contable...</div>
        ) : (
          <div className="finanzas-table-responsive">
            <table className="finanzas-data-table">
              <thead>
                <tr>
                  <th>Condominio</th>
                  <th>Torre</th>
                  <th>Nº Unidad</th>
                  <th>Tipo</th>
                  <th>Residente</th>
                  <th>Monto Cobrado</th>
                  <th>Estado</th>
                  <th>Operaciones</th>
                </tr>
              </thead>
              <tbody>
                {cobros.length === 0 ? (
                  <tr>
                    <td colSpan="8" className="table-empty-fallback">
                      No se han emitido cobros o deudas para este periodo mensual.
                    </td>
                  </tr>
                ) : (
                  cobros.map((cobro) => (
                    <tr key={cobro.idCobro}>
                      <td>{cobro.nombreCondominio}</td>
                      <td>Torre {cobro.numeroTorre}</td>
                      <td className="fw-bold">{cobro.numeroUnidad}</td>
                      <td><span className="type-tag">{cobro.tipoUnidad}</span></td>
                      <td>{cobro.nombreInquilino || "Sin Residente Asignado"}</td>
                      <td className="fw-bold text-money">${cobro.monto.toLocaleString('es-CL')}</td>
                      <td>
                        <span className={`status-pill pill-${cobro.estado.toLowerCase()}`}>
                          {cobro.estado}
                        </span>
                      </td>
                      <td>
                        {cobro.estado === 'PENDIENTE' && (
                          <button className="btn-table-action btn-pay-now" onClick={() => marcarComoPagado(cobro.idCobro)}>
                            ✓ Marcar Pagado
                          </button>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* --- MODAL DE TARIFAS (Limpio y unificado) --- */}
      {isModalOpen && (
        <div className="modal-finanzas-overlay">
          <div className="modal-finanzas-box">
            <h3 className="modal-finanzas-title">Actualizar Tarifa</h3>
            <p className="modal-finanzas-text">Esta tarifa se aplicará a todas las boletas que se generen a partir de ahora.</p>

            <div className="modal-form-group">
              <label>Tipo de Unidad Destino:</label>
              <select
                className="modal-select modal-select-spaced"
                value={tarifaEditando.idTipoUnidad}
                onChange={(e) => setTarifaEditando({ ...tarifaEditando, idTipoUnidad: e.target.value })}
              >
                <option value="">Seleccione el tipo...</option>
                {tiposUnidad.map((tipo) => (
                  <option key={tipo.id} value={tipo.id}>{tipo.nombre}</option>
                ))}
              </select>
            </div>

            <div className="modal-form-group">
              <label>Monto Mensual ($):</label>
              <input
                type="number"
                className="modal-input modal-input-spaced"
                placeholder="Ej: 65000"
                value={tarifaEditando.monto}
                onChange={(e) => setTarifaEditando({ ...tarifaEditando, monto: e.target.value })}
              />
            </div>

            <div className="modal-actions-row modal-actions-row-spaced">
              <button className="btn-finanzas btn-finanzas-secondary btn-modal-cancel" onClick={() => setIsModalOpen(false)}>
                Cancelar
              </button>
              <button className="btn-finanzas btn-finanzas-primary btn-modal-confirm" onClick={guardarTarifa}>
                Confirmar Tarifa
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}