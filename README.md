README

Sistema de gestión de condominios basado en arquitectura de microservicios con Spring Boot, frontend en React + Vite, y orquestado con Docker Compose.

Tecnologías.
Backend: Java 21, Spring Boot, Maven.
Frontend: React, Vite, Node.js
Contenedores: Docker, Docker Compose
Base de datos: PostgreSQL

Requisitos previos.
Tener instalado Docker Desktop

Cómo levantar el proyecto.
Clonar el repositorio:
git clone https://github.com/usuario/Gestion-Condominios.git
cd Gestion-Condominios

Levantar todos los servicios:
docker compose up --build

Nota: La primera vez tomará varios minutos mientras se descargan las dependencias de Maven y Node.js para compilar las imágenes.

Acceder a la aplicación:
Una vez que la terminal deje de descargar dependencias y veas que los servicios de Spring Boot han iniciado, abre tu navegador en:
http://localhost:3000


Detener los servicios:
docker compose down


Datos Pre-cargados.
El sistema cuenta con scripts de inicialización (data.sql) integrados en Spring Boot. Al levantar los contenedores en una base de datos limpia, el sistema creará automáticamente las tablas y poblará la base de datos con información de prueba (Edificios, Torres, Unidades, Proveedores y Órdenes de Mantenimiento). No necesitas ejecutar scripts externos ni usar Postman para ver el sistema funcionando.

Arquitectura y Puertos.
Frontend (UI): Puerto 3000
Backend-For-Frontend (BFF - Gateway): Puerto 9000
Microservicio Registro: Puerto 8082
Microservicio Proveedores: Puerto 8081
Microservicio Mantenimiento: Puerto 8080
Bases de Datos (PostgreSQL): Puertos 5432, 5433, 5434 (mapeados externamente para depuración).
Solución de Problemas Comunes (Troubleshooting)
Si encuentras algún problema al levantar el proyecto, revisa los logs de tu terminal (o usa docker-compose logs -f [nombre_servicio]). Aquí están los problemas más comunes y sus soluciones:
La base de datos no se pobló (Pantalla en blanco)
Problema: Ves el frontend, pero no hay edificios ni datos.
Causa: Docker detectó volúmenes antiguos de bases de datos que ya existían y saltó el proceso de inicialización, o hubo un error previo que dejó las tablas a medias.
Solución: Debes borrar los datos residuales y forzar la inicialización limpia:
docker-compose down -v
docker-compose up --build

Error de "Condición de Carrera" en el inicio 
Problema: Verás que los microservicios de Spring Boot arrojan errores masivos rojos diciendo PSQLException: Connection refused o Unable to determine Dialect, y luego dicen exited with code 1. El BFF mostrará Failed to resolve 'app-registro'.
Causa: Esto es normal en la primera ejecución en un equipo nuevo. Postgres tarda unos 15 segundos en crear los usuarios y bases de datos desde cero, pero Spring Boot (Java) es muy rápido e intenta conectarse en 3 segundos. Al no encontrar la base de datos lista, Java se rinde y el contenedor se apaga.
Solución: 
Espera unos 20 segundos a que la terminal muestre que Postgres terminó (busca el log: database system is ready to accept connections).
Presiona Ctrl + C para detener el proceso.
Vuelve a ejecutar *solo el comando de subida* (sin build ni limpieza): 
docker-compose up

Nota: El archivo docker-compose.yml está configurado con restart: always para mitigar esto, pero un reinicio manual rápido asegura el éxito.

Fallo en la comunicación entre microservicios (BFF no responde)
Problema: En el frontend ves datos parciales o la terminal del BFF muestra un error al intentar conectarse a http://app-mantenimiento:8080.
Causa: El microservicio de destino se detuvo o las variables de entorno en el BFF no están apuntando al nombre correcto del contenedor en la red de Docker.
Solución: Asegúrate de que no tienes aplicaciones locales en tu PC ocupando los puertos 8080, 8081 o 8082, lo que impediría que los contenedores inicien.

Cambios en el código no se reflejan.
Problema: Editaste un archivo en Java o en React, hiciste docker-compose up, pero sigues viendo la versión vieja.
Causa: Docker usa caché para compilar más rápido y no detectó tu cambio.
Solución: Fuerza la reconstrucción sin caché:
bash
docker-compose build --no-cache
docker-compose up
