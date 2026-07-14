import React from "react";
import Navbar from "../components/Navbar.jsx";
import Hero from "../components/Hero";
import Services from "../components/Services";
import "../css/Home.css";

export default function PaginaHome({ estaLogueado, alIrAlLogin, alIrAlPanel, alIrASobreNosotros }) {
    const manejarAccionPrincipal = estaLogueado ? alIrAlPanel : alIrAlLogin;

    return (
        <div className="home-public-container">
            <Navbar
                alIngresar={manejarAccionPrincipal}
                estaLogueado={estaLogueado}
                alIrASobreNosotros={alIrASobreNosotros}
            />
            <main>
                <Hero alIngresar={manejarAccionPrincipal} estaLogueado={estaLogueado} />
                <Services />
            </main>
        </div>
    );
}