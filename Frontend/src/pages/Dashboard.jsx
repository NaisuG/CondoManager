import { useEffect, useRef, useState } from 'react';
import Chart from 'chart.js/auto';
import '../css/Dashboard.css';

const colores = ['#9d99ca', '#fca5e2', '#dad37b', '#abe6d2', '#fd7979', '#2d8bb1', '#e9a36a'];

//grafico dee barras radial
function RadialChart({ labels, valores, titulo }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || !labels.length) return;
    const ctx = canvas.getContext('2d');
    const W = canvas.width, H = canvas.height;
    ctx.clearRect(0, 0, W, H);

    const cx = W / 2;
    const cy = H / 2 + 20;
    const maxR = Math.min(W, H) / 2 - 35;
    const stroke = 13;
    const gap = 16;
    const maxVal = Math.max(...valores);

    labels.forEach((label, i) => {
      const r = maxR - i * (stroke + gap);
      if (r < 16) return;

      const fraccion = valores[i] / maxVal;
      const startAngle = -Math.PI / 2;
      const endAngle = startAngle + fraccion * 2 * Math.PI * 0.82;

      // fondo gris
      ctx.beginPath();
      ctx.arc(cx, cy, r, -Math.PI / 2, -Math.PI / 2 + 2 * Math.PI * 0.82);
      ctx.strokeStyle = '#eee';
      ctx.lineWidth = stroke;
      ctx.lineCap = 'round';
      ctx.stroke();

      // arco color
      ctx.beginPath();
      ctx.arc(cx, cy, r, startAngle, endAngle);
      ctx.strokeStyle = colores[i % colores.length];
      ctx.lineWidth = stroke;
      ctx.lineCap = 'round';
      ctx.stroke();

      // valor al final
      const lx = cx + (r + stroke + 4) * Math.cos(endAngle);
      const ly = cy + (r + stroke + 4) * Math.sin(endAngle);
      ctx.fillStyle = colores[i % colores.length];
      ctx.font = 'bold 12px system-ui';
      ctx.textAlign = 'center';
      ctx.fillText(valores[i], lx, ly);
    });

    // leyenda arriba izquierda
    labels.forEach((label, i) => {
      ctx.fillStyle = colores[i % colores.length];
      ctx.fillRect(10, 10 + i * 20, 11, 11);
      ctx.fillStyle = '#444';
      ctx.font = '11px system-ui';
      ctx.textAlign = 'left';
      ctx.fillText(label, 26, 20 + i * 20);
    });
  }, [labels, valores]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%' }}>
      {titulo && <div className="chart-label">{titulo}</div>}
      <canvas
        ref={canvasRef}
        width={320}
        height={320}
        style={{ width: '100%', maxWidth: 320 }}
      />
    </div>
  );
}

//Dona 
function DonaChart({ labels, valores }) {
  const canvasRef = useRef(null);
  const chartInstance = useRef(null);

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
          hoverOffset: 4,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom' } },
      },
    });

    return () => chartInstance.current?.destroy();
  }, [labels, valores]);

  return (
    <div style={{ position: 'relative', height: 260 }}>
      <canvas ref={canvasRef} />
    </div>
  );
}

//Dashboard
export default function Dashboard() {
  // estadisticas por tipo
  const [tipoLabels, setTipoLabels] = useState([]);
  const [tipoValores, setTipoValores] = useState([]);

  // condominios completos
  const [condominios, setCondominios] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTodo = async () => {
      try {
        // estadisticas por tipo de unidad
        const r1 = await fetch('/api/bff/registro/estadisticas/unidades');
        const datos = await r1.json();
        setTipoLabels(Object.keys(datos));
        setTipoValores(Object.values(datos));

        // todos los condominios con torres
        const r2 = await fetch('/api/bff/registro/condominios');
        const condos = await r2.json();
        setCondominios(condos);
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    };
    fetchTodo();
  }, []);

  // g.radial por condominio => cantidad de torres
  const condoLabels = condominios.map(c => c.nombre);
  const condoTorres = condominios.map(c => c.torres?.length || 0);

  // g.radial por torre => cantidad de unidades totales
  // ej: "Condominio A - Torre 1", "Condominio A - Torre 2", "Condominio B - Torre 1"
  const torresLabels = condominios.flatMap(c =>
    (c.torres || []).map(t => `${c.nombre} – Torre ${t.numero}`)
  );
  const torresUnidades = condominios.flatMap(c =>
    (c.torres || []).map(t => t.unidades?.length || 0)
  );

  const totalUnidades = tipoValores.reduce((a, b) => a + b, 0);
  const tipoMasFrecuente = tipoLabels[tipoValores.indexOf(Math.max(...tipoValores))] || '—';




  //lo que se muestra en el dashboard
  return (
    <div>
      <div className="page-title">Estadísticas del Condominio</div>

      {error && <div className="error-msg">Error: {error}</div>}
      {loading && <div className="loading">Cargando estadisticas...</div>}

      {!loading && (
        <>
          {/* resumen */}
          <div className="dashboard-metrics">
            <div className="metric-box">
              <div className="metric-box-value">{condominios.length}</div>
              <div className="metric-box-label">Condominios</div>
            </div>
            <div className="metric-box">
              <div className="metric-box-value">{torresLabels.length}</div>
              <div className="metric-box-label">Torres totales</div>
            </div>
            <div className="metric-box">
              <div className="metric-box-value">{totalUnidades}</div>
              <div className="metric-box-label">Unidades totales</div>
            </div>
            <div className="metric-box">
              <div className="metric-box-value">{tipoMasFrecuente}</div>
              <div className="metric-box-label">Tipo más frecuente</div>
            </div>
          </div>

          <div className="dashboard-grid">

            {/* grafico 1: torres por condominio */}
            <div className="dashboard-card">
              <RadialChart
                labels={condoLabels}
                valores={condoTorres}
                titulo="Torres por condominio"
              />
            </div>

            {/* grafico 2: unidades por torre */}
            <div className="dashboard-card">
              <RadialChart
                labels={torresLabels}
                valores={torresUnidades}
                titulo="Unidades por torre"
              />
            </div>

            {/* dona: por tipo de unidad */}
            <div className="dashboard-card dashboard-card-full">
              <div className="chart-label">Distribución por tipo de unidad</div>
              <DonaChart labels={tipoLabels} valores={tipoValores} />
            </div>

          </div>
        </>
      )}
    </div>
  );
}