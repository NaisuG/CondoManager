import { useEffect, useRef, useState } from 'react';
import Chart from 'chart.js/auto';
import '../css/Dashboard.css'; // Importación obligatoria del CSS independiente

// Paleta híbrida: azul corporativo como ancla + acentos multicolor para categorías
const colores = ['#0c447c', '#34d399', '#fbbf24', '#a78bfa', '#60a5fa', '#f87171', '#3066be'];

const formatMoneda = (valor) => {
  if (valor === null || valor === undefined) return '$0';
  return '$' + valor.toLocaleString('es-CL');
};

// Gráfico de barras horizontal (Chart.js) — reemplaza el canvas dibujado a mano.
// Usado para "Torres por condominio" y "Unidades por torre".
function BarChart({ labels, valores, titulo }) {
  const canvasRef = useRef(null);
  const chartInstance = useRef(null);

  useEffect(() => {
    if (!canvasRef.current || !labels.length) return;
    if (chartInstance.current) chartInstance.current.destroy();

    chartInstance.current = new Chart(canvasRef.current, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          data: valores,
          backgroundColor: labels.map((_, i) => colores[i % colores.length]),
          borderRadius: 6,
          maxBarThickness: 28,
        }],
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#111827',
            padding: 10,
            cornerRadius: 8,
          },
        },
        scales: {
          x: {
            beginAtZero: true,
            ticks: { precision: 0, color: '#6b7280', font: { size: 11 } },
            grid: { color: '#f3f4f6' },
          },
          y: {
            ticks: { color: '#4b5563', font: { size: 12, weight: '600' } },
            grid: { display: false },
          },
        },
      },
    });

    return () => chartInstance.current?.destroy();
  }, [labels, valores]);

  return (
      <div className="chart-card">
        <h3>{titulo}</h3>
        <div className="chart-wrapper">
          <canvas ref={canvasRef} />
        </div>
      </div>
  );
}

// Dona con total al centro (Chart.js) — usado para distribución por tipo de unidad
function DonaChart({ labels, valores, titulo, centroLabel }) {
  const canvasRef = useRef(null);
  const chartInstance = useRef(null);
  const total = valores.reduce((a, b) => a + b, 0);

  useEffect(() => {
    if (!canvasRef.current || !labels.length) return;
    if (chartInstance.current) chartInstance.current.destroy();

    chartInstance.current = new Chart(canvasRef.current, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [{
          label: 'Total',
          data: valores,
          backgroundColor: colores,
          borderWidth: 0,
          hoverOffset: 6,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '72%',
        plugins: {
          legend: {
            position: 'right',
            labels: { boxWidth: 12, font: { size: 12, weight: '600' }, color: '#4b5563' }
          },
          tooltip: {
            backgroundColor: '#111827',
            padding: 10,
            cornerRadius: 8,
          },
        },
      },
      // Plugin inline para dibujar el total en el centro de la dona
      plugins: [{
        id: 'centroTexto',
        afterDraw(chart) {
          const { ctx, chartArea } = chart;
          const cx = (chartArea.left + chartArea.right) / 2;
          const cy = (chartArea.top + chartArea.bottom) / 2;
          ctx.save();
          ctx.textAlign = 'center';
          ctx.textBaseline = 'middle';
          ctx.fillStyle = '#111827';
          ctx.font = '700 26px sans-serif';
          ctx.fillText(total, cx, cy - 10);
          ctx.fillStyle = '#6b7280';
          ctx.font = '600 11px sans-serif';
          ctx.fillText(centroLabel || 'Total', cx, cy + 14);
          ctx.restore();
        }
      }],
    });

    return () => chartInstance.current?.destroy();
  }, [labels, valores, centroLabel]);

  return (
      <div className="chart-card chart-card-full-width">
        <h3>{titulo}</h3>
        <div className="chart-wrapper" style={{ minHeight: '260px' }}>
          <canvas ref={canvasRef} />
        </div>
      </div>
  );
}

