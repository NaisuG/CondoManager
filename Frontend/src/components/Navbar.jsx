export default function Navbar({ alIngresar, alIrASobreNosotros, alVolverAlHome }) {
    return (
        <nav className="home-navbar">
            <div className="home-logo" onClick={alVolverAlHome} style={{ cursor: alVolverAlHome ? "pointer" : "default" }}>
                <strong>CondoManager</strong>
            </div>
            <div className="nav-links">
                <a href="#inicio" className="nav-link">Inicio</a>
                <a href="#servicios" className="nav-link">Servicios</a>
                {alIrASobreNosotros && (
                    <button onClick={alIrASobreNosotros} className="nav-link nav-link-boton">
                        Sobre Nosotros
                    </button>
                )}
                <button onClick={alIngresar} className="boton-nav">Ingresar al Sistema</button>
            </div>
        </nav>
    );
}