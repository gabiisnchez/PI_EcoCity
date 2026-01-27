# EcoCity - Ciudad Inteligente

**EcoCity** es una aplicación móvil Android nativa diseñada para empoderar a los ciudadanos en la gestión de incidencias urbanas. Este proyecto forma parte de la iniciativa "Ciudad Inteligente" del Ayuntamiento, ofreciendo una solución híbrida que combina la robustez de los sistemas locales con la versatilidad de la nube.

## 1. Contexto del Proyecto

El objetivo principal es permitir a los usuarios reportar incidencias (baches, farolas rotas, basura, etc.) en tiempo real. La arquitectura del sistema está diseñada para ser resiliente, soportando operaciones sin conexión a internet y sincronización automática cuando la red está disponible.

## 2. Descripción Funcional General

La aplicación ofrecerá las siguientes funcionalidades principales:

*   **Gestión de Identidad**: Registro e inicio de sesión seguro.
*   **Reporte de Incidencias**: Creación de alertas con título, descripción, nivel de urgencia, evidencia multimedia (foto/audio) y ubicación GPS.
*   **Sincronización Inteligente**: Operatividad offline con sincronización diferida (Offline-First).
*   **Soporte Técnico**: Canal de comunicación directo vía TCP/IP (Sockets).

### Estructura del Proyecto

El proyecto sigue una organización de carpetas basada en la arquitectura **MVC**, separando claramente las responsabilidades:

```text
EcoCity/
├── app/
│   ├── src/
│   │   ├── main/
│   │       ├── java/com/ecocity/
│   │       │   ├── controller/      # Lógica de negocio y comunicación
│   │       │   │   ├── LoginController.java
│   │       │   │   └── IncidentController.java
│   │       │   ├── model/           # Datos y Base de Datos (SQLite)
│   │       │   │   ├── Incident.java
│   │       │   │   ├── User.java
│   │       │   │   └── DatabaseHelper.java
│   │       │   ├── view/            # Interfaz de Usuario (Activities)
│   │       │   │   ├── LoginActivity.java
│   │       │   │   ├── IncidentListActivity.java
│   │       │   │   ├── CreateIncidentActivity.java
│   │       │   │   └── adapter/     # Adaptadores para RecyclerView
│   │       │   │       └── IncidentAdapter.java
│   │       ├── res/
│   │           ├── layout/          # Diseños XML
│   │           └── values/
├── build.gradle
└── README.md
```

---

## 3. Estado del Proyecto: Hito 1 Completado

Actualmente, el proyecto ha completado el **Hito 1**, centrado en la experiencia de usuario esencial y la persistencia de datos local.

### 📅 Hito 1: Experiencia de Usuario y Persistencia Local

**Objetivo**: Establecer la estructura visual de la aplicación y garantizar su funcionamiento sin conexión a internet mediante base de datos local.

#### Funcionalidades Implementadas

**1. Interfaz de Usuario (UI/UX)**
*   **Pantalla de Login**: Diseño visual y validación de credenciales.
*   **Listado de Incidencias**: Visualización mediante `RecyclerView` con tarjetas personalizadas para cada reporte.
*   **Formulario de Alta**: Pantalla para crear nuevas incidencias con validaciones de entrada de datos.

**2. Datos Locales (Persistencia)**
*   **Base de Datos**: Implementación de SQLite para el almacenamiento local.
*   **CRUD Completo**:
    *   **Crear**: Registrar nuevas incidencias en el dispositivo.
    *   **Leer**: Consultar el listado de incidencias guardadas.
    *   **Editar**: Modificar datos de una incidencia existente.
    *   **Borrar**: Eliminar incidencias de la base de datos local.

> **Próximos Pasos (Hitos Futuros)**
> *   *Hito 2: Integración de características multimedia (Cámara) y sensores (GPS).*
> *   *Hito 3: Sincronización con la nube (Firebase) e implementación de Sockets TCP.*
