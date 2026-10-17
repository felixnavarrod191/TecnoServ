# TecnoServ — Plataforma de control de mantenimiento y servicios técnicos

Aplicación distribuida con **arquitectura de microservicios** en **Java / Spring Boot**, persistencia en
**MongoDB** (Spring Data MongoDB), APIs REST documentadas con **OpenAPI/Swagger**, interfaz con
**Thymeleaf + Bootstrap** y despliegue con **Docker** en **Render** (bases de datos en **MongoDB Atlas**).

> Un taller recibe equipos de clientes, los asigna a técnicos, controla el diagnóstico, los repuestos usados,
> y calcula el costo total (mano de obra + repuestos) hasta la entrega del equipo.

---

## 1. Arquitectura

```
 Usuarios (5 roles)
        │
        ▼
 ┌──────────────────────┐     ┌─────────────────────┐     ┌───────────────────────────────────────────┐
 │  web  (8080)         │     │  api-gateway (8090) │     │ ms-clientes-equipos (8081) → clientes_db  │
 │  Thymeleaf+Bootstrap │ ──▶ │  entrada única      │ ──▶ │ ms-tecnicos         (8082) → tecnicos_db  │
 │  Spring Security     │     │  Spring Cloud       │     │ ms-repuestos        (8083) → repuestos_db │
 │  usuarios → web_db   │     │  Gateway            │     │ ms-ordenes          (8084) → ordenes_db   │
 └──────────────────────┘     └─────────────────────┘     │ ms-reportes         (8085) → reportes_db  │
                                                          └───────────────────────────────────────────┘
        ms-ordenes ──REST──▶ ms-clientes-equipos, ms-tecnicos, ms-repuestos
        ms-reportes ─REST──▶ ms-ordenes
```

La **web nunca llama directo a los microservicios**: todo pasa por el **API Gateway**, que según la ruta
(`/api/ordenes/**`, `/api/tecnicos/**`, …) reenvía la petición al servicio dueño de ese recurso.

| Servicio | Responsabilidad | Colecciones (su propia BD) |
|---|---|---|
| `ms-clientes-equipos` | Clientes y sus equipos/dispositivos | `clientes`, `equipos` |
| `ms-tecnicos` | Técnicos, especialidades, disponibilidad, tarifa | `tecnicos` |
| `ms-repuestos` | Repuestos (stock/precio), proveedores, pedidos y entregas | `repuestos`, `proveedores`, `pedidos` |
| `ms-ordenes` | Órdenes de servicio, diagnósticos, asignación y flujo de estados | `ordenes`, `diagnosticos` |
| `ms-reportes` | Costo total por orden y total general | `reportes` |
| `api-gateway` | Entrada única: enruta `/api/...` hacia cada microservicio | (no tiene base de datos) |
| `web` | Interfaz y seguridad por roles (usuarios) | `usuarios` |

**Cada microservicio**: es autónomo, se despliega solo (su propio `Dockerfile`), y tiene su propia base de datos
(`database-per-service`). Ninguno lee la base de otro: se consultan **solo por REST**.

### Cómo se cumple lo que pide el proyecto

| Requisito | Dónde está |
|---|---|
| Microservicios autónomos, `database-per-service` | Carpeta de cada servicio + `spring.data.mongodb.database` distinto en cada `application.properties` |
| Java + Spring Boot | Todos los módulos (`pom.xml`) |
| API Gateway | Módulo `api-gateway` (rutas en `application.yml`) |
| Spring Data MongoDB | Paquetes `model` y `repository` de cada servicio |
| REST + OpenAPI/Swagger | Paquete `controller`; Swagger UI en `/swagger-ui.html` de cada servicio |
| Vista con Thymeleaf + Bootstrap | `web/src/main/resources/templates` |
| Docker | `Dockerfile` en cada módulo + `docker-compose.yml` |
| Render | Sección 6 |
| 5 roles | Administrador, Recepcionista, Técnico, Cliente y Proveedor de repuestos |

---

## 2. Cómo funciona (reglas de negocio y dónde se aplican)

**Regla 1 — Asignación de técnicos** (`ms-ordenes`, `OrdenService.asignar`).
Solo se puede asignar un técnico que (a) esté **disponible**, (b) tenga la **especialidad** igual al tipo de
reparación de la orden y (c) tenga menos de **3 órdenes activas** (`ordenes.max-activas`). `ms-ordenes` pregunta
por REST a `ms-tecnicos` y cuenta sus propias órdenes. La pantalla solo ofrece los técnicos que cumplen.

