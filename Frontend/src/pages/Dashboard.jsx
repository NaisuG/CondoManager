import { useEffect, useRef } from 'react';
import Chart from 'chart.js/auto';

export default function Dashboard() {
  const chartRef = useRef(null);
  const chartInstance = useRef(null);

  useEffect(() => {
    const cargarDatos = async () => {
      try {
        const respuesta = await fetch('http://localhost:8082/api/unidades/estadisticas');
        const datos = await respuesta.json();

        const etiquetas = Object.keys(datos);
        const valores = Object.values(datos);

        if (chartInstance.current) {
          chartInstance.current.destroy();
        }

        const ctx = chartRef.current.getContext('2d');
        chartInstance.current = new Chart(ctx, {
          type: 'doughnut',
          data: {
            labels: etiquetas,
            datasets: [
              {
                label: 'Total',
                data: valores,
                backgroundColor: [
                  '#3498db', // Azul
                  '#2ecc71', // Verde
                  '#9b59b6', // Morado
                  '#f1c40f', // Amarillo
                  '#e74c3c', // Rojo
                ],
                borderWidth: 0,
                hoverOffset: 4,
              },
            ],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: 'bottom',
              },
            },
          },
        });
      } catch (error) {
        console.error('Error al conectar con el backend:', error);
      }
    };

    cargarDatos();

    return () => {
      if (chartInstance.current) {
        chartInstance.current.destroy();
      }
    };
  }, []);

  return (
    <div style={styles.container}>
      <h2 style={styles.titulo}>Estadísticas del Condominio</h2>
      
      <div style={styles.card}>
        <h3 style={styles.subtitulo}>Distribución de Unidades</h3>
        {/* Aquí es donde Chart.js dibuja la magia */}
        <div style={styles.chartWrapper}>
          <canvas ref={chartRef}></canvas>
        </div>
      </div>
    </div>
  );
};

const styles = {
  container: {
    padding: '2rem',
    backgroundColor: '#f8f9fa',
    minHeight: '100vh',
    fontFamily: 'system-ui, sans-serif'
  },
  titulo: {
    color: '#2c3e50',
    marginBottom: '1.5rem'
  },
  card: {
    backgroundColor: 'white',
    padding: '2rem',
    borderRadius: '12px',
    boxShadow: '0 4px 6px rgba(0,0,0,0.05)',
    maxWidth: '500px'
  },
  subtitulo: {
    textAlign: 'center',
    color: '#34495e',
    marginTop: '0',
    marginBottom: '1rem'
  },
  chartWrapper: {
    position: 'relative',
    height: '300px',
    width: '100%'
  }
};