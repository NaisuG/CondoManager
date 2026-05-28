import { useState } from "react";
import PaginaOrdenes from "./pages/Ordenes";
import PaginaCondominio from "./pages/Condominio";
import "./App.css";
import PaginaDashboard from "./pages/Dashboard";

export default function App() {
  const [page, setPage] = useState("dashboard");

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
        {page === "ordenes" && <PaginaOrdenes />}
        {page === "condo" && <PaginaCondominio />}
      </main>
    </div>
  );
}