**Regla 2 — Flujo de estados estricto** (`OrdenService.avanzar`, enum `Estado`).
`Ingresado → En diagnóstico → Esperando repuesto → Completado → Entregado`. Solo se avanza al **siguiente** estado
(sin saltos ni retrocesos) y cada cambio queda en el **historial** con fecha y usuario. Condiciones:
- a *En diagnóstico*: debe haber técnico asignado.
- a *Completado*: debe existir al menos un diagnóstico.
- *Entregado*: solo lo marca **Recepción**; los demás pasos los hace el **Técnico** de esa orden.
- Si el técnico intenta usar un repuesto **sin existencias** estando *En diagnóstico*, la orden pasa sola a *Esperando repuesto*.

**Regla 3 — Costo total** (`ms-reportes`, `ReporteService`).
`Costo total = horas trabajadas × tarifa del técnico + Σ (cantidad × precio del repuesto)`.
El precio del repuesto y la tarifa se **copian a la orden** al usarlos/asignarlos, para que cambios posteriores del
catálogo no alteren órdenes ya cerradas.

**Qué hace cada rol**

| Rol | Pantallas |
|---|---|
| Administrador | Panel, técnicos, repuestos, proveedores, pedidos, usuarios, reportes de costo |
| Recepcionista | Clientes y equipos, crear órdenes, asignar técnico, marcar *Entregado* |
| Técnico | Sus órdenes: avanzar estado, diagnóstico, usar repuestos, registrar horas |
| Cliente | Solo SUS órdenes: estado, historial y costo |
| Proveedor | Solo SUS repuestos (los que el taller necesita reponer), registrar pedidos y entregas (la entrega **suma stock**) |

La seguridad está en `web` (Spring Security, contraseñas con BCrypt). Además el técnico solo ve/opera sus órdenes y
el cliente solo las suyas (verificado en `OrdenController.cargar`).

---

## 3. Probar en tu computador

### Opción A — Docker (la más fácil)
```bash
docker compose up --build
```
Abre <http://localhost:8080>. (La primera vez tarda porque compila los 7 módulos.)

### Opción B — Sin Docker
Necesitas **JDK 17+**, **Maven** y un **MongoDB** local (por ejemplo con Compass conectado a `localhost:27017`).
En 7 terminales, una por carpeta, en este orden:
```bash
cd ms-clientes-equipos && mvn spring-boot:run
cd ms-tecnicos         && mvn spring-boot:run
cd ms-repuestos        && mvn spring-boot:run
cd ms-ordenes          && mvn spring-boot:run
cd ms-reportes         && mvn spring-boot:run
cd api-gateway         && mvn spring-boot:run
cd web                 && mvn spring-boot:run
```
Pruebas unitarias: `mvn test` dentro de `ms-ordenes` y `ms-reportes`.

### Opción C — Spring Tools / Eclipse
El proyecto **no usa Lombok** ni plugins extra: se abre con lo que trae Spring Tools.
1. *File → Import → Maven → Existing Maven Projects* y elige la carpeta `tecnoserv` (se importan los 7 módulos).
2. Espera a que termine la descarga de dependencias (necesita internet). Si quedan marcas rojas: seleccionar los
   proyectos → clic derecho → *Maven → Update Project…* con **Force Update** marcado.
3. Necesitas un **JDK 17 o superior** (*Window → Preferences → Java → Installed JREs*).
4. En la vista **Spring Boot Dashboard** selecciona los 7 proyectos y pulsa *Start*.
5. Si usas Atlas, define `MONGODB_URI` en *Run Configurations → Environment* de cada proyecto (menos `api-gateway`).

### Usuarios
Al iniciar se crean `admin / admin123` y `recepcion / recepcion123`.
Entra como admin y pulsa **“Cargar datos de demostración”** en el Panel: crea técnicos, repuestos, un proveedor,
un cliente con un equipo y estos usuarios:

| Usuario | Clave | Rol |
|---|---|---|
| `tecnico1` | `tecnico123` | Técnico (Carlos Ruiz: Computadores, Impresoras) |
| `tecnico2` | `tecnico123` | Técnico (Laura Gómez: Celulares, Electrodomésticos) |
| `cliente1` | `cliente123` | Cliente (María Pérez) |
| `proveedor1` | `proveedor123` | Proveedor (TecnoPartes S.A.S.) |