// Dashboard Principal
export default function Dashboard() {
  const [tipoLabels, setTipoLabels] = useState([]);
  const [tipoValores, setTipoValores] = useState([]);
  const [condominios, setCondominios] = useState([]);
  const [finanzas, setFinanzas] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTodo = async () => {
      try {
        // 1. Estadísticas por tipo de unidad
        const r1 = await fetch('/api/bff/registro/estadisticas/unidades');
        const datos = await r1.json();
        setTipoLabels(Object.keys(datos));
        setTipoValores(Object.values(datos));

        // 2. Condominios con sus torres correspondientes
        const r2 = await fetch('/api/bff/registro/condominios');
        const condos = await r2.json();
        setCondominios(condos);

        // 3. Estadísticas financieras del mes actual
        const fecha = new Date();
        const mesActual = fecha.getMonth() + 1;
        const anioActual = fecha.getFullYear();

        const r3 = await fetch(`/api/bff/contabilidad/estadisticas?mes=${mesActual}&anio=${anioActual}`);
        if (r3.ok) {
          const datosFinanzas = await r3.json();
          setFinanzas(datosFinanzas);
        }
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    };
    fetchTodo();
  }, []);

  if (loading) {
    return <div className="dashboard-loading-state">Cargando estadísticas de la base de datos...</div>;
  }

  if (error) {
    return <div className="dashboard-error-state">Error al sincronizar datos: {error}</div>;
  }

  // Cálculos de arreglos idénticos a tu archivo original
  const condoLabels = condominios.map(c => c.nombre);
  const condoTorres = condominios.map(c => c.torres?.length || 0);

  const torresLabels = condominios.flatMap(c =>
      (c.torres || []).map(t => `${c.nombre} – T.${t.numero}`)
  );
  const torresUnidades = condominios.flatMap(c =>
      (c.torres || []).map(t => t.unidades?.length || 0)
  );

  const totalUnidades = tipoValores.reduce((a, b) => a + b, 0);
  const tipoMasFrecuente = tipoLabels[tipoValores.indexOf(Math.max(...tipoValores))] || '—';

  return (
      <div className="dashboard-container">
        <div className="page-title" style={{ fontSize: '1.5rem', fontWeight: '700', color: '#0c447c' }}>
          Estadísticas Generales del Condominio
        </div>

        {/* --- BLOQUE METRICAS 1: ESTRUCTURA FISICA --- */}
        <div className="dashboard-metrics-row">
          <div className="metric-card">
            <div className="metric-header">
              <span className="metric-title">Condominios</span>
            </div>
            <div className="metric-value">{condominios.length}</div>
          </div>

          <div className="metric-card">
            <div className="metric-header">
              <span className="metric-title">Torres Totales</span>
            </div>
            <div className="metric-value">{torresLabels.length}</div>
          </div>

          <div className="metric-card">
            <div className="metric-header">
              <span className="metric-title">Unidades Totales</span>
            </div>
            <div className="metric-value">{totalUnidades}</div>
          </div>

          <div className="metric-card">
            <div className="metric-header">
              <span className="metric-title">Tipo más Frecuente</span>
            </div>
            <div className="metric-value" style={{ fontSize: '1.35rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {tipoMasFrecuente}
            </div>
          </div>
        </div>

        {/* --- BLOQUE METRICAS 2: CONTABILIDAD Y FINANZAS --- */}
        {finanzas && (
            <div className="dashboard-metrics-row">
              <div className="metric-card">
                <div className="metric-header">
                  <span className="metric-title">Proyectado Mensual</span>
                </div>
                <div className="metric-value">{formatMoneda(finanzas.totalProyectado)}</div>
              </div>

              <div className="metric-card">
                <div className="metric-header">
                  <span className="metric-title">Recaudado</span>
                </div>
                <div className="metric-value success-color">
                  {formatMoneda(finanzas.totalRecaudado)}
                </div>
              </div>

              <div className="metric-card">
                <div className="metric-header">
                  <span className="metric-title">Morosidad</span>
                </div>
                <div className={`metric-value ${finanzas.tasaMorosidad > 0 ? 'danger-color' : ''}`}>
                  {finanzas.tasaMorosidad}%
                </div>
              </div>

              <div className="metric-card">
                <div className="metric-header">
                  <span className="metric-title">Unidades Impagas</span>
                </div>
                <div className="metric-value">{finanzas.cantidadPendientes}</div>
              </div>
            </div>
        )}

        {/* --- GRILLA INFERIOR DE REPORTES GRÁFICOS --- */}
        <div className="dashboard-charts-grid">
          <BarChart
              labels={condoLabels}
              valores={condoTorres}
              titulo="Torres por condominio"
          />

          <BarChart
              labels={torresLabels}
              valores={torresUnidades}
              titulo="Unidades por torre"
          />

          <DonaChart
              labels={tipoLabels}
              valores={tipoValores}
              titulo="Distribución por tipo de unidad"
              centroLabel="Unidades"
          />
        </div>
      </div>
  );
}