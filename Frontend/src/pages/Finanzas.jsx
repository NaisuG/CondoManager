import { useState, useEffect } from 'react';
import '../css/Finanzas.css';

export default function Finanzas() {
  const [mesActual, setMesActual] = useState(new Date().getMonth() + 1);
  const [anioActual, setAnioActual] = useState(new Date().getFullYear());

  // Estados unificados para datos del Backend
  const [cobros, setCobros] = useState([]);
  const [tarifas, setTarifas] = useState([]);
  const [tiposUnidad, setTiposUnidad] = useState([]);
  const [condominios, setCondominios] = useState([]); // <-- Agregado para el cruce de IDs

  // Estados de carga (UI)
  const [loading, setLoading] = useState(false);
  const [generando, setGenerando] = useState(false);

  // Estados para el Modal de Tarifas
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [tarifaEditando, setTarifaEditando] = useState({ idTipoUnidad: '', monto: '' });

  // Estados para el Modal de Pago
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

      // Cargamos el catálogo de condominios
      const resCondominios = await fetch(`/api/bff/registro/condominios`);
      if (resCondominios.ok) setCondominios(await resCondominios.json());

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
    setComprobanteFile(null); // Limpiar archivo previo
    setModalPagoOpen(true);
  };

  const ejecutarPagoConArchivo = async (e) => {
    e.preventDefault();
    if (!comprobanteFile) return alert("El archivo del comprobante es obligatorio.");

    // 1. Rescatamos el nombre que sabemos que envía ms-registro
    const nombreCondo = cobroSeleccionado.nombreCondominio;

    if (!nombreCondo || nombreCondo === 'Desconocido' || nombreCondo === 'N/A') {
      alert("Error crítico: No se pudo identificar el nombre del condominio de esta deuda.");
      return;
    }

    // 2. Buscamos el ID numérico en nuestro catálogo cruzando por el nombre
    const condominioMatch = condominios.find(c => c.nombre === nombreCondo);
    const idCondominioReal = condominioMatch ? condominioMatch.id : null;

    if (!idCondominioReal) {
      alert(`Error interno: No se pudo resolver el ID para el condominio "${nombreCondo}".`);
      return;
    }

    // 3. Formateamos el texto para que sea una carpeta válida en MinIO
    const folderName = nombreCondo.toLowerCase().replace(/\s+/g, '-');
    const periodo = `${mesActual}-${anioActual}`;

    // 4. Armamos el envío: El número para Postgres y el Texto para MinIO
    const formData = new FormData();
    formData.append("archivo", comprobanteFile);
    formData.append("idCondominio", idCondominioReal);
    formData.append("nombreCarpeta", folderName);
    formData.append("idUsuarioSubio", localStorage.getItem("idUsuario") || 1);
    formData.append("periodo", periodo);

    try {
      const res = await fetch(`/api/bff/contabilidad/cobros/${cobroSeleccionado.idCobro}/pagar`, {
        method: 'POST',
        body: formData
      });
      
      if (res.ok) {
        alert("Pago registrado y comprobante archivado correctamente.");
        setModalPagoOpen(false);
        cargarDatos(); 
      } else if (res.status === 413) {
        alert("El archivo es demasiado pesado. El tamaño máximo permitido es de 15MB.");
      } else {
        const errData = await res.json().catch(() => ({}));
        alert(`Error al registrar el pago: ${errData.error || "Error del servidor"}`);
      }
    } catch (err) {
      console.error("Error en la subida:", err);
      if (err.message.includes('Failed to fetch') || err.message === 'Network Error') {
        alert("Error de conexión: El archivo excede el límite permitido o se perdió la red.");
      } else {
        alert("Ocurrió un error inesperado al subir el archivo.");
      }
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
        cargarDatos(); 
      }
    } catch (err) {
      console.error("Error en reversión:", err);
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
              const tarifaEncontrada = tarifas.find(t => t.idTipoUnidad === tipo.id);
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

      {/* --- MODAL DE TARIFAS --- */}
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

      {/* --- MODAL DE PAGO CON COMPROBANTE --- */}
      {modalPagoOpen && cobroSeleccionado && (
        <div className="modal-finanzas-overlay">
          <div className="modal-finanzas-box">
            <h3 className="modal-finanzas-title">Registrar Pago</h3>
            <p className="modal-finanzas-text">
              Suba el comprobante de transferencia para la Unidad {cobroSeleccionado.numeroUnidad}.
            </p>

            <form onSubmit={ejecutarPagoConArchivo}>
              <div className="modal-form-group">
                <label>Archivo del Comprobante:</label>
                <input
                  type="file"
                  className="modal-input modal-input-spaced"
                  onChange={(e) => setComprobanteFile(e.target.files[0])}
                  required
                />
              </div>

              <div className="modal-actions-row modal-actions-row-spaced">
                <button type="button" className="btn-finanzas btn-finanzas-secondary btn-modal-cancel" onClick={() => setModalPagoOpen(false)}>
                  Cancelar
                </button>
                <button type="submit" className="btn-finanzas btn-finanzas-success btn-modal-confirm">
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