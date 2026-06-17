export default function Hero({ alIngresar }) {
    return (
        <header id="inicio" className="hero-section">
            <div className="hero-content">
                <span className="hero-tagline">Solución Digital Inteligente</span>
                <h1 className="hero-titulo">La forma más eficiente de gestionar tu Condominio</h1>
                <p className="hero-subtitulo">
                    Controla mantenimientos, proveedores, unidades y órdenes de trabajo desde una sola plataforma unificada y en tiempo real.
                </p>
                <div className="hero-actions">
                    <button onClick={alIngresar} className="boton-principal">Comenzar Ahora</button>
                    <a href="#servicios" className="boton-secundario">Saber Más</a>
                </div>
            </div>
            <div className="hero-image-container">
                <div className="geometric-mockup">
                    <div className="mockup-bar"></div>
                    <div className="mockup-circle"></div>
                    <div className="mockup-lines"></div>
                </div>
            </div>
        </header>
    );
}