import Navbar from "../components/Navbar.jsx";
import "../css/SobreNosotros.css";

export default function PaginaSobreNosotros({ estaLogueado, alIrAlLogin, alIrAlPanel, alVolverAlHome }) {
    const manejarAccionPrincipal = estaLogueado ? alIrAlPanel : alIrAlLogin;

    return (
        <div className="home-public-container">
            <Navbar alIngresar={manejarAccionPrincipal} estaLogueado={estaLogueado} alVolverAlHome={alVolverAlHome} />

            <main>
                {/* ---------- INTRO ---------- */}
                <header className="about-hero">
                    <span className="hero-tagline">Sobre Nosotros</span>
                    <h1 className="about-titulo">
                        Un solo panel para administrar cada torre, unidad y proveedor.
                    </h1>
                    <p className="about-subtitulo">
                        CondoManager nace de un problema simple: la administración de condominios
                        se maneja hoy entre planillas sueltas, grupos de WhatsApp y papeles. Construimos
                        una plataforma que junta mantenimiento, proveedores, documentos y finanzas en
                        un mismo lugar, pensada para administradores y copropietarios reales.
                    </p>
                </header>

                {/* ---------- ARQUITECTURA / CÓMO FUNCIONA ---------- */}
                <section className="about-arquitectura">
                    <h2 className="section-title">Un sistema, siete piezas independientes</h2>
                    <p className="section-subtitle">
                        En vez de un monolito, dividimos la plataforma en microservicios independientes.
                        Si el módulo de documentos necesita mantención, el resto del sistema sigue funcionando.
                    </p>

                    <div className="arquitectura-grid">
                        <div className="arq-item">
                            <span className="arq-tag">Acceso</span>
                            <p>Autenticación y roles</p>
                        </div>
                        <div className="arq-item">
                            <span className="arq-tag">Operación</span>
                            <p>Mantenimiento y proveedores</p>
                        </div>
                        <div className="arq-item">
                            <span className="arq-tag">Registro</span>
                            <p>Condominios y unidades</p>
                        </div>
                        <div className="arq-item">
                            <span className="arq-tag">Finanzas</span>
                            <p>Contabilidad y cobros</p>
                        </div>
                        <div className="arq-item">
                            <span className="arq-tag">Archivos</span>
                            <p>Documentos y reglamentos</p>
                        </div>
                    </div>
                </section>

                {/* ---------- VALORES ---------- */}
                <section className="services-section about-valores">
                    <h2 className="section-title">Lo que no negociamos</h2>
                    <p className="section-subtitle">
                        Principios que guían cada decisión técnica del proyecto, desde el diseño de la
                        base de datos hasta la última pantalla.
                    </p>

                    <div className="services-grid">
                        <div className="service-card">
                            <h3 className="service-card-title">Transparencia</h3>
                            <p className="service-card-desc">
                                Cada orden de mantenimiento y cada pago queda registrado y trazable,
                                sin planillas paralelas ni versiones distintas de la misma información.
                            </p>
                        </div>

                        <div className="service-card">
                            <h3 className="service-card-title">Seguridad de datos</h3>
                            <p className="service-card-desc">
                                Autenticación centralizada y contraseñas siempre encriptadas. La información
                                de cada condominio se mantiene separada y protegida.
                            </p>
                        </div>

                        <div className="service-card">
                            <h3 className="service-card-title">Escalabilidad real</h3>
                            <p className="service-card-desc">
                                Cada módulo crece de forma independiente. Sumar un condominio nuevo no
                                significa reconstruir el sistema completo.
                            </p>
                        </div>
                    </div>
                </section>

                {/* ---------- EQUIPO ---------- */}
                <section className="services-section about-equipo">
                    <h2 className="section-title">El equipo detrás del proyecto</h2>
                    <p className="section-subtitle">
                        Un equipo pequeño, responsable de principio a fin: desde el diseño de la
                        arquitectura hasta la última pantalla del panel.
                    </p>

                    <div className="equipo-grid">
                        <div className="equipo-card">
                            <div className="equipo-avatar">NC</div>
                            <h3 className="equipo-nombre">Nicole</h3>
                            <span className="equipo-rol">Frontend & UX</span>
                        </div>

                        <div className="equipo-card">
                            <div className="equipo-avatar">FG</div>
                            <h3 className="equipo-nombre">Freddy</h3>
                            <span className="equipo-rol">Backend & Arquitectura</span>
                        </div>
                    </div>
                </section>

                {/* ---------- CONTACTO ---------- */}
                <section className="about-contacto">
                    <h2 className="section-title">Conversemos</h2>
                    <p className="section-subtitle">
                        ¿Tienes preguntas sobre el proyecto o quieres proponer una mejora? Escríbenos.
                    </p>

                    <div className="contacto-grid">
                        <a href="mailto:contacto@condomanager.cl" className="contacto-item">
                            <span className="contacto-label">Correo</span>
                            <span className="contacto-valor">contacto@condomanager.cl</span>
                        </a>
                        <a href="https://github.com/NaisuG/CondoManager" target="_blank" rel="noreferrer" className="contacto-item">
                            <span className="contacto-label">Repositorio</span>
                            <span className="contacto-valor">GitHub</span>
                        </a>
                        <div className="contacto-item">
                            <span className="contacto-label">Ubicación</span>
                            <span className="contacto-valor">Santiago, Chile</span>
                        </div>
                    </div>
                </section>

                {/* ---------- CTA FINAL ---------- */}
                <section className="about-cta">
                    <h2 className="about-cta-titulo">¿Listo para ordenar tu condominio?</h2>
                    <p className="about-cta-texto">
                        Entra al panel y prueba el flujo completo: crea una orden de mantenimiento,
                        revisa proveedores y gestiona tus unidades desde un solo lugar.
                    </p>
                    <button onClick={manejarAccionPrincipal} className="boton-principal">
                        {estaLogueado ? "Ir a mi panel" : "Comenzar Ahora"}
                    </button>
                </section>
            </main>
        </div>
    );
}