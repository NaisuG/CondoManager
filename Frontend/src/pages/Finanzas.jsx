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

  const [modalPagoOpen, setModalPagoOpen] = useState(false);
  const [cobroSeleccionado, setCobroSeleccionado] = useState(null);
  const [comprobanteFile, setComprobanteFile] = useState(null);

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

  const abrirModalPago = (cobro) => {
  setCobroSeleccionado(cobro);
  setModalPagoOpen(true);
};

const ejecutarPagoConArchivo = async (e) => {
  e.preventDefault();
  if (!comprobanteFile) return alert("El archivo del comprobante es obligatorio.");

  const periodo = `${mesActual}-${anioActual}`;
  const formData = new FormData();
  formData.append("archivo", comprobanteFile);
  formData.append("idCondominio", cobroSeleccionado.idCondominio || 1); // fallback
  formData.append("idUsuarioSubio", localStorage.getItem("idUsuario") || 1);
  formData.append("periodo", periodo);

  try {
    const res = await fetch(`/api/bff/contabilidad/cobros/${cobroSeleccionado.idCobro}/pagar`, {
      method: 'POST',
      body: formData
    });
    if (res.ok) {
      alert("Pago registrado y comprobante archivado.");
      setModalPagoOpen(false);
      cargarDatos(); // Recarga la grilla contable
    }
  } catch (err) {
    console.error(err);
  }
};

  const solicitarReversion = async (cobro) => {
    if (!window.confirm('¿Desea cambiar el estado a "Pendiente"? El comprobante se desvinculará.')) return;

    try {
      const res = await fetch(`/api/bff/contabilidad/cobros/${cobro.idCobro}/revertir`, {
        method: 'PATCH'
      });
      if (res.ok) {
        alert("Estado revertido a PENDIENTE de manera exitosa.");
        cargarDatos(); // Sincroniza la tabla y los analytics del dashboard automáticamente
      }
    } catch (err) {
      console.error(err);
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
      <div className="dashboard-card" style={{ marginBottom: '20px', padding: '20px' }}>
        <h3>1. Tarifas Base (Gastos Comunes)</h3>
        <p style={{ color: '#666', fontSize: '14px' }}>Configura el valor a cobrar por cada tipo de unidad antes de generar las boletas.</p>

        <div style={{ display: 'flex', gap: '10px', alignItems: 'center', marginTop: '15px' }}>

          {tiposUnidad.length === 0 ? (
            <span style={{ color: '#888' }}>Cargando tipos de unidad...</span>
          ) : (
            tiposUnidad.map((tipo) => {
              // Buscamos si el microservicio nos mandó una tarifa para este tipo
              const tarifaEncontrada = tarifas.find(t => t.idTipoUnidad === tipo.id);
              // Si no hay, le ponemos 0
              const montoMostrar = tarifaEncontrada ? tarifaEncontrada.monto : 0;

              return (
                <div key={tipo.id} style={{ padding: '10px', background: '#f8f9fa', borderRadius: '5px', border: '1px solid #ddd' }}>
                  <strong>{tipo.nombre}:</strong> ${montoMostrar.toLocaleString('es-CL')}
                </div>
              );
            })
          )}

          <button className="btn-primary" onClick={abrirModalTarifa} style={{ marginLeft: 'auto' }}>
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
                            <button className="btn-table-action btn-pay-now" onClick={() => abrirModalPago(cobro)}>
                              ✓ Marcar Pagado
                            </button>
                          )}
                          {cobro.estado === 'PAGADO' && (
                            <button className="btn-table-action" onClick={() => solicitarReversion(cobro)} style={{ background: '#fed7d7', color: '#9b2c2c', border: '1px solid #feb2b2' }}>
                              ↺ Revertir a Pendiente
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

      {/* --- MODAL DE TARIFAS--- */}
      {isModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000
        }}>
          <div style={{ background: 'white', padding: '30px', borderRadius: '8px', width: '400px', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}>
            <h3 style={{ marginTop: 0 }}>Actualizar Tarifa</h3>
            <p style={{ fontSize: '14px', color: '#666' }}>Esta tarifa se aplicará a todas las boletas que se generen a partir de ahora.</p>

            <div className="modal-form-group">
              <label>Tipo de Unidad Destino:</label>
              <select
                className="modal-select"
                value={tarifaEditando.idTipoUnidad}
                onChange={(e) => setTarifaEditando({ ...tarifaEditando, idTipoUnidad: e.target.value })}
                style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc', marginBottom: '15px' }}
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
                className="modal-input"
                placeholder="Ej: 65000"
                value={tarifaEditando.monto}
                onChange={(e) => setTarifaEditando({ ...tarifaEditando, monto: e.target.value })}
                style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc', marginBottom: '20px' }}
              />
            </div>

            <div className="modal-actions-row" style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
              <button className="btn-finanzas btn-finanzas-secondary" onClick={() => setIsModalOpen(false)} style={{ padding: '8px 15px', background: '#e2e8f0', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                Cancelar
              </button>
              <button className="btn-finanzas btn-finanzas-primary" onClick={guardarTarifa} style={{ padding: '8px 15px', background: '#3182ce', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                Confirmar Tarifa
              </button>
            </div>
          </div>
        </div>
      )}

        {/* --- MODAL DE PAGO CON COMPROBANTE --- */}
      {modalPagoOpen && cobroSeleccionado && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000
        }}>
          <div style={{ background: 'white', padding: '30px', borderRadius: '8px', width: '400px', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}>
            <h3 style={{ marginTop: 0 }}>Registrar Pago</h3>
            <p style={{ fontSize: '14px', color: '#666' }}>
              Suba el comprobante de transferencia para la Unidad {cobroSeleccionado.numeroUnidad}.
            </p>

            <form onSubmit={ejecutarPagoConArchivo}>
              <div className="modal-form-group">
                <label>Archivo del Comprobante:</label>
                <input
                  type="file"
                  className="modal-input"
                  onChange={(e) => setComprobanteFile(e.target.files[0])}
                  required
                  style={{ width: '100%', padding: '8px', marginBottom: '20px' }}
                />
              </div>

              <div className="modal-actions-row" style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button type="button" className="btn-finanzas btn-finanzas-secondary" onClick={() => setModalPagoOpen(false)} style={{ padding: '8px 15px', background: '#e2e8f0', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                  Cancelar
                </button>
                <button type="submit" className="btn-finanzas btn-finanzas-success" style={{ padding: '8px 15px', background: '#38a169', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                  Subir y Pagar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}