### Recorrido sugerido para la sustentación
1. **recepcion** → *Nueva orden* → cliente María Pérez → equipo → tipo *Computadores* → crear.
2. En la orden, **Asignar técnico**: solo aparece Carlos (Laura no tiene esa especialidad) → *Regla 1*.
3. **tecnico1** → *Mis órdenes* → abrir → *Avanzar a En diagnóstico* → *Registrar diagnóstico*.
4. Todavía como técnico: usar el repuesto **Disco SSD 480 GB** × 2 (hay 1) → error de stock y la orden pasa a *Esperando repuesto*.
5. **proveedor1** → ve que el SSD “necesita reposición” → *Registrar pedido* (5) → *Registrar entrega* → el stock sube.
6. **tecnico1** → usar el SSD otra vez, registrar horas (por ejemplo 2) → *Avanzar a Completado*.
7. **recepcion** → abrir la orden → *Avanzar a Entregado*.
8. **admin** → *Reportes*: costo = 2 h × 25.000 + repuestos → *Regla 3*.
9. **cliente1** → ve su orden, historial y costo.

### Swagger (OpenAPI)
Cada servicio expone su documentación en `/swagger-ui.html` (y el JSON en `/v3/api-docs`):
8081 clientes y equipos · 8082 técnicos · 8083 repuestos · 8084 órdenes · 8085 reportes.

---

## 4. Variables de entorno

| Variable | Usada por | Para qué |
|---|---|---|
| `MONGODB_URI` | todos | URL del cluster de MongoDB (Atlas). **Sin** nombre de base: cada servicio fija la suya. |
| `PORT` | todos | Puerto (Render lo define solo; en local se usa el de cada servicio). |
| `CLIENTES_URL`, `TECNICOS_URL`, `REPUESTOS_URL` | `ms-ordenes`, `api-gateway` | URL de esos servicios. |
| `ORDENES_URL` | `ms-reportes`, `api-gateway` | URL de `ms-ordenes`. |
| `REPORTES_URL` | `api-gateway` | URL de `ms-reportes`. |
| `GATEWAY_URL` | `web` | URL del `api-gateway` (la web solo llama a esta). |

La URL de conexión **nunca va dentro del código**: se entrega por variable de entorno.

---

## 5. MongoDB Atlas (base de datos en la nube)

1. Crea una cuenta y un **cluster gratuito** en MongoDB Atlas.
2. *Database Access* → crea un usuario y contraseña (si la contraseña tiene símbolos como `@` o `#`, codifícalos en la URL).
3. *Network Access* → permite el acceso desde Render. En un trabajo de clase suele usarse `0.0.0.0/0`
   (abre el acceso a cualquier IP: no lo dejes así en un proyecto real).
4. *Connect → Drivers* → copia la URL (`mongodb+srv://usuario:clave@cluster.xxxxx.mongodb.net/?retryWrites=true&w=majority`).
   Esa es tu `MONGODB_URI`. Las bases `clientes_db`, `tecnicos_db`, `repuestos_db`, `ordenes_db`, `reportes_db` y
   `web_db` se crean solas dentro del mismo cluster al guardar el primer dato.
5. Con **Compass** puedes conectarte con esa misma URL para ver los datos.

## 6. Despliegue en Render

Render construye cada servicio a partir de su `Dockerfile`. Crea **un *Web Service* por cada módulo** (7 en total):

1. *New → Web Service* → conecta el repositorio donde está este proyecto.
2. **Root Directory**: la carpeta del módulo (por ejemplo `ms-ordenes`). **Runtime/Language**: *Docker*.
3. En *Environment* agrega las variables de la sección 4. Las URL de los servicios son las que Render asigna
   (`https://ms-ordenes.onrender.com`, etc.), así que se despliega en este orden:
   `ms-clientes-equipos`, `ms-tecnicos`, `ms-repuestos` → `ms-ordenes` → `ms-reportes` → `api-gateway`
   (con las 5 URLs anteriores) → `web` (con `GATEWAY_URL` = URL del gateway).
4. Comprueba cada microservicio en `https://<servicio>.onrender.com/swagger-ui.html` y la aplicación en la URL de `web`.

En el plan gratuito Render “duerme” los servicios tras un rato sin uso; la primera petición puede tardar.

---

## 7. Decisiones y límites (para tenerlos claros)

- **Seguridad:** los roles se controlan en `web`. El gateway y los microservicios no tienen autenticación propia,
  así que en un entorno real deberían quedar en una red privada, accesibles solo desde `web`.
- **Concurrencia del stock:** el descuento de stock es simple (lee y guarda). Con muchos usuarios simultáneos habría
  que usar una actualización atómica en MongoDB.
- **Paso “Esperando repuesto”:** el flujo es estricto, así que toda orden pasa por ese estado aunque no falte repuesto.
  Confirma con el docente si debe poder omitirse.
- **Estado de la verificación:** el código se escribió y revisó, pero en el entorno donde se generó **no hubo Maven, red
  ni MongoDB para compilarlo y ejecutarlo**. La primera vez que lo corras puede aparecer algún error de compilación
  menor; si pasa, envíame el mensaje exacto y lo corrijo.
