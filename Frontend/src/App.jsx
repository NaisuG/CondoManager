import { useState } from "react";
import PaginaHome from "./pages/Home";
import PaginaAuth from "./pages/Auth";
import PaginaOrdenes from "./pages/Ordenes";
import PaginaCondominio from "./pages/Condominio";
import PaginaDashboard from "./pages/Dashboard";
import PaginaFinanzas from "./pages/Finanzas";

export default function App() {
  //controlar si el usuario inicio sesion
  const [estaLogueado, setEstaLogueado] = useState(() => {
    // existe un token guardado en el navegador
    return !!localStorage.getItem("token_jwt");
  });


  const [view, setView] = useState("home");
  const [page, setPage] = useState("dashboard"); // menu del panel privado

  //cerrar sesion
  const handleLogout = () => {
    localStorage.removeItem("token_jwt");
    setEstaLogueado(false);
    setView("home");
  };

//sin iniciar
  if (!estaLogueado) {
    if (view === "auth") {
      return (
          <PaginaAuth
              alLoguearse={() => setEstaLogueado(true)}
              alVolverAlHome={() => setView("home")}
          />
      );
    }
    // Home por defecto
    return <PaginaHome alIrAlLogin={() => setView("auth")} />;
  }

//usuario logueado
  return (
      <div className="app">
        <aside className="sidebar">
          <div className="sidebar-brand">Condominio</div>
          <nav className="sidebar-nav">
          <span
              className={`sidebar-link ${page === "dashboard" ? "active" : ""}`}
              onClick={() => setPage("dashboard")}
          >
            Dashboard
          </span>
          <span
            className={`sidebar-link ${page === "finanzas" ? "active" : ""}`}
            onClick={() => setPage("finanzas")}
          >
            Finanzas
          </span>
          <span
            className={`sidebar-link ${page === "ordenes" ? "active" : ""}`}
            onClick={() => setPage("ordenes")}
          >
            Mantenimientos
          </span>
            <span
                className={`sidebar-link ${page === "condo" ? "active" : ""}`}
                onClick={() => setPage("condo")}
            >
            Registro condominio
          </span>
        </nav>
      </aside>
      <main className="main">
        {page === "dashboard" && <PaginaDashboard />}
        {page === "finanzas" && <PaginaFinanzas />}
        {page === "ordenes" && <PaginaOrdenes />}
        {page === "condo" && <PaginaCondominio />}
      </main>
    </div>
  );
}