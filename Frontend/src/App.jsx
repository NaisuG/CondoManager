import { useState } from "react";
import PaginaHome from "./pages/Home";
import PaginaAuth from "./pages/Auth";
import PaginaDashboard from "./pages/Dashboard";
import PaginaFinanzas from "./pages/Finanzas";
import PaginaOrdenes from "./pages/Ordenes";
import PaginaCondominio from "./pages/Condominio";
import Sidebar from "./components/Sidebar";
import NavbarPrivado from "./components/NavbarPriv";
import PaginaDocumentos from "./pages/Documentos";
import "./App.css";

export default function App() {
  const [estaLogueado, setEstaLogueado] = useState(() => {
    return !!localStorage.getItem("token_jwt");
  });

  const [view, setView] = useState("home");
  const [page, setPage] = useState("dashboard");

  const handleLogout = () => {
    localStorage.removeItem("token_jwt");
    setEstaLogueado(false);
    setView("home");
  };

  // VISTA 1: Home Público (Paso obligado al cargar la página)
  if (view === "home") {
    return (
        <PaginaHome
            estaLogueado={estaLogueado}
            alIrAlLogin={() => setView("auth")}
            alIrAlPanel={() => setView("panel_privado")}
        />
    );
  }

  // VISTA 2: Autenticación (Login / Registro)
  if (view === "auth") {
    return (
        <PaginaAuth
            alLoguearse={() => {
              setEstaLogueado(true);
              setView("panel_privado");
            }}
            alVolverAlHome={() => setView("home")}
        />
    );
  }

  // VISTA 3: Panel Privado Estructurado Modularmente
  if (view === "panel_privado" && estaLogueado) {
    return (
        <div className="dashboard-layout">
          <Sidebar
              pageActiva={page}
              setPage={setPage}
              alCerrarSesion={handleLogout}
          />

          <div className="dashboard-main-container">
            <NavbarPrivado pageActiva={page} />

            <main className="dashboard-content">
              {page === "dashboard" && <PaginaDashboard />}
              {page === "finanzas" && <PaginaFinanzas />}
              {page === "ordenes" && <PaginaOrdenes />}
              {page === "condo" && <PaginaCondominio />}
              {page === "documentos" && <PaginaDocumentos />}
            </main>
          </div>
        </div>
    );
  }

  return <PaginaHome estaLogueado={estaLogueado} alIrAlLogin={() => setView("auth")} alIrAlPanel={() => setView("panel_privado")} />;
}