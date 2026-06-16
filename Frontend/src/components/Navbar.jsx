export default function Navbar({ alIngresar }) {
    return (
        <nav className="home-navbar">
            <div className="home-logo"><strong>CondoManager</strong></div>
            <div className="nav-links">
                <a href="#inicio" className="nav-link">Inicio</a>
                <a href="#servicios" className="nav-link">Servicios</a>
                <button onClick={alIngresar} className="boton-nav">Ingresar al Sistema</button>
            </div>
        </nav>
    );
}