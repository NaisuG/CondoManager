import { useState, useEffect } from "react";
import "../css/Registro.css";

export default function PaginaRegistro() {
  const [activeTab, setActiveTab] = useState("condominio");


  const usuarioSesion = JSON.parse(localStorage.getItem("usuario_sesion") || "null");
  const idUsuarioActual = usuarioSesion?.idUsuario ?? null;

  const headersConAuth = () => {
    const headers = { "Content-Type": "application/json" };
    const token = localStorage.getItem("token_jwt");
    // if (token) headers["Authorization"] = `Bearer ${token}`;
    return headers;
  };

  // Catálogos cargados dinámicamente para alimentar los selectores (<select>)
  const [condominios, setCondominios] = useState([]);
  const [tiposUnidad, setTiposUnidad] = useState([]);
  const [torresDisponibles, setTorresDisponibles] = useState([]);
  const [condoSeleccionadoParaFiltrar, setCondoSeleccionadoParaFiltrar] = useState("");

  // Estados de formularios alineados al 100% con tus RequestDTOs de Java
  const [formCondo, setFormCondo] = useState({ nombre: "", direccion: "" });
  const [formTipo, setFormTipo] = useState({ nombre: "" });
  const [formTorre, setFormTorre] = useState({ condominioId: "", numero: "" });
  const [formUnidad, setFormUnidad] = useState({ torreId: "", numero: "", tipoId: "", m2: "" });
  const [formResidente, setFormResidente] = useState({ run: "", nombre: "", correo: "" });

  // Sincroniza los catálogos base al cargar la consola
  useEffect(() => {
    cargarCatalogosBase();
  }, []);

  const cargarCatalogosBase = async () => {
    try {
      const resCondos = await fetch("/api/bff/registro/condominios");
      if (resCondos.ok) setCondominios(await resCondos.json());

      const resTipos = await fetch("/api/bff/registro/tipos-unidad");
      if (resTipos.ok) setTiposUnidad(await resTipos.json());
    } catch (e) {
      console.error("Error al sincronizar catálogos base:", e);
    }
  };

  // Carga reactiva de torres específicas cuando se escoge un condominio en la pestaña de Unidades
  useEffect(() => {
    if (!condoSeleccionadoParaFiltrar) {
      setTorresDisponibles([]);
      return;
    }
    fetch(`/api/bff/registro/condominio/${condoSeleccionadoParaFiltrar}`)
      .then((r) => r.json())
      .then((data) => setTorresDisponibles(data.torres || []))
      .catch((e) => console.error("Error al mapear subestructura de torres:", e));
  }, [condoSeleccionadoParaFiltrar]);

  // POST hacia las nuevas rutas del BFF
  const procesarEnvio = async (e, endpoint, payload, mensajeExito, limpiarFormulario) => {
    e.preventDefault();
    try {
      const response = await fetch(endpoint, {
        method: "POST",
        headers: headersConAuth(),
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        alert(mensajeExito);
        limpiarFormulario();
        await cargarCatalogosBase(); // Refresca los selectores automáticamente
      } else {
        const errData = await response.json().catch(() => ({}));
        alert(`Error en validación: ${errData.error || "Operación rechazada por la base de datos."}`);
      }
    } catch (err) {
      alert("Error crítico: El BFF no responde.");
    }
  };

  return (
    <div className="modulo-registros-container">
      
      {/* HEADER COHERENTE CON CONDOMINIO.JSX */}
      <div className="modulo-registros-header">
        <h1 className="modulo-registros-title">Consola de Alta de Activos</h1>
        <p className="modulo-registros-subtitle">
          Estructura nuevos condominios, configure tipologías de inmuebles y declare la propiedad física de forma jerárquica.
        </p>
      </div>

      {/* SISTEMA DE NAVEGACIÓN ENTRE FORMULARIOS */}
      <div className="registros-tabs-bar">
        <button className={`reg-tab-btn ${activeTab === "condominio" ? "active" : ""}`} onClick={() => setActiveTab("condominio")}>1. Condominio</button>
        <button className={`reg-tab-btn ${activeTab === "tipo" ? "active" : ""}`} onClick={() => setActiveTab("tipo")}>2. Tipos de Unidad</button>
        <button className={`reg-tab-btn ${activeTab === "torre" ? "active" : ""}`} onClick={() => setActiveTab("torre")}>3. Torres</button>
        <button className={`reg-tab-btn ${activeTab === "unidad" ? "active" : ""}`} onClick={() => setActiveTab("unidad")}>4. Unidades</button>
        <button className={`reg-tab-btn ${activeTab === "residente" ? "active" : ""}`} onClick={() => setActiveTab("residente")}>5. Residentes</button>
      </div>

      {/* ÁREA DE CONTENIDO */}
      <div className="registros-content-card">
        
        {/* FORMULARIO 1: CONDOMINIO */}
        {activeTab === "condominio" && (
          <form onSubmit={(e) => procesarEnvio(e, "/api/bff/registro/condominios/crear", { ...formCondo, idUsuario: idUsuarioActual }, "¡Condominio creado de forma exitosa!", () => setFormCondo({ nombre: "", direccion: "" }))}>
            <h3 className="form-section-heading">Crear Nueva Condominio</h3>
            <div className="reg-input-group">
              <label>Nombre:</label>
              <input type="text" required placeholder="Ej: Condominio Parinas II" value={formCondo.nombre} onChange={(e) => setFormCondo({ ...formCondo, nombre: e.target.value })} />
            </div>
            <div className="reg-input-group">
              <label>Dirección Legal:</label>
              <input type="text" required placeholder="Ej: Av. Francisco Bilbao 2340" value={formCondo.direccion} onChange={(e) => setFormCondo({ ...formCondo, direccion: e.target.value })} />
            </div>
            <button type="submit" className="reg-submit-btn">Inicializar Propiedad</button>
          </form>
        )}

        {/* FORMULARIO 2: TIPO UNIDAD */}
        {activeTab === "tipo" && (
          <form onSubmit={(e) => procesarEnvio(e, "/api/bff/registro/tipos-unidad/crear", formTipo, "¡Nueva Tipo de inmueble añadida correctamente!", () => setFormTipo({ nombre: "" }))}>
            <h3 className="form-section-heading">Definir Tipo de Inmueble</h3>
            <div className="reg-input-group">
              <label>Nombre identificatorio del Tipo:</label>
              <input type="text" required placeholder="Ej: DEPARTAMENTO TIPO C, ESTACIONAMIENTO, BODEGA" value={formTipo.nombre} onChange={(e) => setFormTipo({ nombre: e.target.value.toUpperCase() })} />
            </div>
            <button type="submit" className="reg-submit-btn">Almacenar Categoría</button>
          </form>
        )}

        {/* FORMULARIO 3: TORRE */}
        {activeTab === "torre" && (
          <form onSubmit={(e) => procesarEnvio(e, "/api/bff/registro/torres/crear", { condominioId: Number(formTorre.condominioId), numero: Number(formTorre.numero) }, "🗼 ¡Bloque/Torre anexado a la copropiedad!", () => setFormTorre({ condominioId: "", numero: "" }))}>
            <h3 className="form-section-heading">Asignar Bloque o Torre</h3>
            <div className="reg-input-group">
              <label>Condominio Destino:</label>
              <select required value={formTorre.condominioId} onChange={(e) => setFormTorre({ ...formTorre, condominioId: e.target.value })}>
                <option value="">-- Seleccione un condominio del catálogo --</option>
                {condominios.map((c) => <option key={c.id} value={c.id}>{c.nombre}</option>)}
              </select>
            </div>
            <div className="reg-input-group">
              <label>Número identificador de la Torre:</label>
              <input type="number" required placeholder="Ej: 4" value={formTorre.numero} onChange={(e) => setFormTorre({ ...formTorre, numero: e.target.value })} />
            </div>
            <button type="submit" className="reg-submit-btn">Anexar Torre</button>
          </form>
        )}

        {/* FORMULARIO 4: UNIDAD */}
        {activeTab === "unidad" && (
          <form onSubmit={(e) => procesarEnvio(e, "/api/bff/registro/unidades/crear", { torreId: Number(formUnidad.torreId), numero: Number(formUnidad.numero), tipoId: Number(formUnidad.tipoId), m2: parseFloat(formUnidad.m2) }, "🚪 ¡Unidad inmobiliaria construida!", () => setFormUnidad({ torreId: "", numero: "", tipoId: "", m2: "" }))}>
            <h3 className="form-section-heading">Alta de Unidades Autónomas</h3>
            
            <div className="reg-input-group">
              <label>1. Filtrar por Condominio:</label>
              <select value={condoSeleccionadoParaFiltrar} onChange={(e) => setCondoSeleccionadoParaFiltrar(e.target.value)}>
                <option value="">-- Busque la copropiedad base --</option>
                {condominios.map((c) => <option key={c.id} value={c.id}>{c.nombre}</option>)}
              </select>
            </div>

            <div className="reg-input-group">
              <label>2. Seleccionar Bloque o Torre Asociada:</label>
              <select required disabled={torresDisponibles.length === 0} value={formUnidad.torreId} onChange={(e) => setFormUnidad({ ...formUnidad, torreId: e.target.value })}>
                <option value="">{torresDisponibles.length === 0 ? "⚠️ Primero seleccione un condominio con torres" : "-- Seleccione el bloque destino --"}</option>
                {torresDisponibles.map((t) => <option key={t.id} value={t.id}>Torre {t.numero}</option>)}
              </select>
            </div>

            <div className="reg-input-group">
              <label>Número de Departamento / Oficina / Bodega:</label>
              <input type="number" required placeholder="Ej: 402" value={formUnidad.numero} onChange={(e) => setFormUnidad({ ...formUnidad, numero: e.target.value })} />
            </div>

            <div className="reg-input-group">
              <label>Clasificación / Tipo de Inmueble:</label>
              <select required value={formUnidad.tipoId} onChange={(e) => setFormUnidad({ ...formUnidad, tipoId: e.target.value })}>
                <option value="">-- Vincular con Tipo Unidad --</option>
                {tiposUnidad.map((t) => <option key={t.id} value={t.id}>{t.nombre}</option>)}
              </select>
            </div>

            <div className="reg-input-group">
              <label>Superficie Útil Declarada (m²):</label>
              <input type="number" step="0.01" required placeholder="Ej: 74.35" value={formUnidad.m2} onChange={(e) => setFormUnidad({ ...formUnidad, m2: e.target.value })} />
            </div>

            <button type="submit" className="reg-submit-btn">Dar de Alta Unidad</button>
          </form>
        )}

        {/* FORMULARIO 5: RESIDENTE */}
        {activeTab === "residente" && (
          <form onSubmit={(e) => procesarEnvio(e, "/api/bff/registro/residentes/crear", formResidente, "👤 ¡Ficha maestra de residente incorporada!", () => setFormResidente({ run: "", nombre: "", correo: "" }))}>
            <h3 className="form-section-heading">Ficha Maestra de Copropietarios</h3>
            <div className="reg-input-group">
              <label>RUN / Identificación Nacional:</label>
              <input type="text" required placeholder="Ej: 21456789-0" value={formResidente.run} onChange={(e) => setFormResidente({ ...formResidente, run: e.target.value })} />
            </div>
            <div className="reg-input-group">
              <label>Nombre Completo:</label>
              <input type="text" required placeholder="Ej: Carlos Silva Mendoza" value={formResidente.nombre} onChange={(e) => setFormResidente({ ...formResidente, nombre: e.target.value })} />
            </div>
            <div className="reg-input-group">
              <label>Correo Electrónico de Contacto:</label>
              <input type="email" required placeholder="carlos.silva@email.com" value={formResidente.correo} onChange={(e) => setFormResidente({ ...formResidente, correo: e.target.value })} />
            </div>
            <button type="submit" className="reg-submit-btn secondary-btn">Almacenar Residente</button>
          </form>
        )}

      </div>
    </div>
  );
}