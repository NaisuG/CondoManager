import { useState, useEffect } from 'react';
import '../css/NavbarPriv.css';

export default function NavbarPrivado() {
  const [usuario, setUsuario] = useState({
    nombre: "Usuario",
    apellido: "",
    rol: "Personal",
    avatarUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80"
  });

  useEffect(() => {
    // Esta función se ejecuta inmediatamente al renderizar la barra
    const obtenerSesion = () => {
      try {
        const sesionGuardada = localStorage.getItem("usuario_sesion");

        if (sesionGuardada) {
          const datosReales = JSON.parse(sesionGuardada);
          setUsuario({
            nombre: datosReales.nombre || "Usuario",
            apellido: datosReales.apellido || "",
            rol: datosReales.rol || "Administrador",
            avatarUrl: datosReales.avatarUrl || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80"
          });
        }
      } catch (error) {
        console.error("Error al leer la sesión en el Navbar:", error);
      }
    };

    obtenerSesion();

    // Escuchar si los datos cambian en otra pestaña o ventana del navegador
    window.addEventListener('storage', obtenerSesion);
    return () => window.removeEventListener('storage', obtenerSesion);
  }, []);

  return (
      <nav className="navbar-privado">
        {/* SECCIÓN DERECHA: PERFIL */}
        <div className="navbar-user-actions">
          <div className="user-profile-wrapper">
            <div className="user-info-text">
            <span className="user-display-name">
              {usuario.nombre} {usuario.apellido}
            </span>
              <span className="user-display-role">
              {usuario.rol}
            </span>
            </div>

            <img
                src={usuario.avatarUrl}
                alt="Avatar"
                className="user-avatar"
            />
          </div>
        </div>
      </nav>
  );
}