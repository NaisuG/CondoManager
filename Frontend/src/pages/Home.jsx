import React from "react";
import Navbar from "../components/Navbar.jsx";
import Hero from "../components/Hero";
import Services from "../components/Services";
import "../css/Home.css";

export default function PaginaHome({ alIrAlLogin }) {
    return (
        <div className="home-public-container" style={{ background: "#fff", width: "100%", minHeight: "100vh" }}>
            <Navbar alIngresar={alIrAlLogin} />
            <main>
                <Hero alIngresar={alIrAlLogin} />
                <Services />
            </main>
        </div>
    );
}