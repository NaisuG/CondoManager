export default function Services() {
    return (
        <section id="servicios" className="services-section">
            <h2 className="section-title">Módulos Integrados del Ecosistema</h2>
            <p className="section-subtitle">Diseñado para cubrir todas las necesidades operativas de la administración de copropiedades.</p>

            <div className="services-grid">
                <div className="service-card">
                    <h3 className="service-card-title">Dashboard Operativo</h3>
                    <p className="service-card-desc">Visualización gráfica de métricas de unidades operacionales y estados globales.</p>
                </div>

                <div className="service-card">
                    <h3 className="service-card-title">Mantenimientos</h3>
                    <p className="service-card-desc">Control estricto de órdenes de trabajo de mantenimiento y estados de ejecución.</p>
                </div>

                <div className="service-card">
                    <h3 className="service-card-title">Unidades Habitacionales</h3>
                    <p className="service-card-desc">Administración de torres, departamentos, metrajes cuadrados y datos maestros.</p>
                </div>
            </div>
        </section>
    );
}