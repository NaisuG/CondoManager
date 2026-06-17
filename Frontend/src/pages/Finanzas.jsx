import { useState, useEffect } from 'react';

export default function Finanzas() {
  const [mesActual, setMesActual] = useState(new Date().getMonth() + 1);
  const [anioActual, setAnioActual] = useState(new Date().getFullYear());
  
  // Estados para datos del Backend
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
      // 1. Cargar Cobros
      const resCobros = await fetch(`http://localhost:9000/api/bff/contabilidad/cobros-detallados?mes=${mesActual}&anio=${anioActual}`);
      if (resCobros.ok) setCobros(await resCobros.json());

      const resTipos = await fetch(`http://localhost:9000/api/bff/registro/tipos-unidad`);
      if (resTipos.ok) setTiposUnidad(await resTipos.json());

    } catch (error) {
      console.error("Error cargando datos:", error);
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

  // --- NUEVA FUNCIÓN CONECTADA ---
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
      console.error("Error al pagar:", error);
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
          <div style={{ padding: '10px', background: '#f8f9fa', borderRadius: '5px', border: '1px solid #ddd' }}>
             <strong>Departamento:</strong> $50.000 
          </div>
          <div style={{ padding: '10px', background: '#f8f9fa', borderRadius: '5px', border: '1px solid #ddd' }}>
             <strong>Bodega:</strong> $10.000 
          </div>
          <button className="btn-primary" onClick={abrirModalTarifa} style={{ marginLeft: 'auto' }}>
            + Modificar Tarifas
          </button>
        </div>
      </div>

      {/* --- SECCIÓN 2: GENERACIÓN DE BOLETAS --- */}
      <div className="dashboard-card" style={{ marginBottom: '20px', padding: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h3>2. Generación Mensual</h3>
          <p style={{ color: '#666', fontSize: '14px' }}>Genera las deudas para todos los departamentos correspondientes a <strong>Mes {mesActual} / Año {anioActual}</strong>.</p>
        </div>
        <button 
          className="btn-success" 
          onClick={generarBoletasMes}
          disabled={generando}
          style={{ padding: '12px 24px', fontSize: '16px', opacity: generando ? 0.7 : 1, cursor: generando ? 'not-allowed' : 'pointer' }}
        >
          {generando ? 'Generando...' : 'Generar Boletas del Mes'}
        </button>
      </div>

      {/* --- SECCIÓN 3: TABLA DE GESTIÓN --- */}
      <div className="dashboard-card" style={{ padding: '20px' }}>
        <h3>3. Estado de Cobros</h3>
        
        {loading ? (
          <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>Cargando información...</div>
        ) : (
          <table style={{ width: '100%', marginTop: '15px', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #eee' }}>
                <th style={{ padding: '10px' }}>Condominio</th>
                <th style={{ padding: '10px' }}>Torre</th>
                <th style={{ padding: '10px' }}>Depto</th>
                <th style={{ padding: '10px' }}>Tipo</th>
                <th style={{ padding: '10px' }}>Inquilino</th>
                <th style={{ padding: '10px' }}>Monto</th>
                <th style={{ padding: '10px' }}>Estado</th>
                <th style={{ padding: '10px' }}>Acción</th>
              </tr>
            </thead>
            <tbody>
              {cobros.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ textAlign: 'center', padding: '20px', color: '#888' }}>
                    No hay cobros generados para este mes.
                  </td>
                </tr>
              ) : (
                cobros.map((cobro) => (
                  <tr key={cobro.idCobro} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '10px' }}>{cobro.nombreCondominio}</td>
                    <td style={{ padding: '10px' }}>{cobro.numeroTorre}</td>
                    <td style={{ padding: '10px' }}>{cobro.numeroUnidad}</td>
                    <td style={{ padding: '10px' }}>{cobro.tipoUnidad}</td>
                    <td style={{ padding: '10px' }}>{cobro.nombreInquilino}</td>
                    <td style={{ padding: '10px' }}>${cobro.monto.toLocaleString('es-CL')}</td>
                    <td style={{ padding: '10px' }}>
                      <span style={{ 
                        padding: '4px 8px', borderRadius: '12px', fontSize: '12px', fontWeight: 'bold',
                        backgroundColor: cobro.estado === 'PAGADO' ? '#c6f6d5' : cobro.estado === 'VENCIDO' ? '#fed7d7' : '#feebc8',
                        color: cobro.estado === 'PAGADO' ? '#22543d' : cobro.estado === 'VENCIDO' ? '#822727' : '#7b341e'
                      }}>
                        {cobro.estado}
                      </span>
                    </td>
                    <td style={{ padding: '10px' }}>
                      {cobro.estado === 'PENDIENTE' && (
                        <button style={{ padding: '5px 10px', background: '#48bb78', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }} 
                                onClick={() => marcarComoPagado(cobro.idCobro)}>
                          ✓ Pagar
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>

      {/* --- MODAL (ALERT) DE TARIFAS --- */}
      {isModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, 
          backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000
        }}>
          <div style={{ background: 'white', padding: '30px', borderRadius: '8px', width: '400px', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}>
            <h3 style={{ marginTop: 0 }}>Actualizar Tarifa</h3>
            <p style={{ fontSize: '14px', color: '#666' }}>Esta tarifa se aplicará a todas las boletas que se generen a partir de ahora.</p>
            
            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Tipo de Unidad:</label>
              <select style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                      value={tarifaEditando.idTipoUnidad} 
                      onChange={(e) => setTarifaEditando({...tarifaEditando, idTipoUnidad: e.target.value})}>
                <option value="">Seleccione un tipo...</option>
                <option value="1">Departamento</option>
                <option value="2">Bodega</option>
              </select>
            </div>

            <div style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Monto a Cobrar ($):</label>
              <input type="number" style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                     value={tarifaEditando.monto} 
                     onChange={(e) => setTarifaEditando({...tarifaEditando, monto: e.target.value})} />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
              <button style={{ padding: '8px 15px', background: '#e2e8f0', border: 'none', borderRadius: '4px', cursor: 'pointer' }} 
                      onClick={() => setIsModalOpen(false)}>Cancelar</button>
              <button style={{ padding: '8px 15px', background: '#3182ce', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }} 
                      onClick={guardarTarifa}>Confirmar Cambio</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}