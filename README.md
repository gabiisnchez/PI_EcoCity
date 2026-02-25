# 🌱 EcoCity – Ciudad Inteligente

<p align="center">
  <img src="https://img.shields.io/badge/Plataforma-Android-3DDC84?style=flat&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Lenguaje-Java-007396?style=flat&logo=java&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Base_de_Datos-SQLite_%7C_Firestore-FFCA28?style=flat&logo=firebase&logoColor=black" alt="BBDD">
  <img src="https://img.shields.io/badge/Inteligencia_Artificial-Gemini_AI-1A73E8?style=flat&logo=googlebard&logoColor=white" alt="AI">
  <img src="https://img.shields.io/badge/Arquitectura-Offline--First-blueviolet?style=flat" alt="Arch">
</p>

**EcoCity** es una aplicación móvil **Android nativa** diseñada para empoderar a la ciudadanía en la gestión de incidencias urbanas 🏙️. Forma parte de la iniciativa **"Ciudad Inteligente"** del Ayuntamiento, combinando la robustez de los sistemas locales con la flexibilidad de la nube ☁️. Construida como **Proyecto Integrador (PI)** para el ciclo de Desarrollo de Aplicaciones Multiplataforma (DAM).

---

## 📌 Índice

* 🚀 [Contexto del Proyecto](#-contexto-del-proyecto)
* ✨ [Características Principales](#-características-principales)
* 🛠️ [Stack Tecnológico y Arquitectura](#️-stack-tecnológico-y-arquitectura)
* 🗂️ [Estructura del Proyecto](#️-estructura-del-proyecto)
* 🚀 [Instalación y Ejecución](#-instalación-y-ejecución)
* 👥 [Autores](#-autores)
* 📧 [Contacto](#-contacto)

---

## 🚀 Contexto del Proyecto

El objetivo principal de **EcoCity** es permitir a los usuarios **reportar incidencias urbanas en tiempo real** como:

* 🕳️ Baches
* 💡 Farolas rotas
* 🗑️ Acumulación de basura
* 🚧 Otros problemas urbanos

La aplicación está diseñada bajo una arquitectura **resiliente y Offline-First**, permitiendo su uso **sin conexión a internet** y sincronizando automáticamente cuando la red está disponible 📶.

---

## ✨ Características Principales

*   🔐 **Autenticación Segura:** Registro e inicio de sesión integrados con Firebase Authentication y perfiles de usuario.
*   📡 **Arquitectura Offline-First:** La aplicación está diseñada para funcionar sin conexión a internet. Todas las acciones (Crear, Editar, Borrar incidencias) se guardan instantáneamente en una base de datos local SQLite ultrarrápida.
*   🔄 **Sincronización Inteligente en 2º Plano:** Un monitor de red detecta cuándo vuelve la cobertura e inicia hilos de concurrencia (`Threads`) asíncronos para subir los datos locales a la nube (Firebase Firestore) sin bloquear la interfaz.
*   📸 **Soporte Multimedia & Hardware:** Integración nativa con la cámara del dispositivo usando `FileProvider` y `ActivityResultContracts` para adjuntar pruebas fotográficas.
*   🗺️ **Geolocalización Nativa:** Integración con Google Maps SDK y Location Services (GPS) para indicar y visualizar el punto exacto de la incidencia, ordenando el listado por cercanía.
*   💬 **Chat de Soporte en Tiempo Real:** Implementación de una sala de chat grupal técnica usando conexiones de sockets TCP puras en **Java (Clases Socket/ServerSocket)**, demostrando control de red de bajo nivel.
*   🤖 **Asistente Virtual con IA:** Integración nativa con la **API REST de Google Gemini (Generative AI)**. El soporte técnico cuenta tus incidencias asíncronamente en SQLite y nutre de contexto en tiempo real al modelo de lenguaje para darte respuestas precisas.
*   🎨 **Diseño Material:** Interfaz limpia, minimalista e intuitiva respetando las guías de Material Design de Google (Edge-to-Edge, Componentes MUI).

---

## 🛠️ Stack Tecnológico y Arquitectura

El proyecto abarca tecnologías modernas de desarrollo Android y servicios BaaS, y cumple con los currículos de las asignaturas técnicas del ciclo formativo:

| Categoría | Tecnologías Utilizadas |
| :--- | :--- |
| **Desarrollo Móvil (PMDM / DI)** | `Java`, `Android SDK`, `Material Design Components`, `Activity Result API` |
| **Acceso a Datos (AD)** | `SQLiteOpenHelper`, `Cursor`, Listas en Memoria, `ContentValues` |
| **Servicios en la Nube** | `Firebase Authentication`, `Cloud Firestore (NoSQL)` |
| **APIs REST & Inteligencia Artificial** | `Google Gemini API (Generative Language)`, `Retrofit 2`, `Gson` |
| **Servicios y Procesos (PSP)** | `Threads / Runnables`, `Concurrencia GUI (runOnUiThread)`, Sockets TCP, Callbacks asíncronos |
| **Sensores y APIs Externas** | `Google Maps API`, `Fused Location Provider`, Cámara |

### 🏗️ Sincronización (Offline-First)

El núcleo técnico de EcoCity es su robustez frente a pérdidas de conexión, diseñada bajo un patrón DAO híbrido:
1. **Capa Local (Fuente de la Verdad):** Toda operación (CRUD) se persiste inmediatamente contra la base de datos `SQLite` local con *Optimistic UI Updates*.
2. **Capa de Sincronización:** Una bandera (`is_synced`) marca el estado del registro. El `NetworkMonitor` "escucha" los cambios del sistema usando `NetworkCallback`.
3. **Capa Cloud (Respaldo):** Al recuperar red, un hilo secundario vuelca silenciosamente los registros locales pendientes hacia `Firestore`.

---

## 🗂️ Estructura del Proyecto

El proyecto sigue el patrón de arquitectura **MVC (Model–View–Controller)**:

```text
EcoCity/
├── app/
│   ├── src/
│   │   ├── main/
│   │       ├── java/com/ecocity/
│   │       │   ├── controller/                        # Lógica de negocio y comunicación
│   │       │   ├── database/                          # SQLite (DAO, DbHelper)
│   │       │   ├── model/                             # Datos (Incidencia, Usuario)
│   │       │   ├── ui/                                # Interfaz de Usuario (Activities, Adapters)
│   │       │   └── utils/                             # Utilidades (NetworkMonitor, SessionManager)
│   │       ├── res/
│   │           ├── layout/                            # Diseños XML (Material Design)
│   │           └── values/
├── build.gradle
└── README.md
```

---

## 🚀 Instalación y Ejecución

Para desplegar este proyecto en tu entorno local:

1.  **Clonar el repositorio:**
    ```bash
    git clone https://ruta-a-tu-repositorio/PI_EcoCity.git
    cd PI_EcoCity
    ```
2.  **Configurar credenciales (Requisito indispensable):**
    - Añadir el archivo generado de configuración de Firebase `google-services.json` dentro del directorio `app/`.
    - Registrar tu clave de API de **Google Maps** (`MAPS_API_KEY`) en el archivo `local.properties`.
3.  **Construir con Gradle:**
    Abre el proyecto en **Android Studio**, deja que Gradle sincronice las dependencias y ejecuta `assembleDebug` para compilar el APK.
4.  **Correr el Servidor de Chat (Requisito para el Chat TCP):**
    Debes levantar el script servidor Java de Sockets (puerto 5000) de manera paralela si deseas testear el módulo TCP de soporte.

---

## 👥 Autores

### Alejandro Martínez Bou

[![GitHub](https://img.shields.io/badge/GitHub-AlejandroBou-181717?style=for-the-badge&logo=github)](https://github.com/AlejandroBou)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Alejandro_Martínez_Bou-0A66C2?style=for-the-badge&logo=linkedin)](https://www.linkedin.com/in/alejandro-mart%C3%ADnez-bou-3666a6349/)

### Gabriel Sánchez Heredia

[![GitHub](https://img.shields.io/badge/GitHub-gabiisnchez-181717?style=for-the-badge&logo=github)](https://github.com/gabiisnchez)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Gabriel_Sánchez_Heredia-0A66C2?style=for-the-badge&logo=linkedin)](https://www.linkedin.com/in/gabrielsanher/)

---

## 📧 Contacto

Para preguntas o sugerencias sobre el proyecto:

- 📫 Abre un issue en el repositorio
- 💬 Inicia una discusión en la pestaña Discussions
- ⭐ Da una estrella si te ha sido útil

---

<div align="center">

⭐ **¡No olvides dar una estrella al proyecto si te ha sido útil!** ⭐

💚 *EcoCity – Construyendo ciudades más inteligentes y sostenibles.*

**Desarrollado con ❤️ como proyecto de PSP, PMDM y DI.**

</div>
