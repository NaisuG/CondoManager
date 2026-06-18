import React from 'react';

export default function Sidebar({ pageActiva, setPage, alCerrarSesion }) {
    return (
        <aside className="dashboard-sidebar">
            <div className="sidebar-logo">CondoManager</div>

            <nav className="sidebar-menu">
                <button
                    className={`sidebar-btn ${pageActiva === 'dashboard' ? 'active' : ''}`}
                    onClick={() => setPage('dashboard')}
                >
                    Dashboard
                </button>
                <button
                    className={`sidebar-btn ${pageActiva === 'finanzas' ? 'active' : ''}`}
                    onClick={() => setPage('finanzas')}
                >
                    Contabilidad
                </button>
                <button
                    className={`sidebar-btn ${pageActiva === 'ordenes' ? 'active' : ''}`}
                    onClick={() => setPage('ordenes')}
                >
                    Mantenimientos
                </button>
                <button
                    className={`sidebar-btn ${pageActiva === 'condo' ? 'active' : ''}`}
                    onClick={() => setPage('condo')}
                >
                    Buscar Condominio
                </button>
                <button
                    className={`sidebar-btn ${pageActiva === 'documentos' ? 'active' : ''}`}
                    onClick={() => setPage('documentos')}
                >
                    Documentos
                </button>
                <button
                    className={`sidebar-btn ${pageActiva === 'registro' ? 'active' : ''}`}
                    onClick={() => setPage('registro')}
                >
                    Registro
                </button>
                                <button
                    className={`sidebar-btn ${pageActiva === 'alta' ? 'active' : ''}`}
                    onClick={() => setPage('alta')}
                >
                    Alta
                </button>
            </nav>

            <div className="sidebar-footer">
                <button className="sidebar-logout-btn" onClick={alCerrarSesion}>
                    Cerrar Sesión
                </button>
            </div>
        </aside>
    );
}