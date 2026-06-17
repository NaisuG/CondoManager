import React, { useState } from "react";
import "../css/Auth.css";

export default function PaginaAuth({ alLoguearse, alVolverAlHome }) {
    const [esLogin, setEsLogin] = useState(true);
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [nombre, setNombre] = useState("");
    const [apellido, setApellido] = useState("");
    const [rol, setRol] = useState("ROL_USER");
    const [error, setError] = useState("");
    const [mensaje, setMensaje] = useState("");

    const handleSubmit = (e) => {
        e.preventDefault();
        setError("");
        setMensaje("");

        const endpoint = esLogin ? "/api/bff/auth/login" : "/api/bff/auth/register";

        // Envia las variables
        const bodyPayload = esLogin
            ? { email, password }
            : { email, password, nombre, apellido, rol };

        fetch(`http://localhost:9000${endpoint}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(bodyPayload)
        })
            .then((res) => {
                if (!res.ok) throw new Error("Credenciales inválidas o error interno en el servidor.");
                return res.json();
            })
            .then((data) => {
                if (esLogin) {
                    // 1. Guardamos el token de autenticación original
                    localStorage.setItem("token_jwt", data.token);

                    /* 2. CLAVE PARA EL NAVBAR: Creamos el objeto con los datos de la base de datos.
                       Si tu backend ya devuelve nombre/apellido en el 'data', los usará directamente.
                       Si no vienen, dejamos valores de respaldo basados en los inputs del formulario.
                    */
                    const datosUsuario = {
                        nombre: data.nombre || nombre || "Usuario",
                        apellido: data.apellido || apellido || "",
                        rol: (data.rol || rol) === "ROL_ADMIN" ? "Manager" : "Residente",
                        avatarUrl: data.avatarUrl || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80"
                    };

                    // Sincronizamos el disco con la estructura exacta que lee tu barra privada
                    localStorage.setItem("usuario_sesion", JSON.stringify(datosUsuario));

                    // 3. Cambiamos el estado de la aplicación
                    alLoguearse();
                } else {
                    setMensaje("¡Usuario registrado con éxito! Ya puedes iniciar sesión.");
                    // Limpia los campos para hacer el login
                    setNombre("");
                    setApellido("");
                    setEmail("");
                    setPassword("");
                    setEsLogin(true);
                }
            })
            .catch((err) => {
                setError(err.message);
            });
    };

    return (
        <div className="auth-page-container">
            <div className="auth-card">
                <button onClick={alVolverAlHome} className="auth-back-btn">
                    ← Volver al inicio
                </button>

                <h2 className="auth-title">
                    {esLogin ? "Iniciar Sesión" : "Crear Cuenta"}
                </h2>
                <p className="auth-subtitle">
                    {esLogin ? "Ingresa al ecosistema CondoManager" : "Regístrate en la plataforma de copropiedad"}
                </p>

                {error && <div className="auth-error-msg">{error}</div>}
                {mensaje && <div className="auth-success-msg">{mensaje}</div>}

                <form onSubmit={handleSubmit} className="auth-form">

                    {/* CAMPOS DINAMICOS SOLO SI ESTÁ EN MODO REGISTRO */}
                    {!esLogin && (
                        <>
                            <div className="auth-group">
                                <label className="auth-label">Nombre</label>
                                <input
                                    type="text"
                                    value={nombre}
                                    onChange={(e) => setNombre(e.target.value)}
                                    className="auth-input"
                                    placeholder="Tu primer nombre"
                                    required
                                />
                            </div>
                            <div className="auth-group">
                                <label className="auth-label">Apellido</label>
                                <input
                                    type="text"
                                    value={apellido}
                                    onChange={(e) => setApellido(e.target.value)}
                                    className="auth-input"
                                    placeholder="Tu apellido"
                                    required
                                />
                            </div>
                        </>
                    )}

                    <div className="auth-group">
                        <label className="auth-label">Correo Electrónico</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="auth-input"
                            placeholder="ejemplo@correo.com"
                            required
                        />
                    </div>

                    <div className="auth-group">
                        <label className="auth-label">Contraseña</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="auth-input"
                            placeholder="••••••••"
                            required
                        />
                    </div>

                    {!esLogin && (
                        <div className="auth-group">
                            <label className="auth-label">Rol asignado</label>
                            <select
                                value={rol}
                                onChange={(e) => setRol(e.target.value)}
                                className="auth-select"
                            >
                                <option value="ROL_USER">Usuario Residente / Conserje</option>
                                <option value="ROL_ADMIN">Administrador de Copropiedad</option>
                            </select>
                        </div>
                    )}

                    <button type="submit" className="auth-submit-btn">
                        {esLogin ? "Entrar" : "Registrarse"}
                    </button>
                </form>

                <div className="auth-switch-text">
                    {esLogin ? "¿No tienes cuenta? " : "¿Ya tienes cuenta? "}
                    <span
                        onClick={() => {
                            setEsLogin(!esLogin);
                            setError("");
                            setMensaje("");
                            setNombre("");
                            setApellido("");
                        }}
                        className="auth-switch-link"
                    >
                        {esLogin ? "Regístrate aquí" : "Inicia sesión"}
                    </span>
                </div>

            </div>
        </div>
    );
}