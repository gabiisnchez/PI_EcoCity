# 🌱 EcoCity – Ciudad Inteligente

**EcoCity** es una aplicación móvil **Android nativa** diseñada para empoderar a la ciudadanía en la gestión de incidencias urbanas 🏙️. Forma parte de la iniciativa **"Ciudad Inteligente"** del Ayuntamiento, combinando la robustez de los sistemas locales con la flexibilidad de la nube ☁️.

---

## 📌 Índice

* 🚀 [Contexto del Proyecto](#-contexto-del-proyecto)
* ⚙️ [Descripción Funcional General](#️-descripción-funcional-general)
* 🗂️ [Estructura del Proyecto](#️-estructura-del-proyecto)
* ✅ [Estado del Proyecto – Hito 1](#-estado-del-proyecto--hito-1)
* 🔮 [Próximos Pasos](#-próximos-pasos)

---

## 🚀 Contexto del Proyecto

El objetivo principal de **EcoCity** es permitir a los usuarios **reportar incidencias urbanas en tiempo real** como:

* 🕳️ Baches
* 💡 Farolas rotas
* 🗑️ Acumulación de basura
* 🚧 Otros problemas urbanos

La aplicación está diseñada bajo una arquitectura **resiliente y Offline-First**, permitiendo su uso **sin conexión a internet** y sincronizando automáticamente cuando la red está disponible 📶.

---

## ⚙️ Descripción Funcional General

La aplicación ofrece las siguientes funcionalidades clave:

### 🔐 Gestión de Identidad

* Registro e inicio de sesión seguro de usuarios.

### 📝 Reporte de Incidencias

* Creación de alertas con:

  * 🏷️ Título
  * 📄 Descripción
  * 🚨 Nivel de urgencia
  * 📷 Evidencia multimedia (foto / audio)
  * 📍 Ubicación GPS

### 🔄 Sincronización Inteligente

* Funcionamiento **offline** con sincronización diferida automática.

### 🛠️ Soporte Técnico

* Canal de comunicación directa mediante **Sockets TCP/IP**.

---

## 🗂️ Estructura del Proyecto

El proyecto sigue el patrón de arquitectura **MVC (Model–View–Controller)**, separando claramente responsabilidades:

```text
EcoCity/
├── app/
│   ├── src/
│   │   ├── main/
│   │       ├── java/com/ecocity/
│   │       │   ├── controller/                        # Lógica de negocio y comunicación
│   │       │   │   ├── LoginController.java
│   │       │   │   └── IncidentController.java
│   │       │   ├── model/                             # Datos y Base de Datos (SQLite)
│   │       │   │   ├── Incident.java
│   │       │   │   ├── User.java
│   │       │   │   └── DatabaseHelper.java
│   │       │   ├── view/                              # Interfaz de Usuario (Activities)
│   │       │   │   ├── LoginActivity.java
│   │       │   │   ├── IncidentListActivity.java
│   │       │   │   ├── CreateIncidentActivity.java
│   │       │   │   └── adapter/                       # Adaptadores para RecyclerView
│   │       │   │       └── IncidentAdapter.java
│   │       ├── res/
│   │           ├── layout/                            # Diseños XML
│   │           └── values/
├── build.gradle
└── README.md
```

---

## ✅ Estado del Proyecto – Hito 1

🎯 **Hito 1 completado con éxito**. Este primer hito se ha centrado en la **experiencia de usuario** y la **persistencia local de datos**.

### 🎨 Interfaz de Usuario (UI/UX)

* 🔑 **Pantalla de Login**

  * Diseño visual limpio
  * Validación de credenciales

* 📋 **Listado de Incidencias**

  * Uso de `RecyclerView`
  * Tarjetas personalizadas por incidencia

* ➕ **Formulario de Alta**

  * Creación de nuevas incidencias
  * Validación de entradas de usuario

### 💾 Persistencia de Datos (Offline)

* 🗄️ Base de datos **SQLite** integrada
* 🔄 **CRUD completo**:

  * ➕ Crear incidencias
  * 👀 Leer incidencias almacenadas
  * ✏️ Editar incidencias existentes
  * 🗑️ Eliminar incidencias

---

## 🔮 Próximos Pasos

📍 **Roadmap del proyecto**:

* 🧭 **Hito 2**: Integración de multimedia (📷 Cámara, 🎤 Audio) y sensores (📍 GPS).
* ☁️ **Hito 3**: Sincronización con la nube (Firebase) y comunicación mediante **Sockets TCP**.

---

## 👥 Autores

### Alejandro Martínez Bou

[![GitHub](https://img.shields.io/badge/GitHub-AlejandroBou-181717?style=for-the-badge&logo=github)](https://github.com/AlejandroBou)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Alejandro_Martínez_Bou-0A66C2?style=for-the-badge&logo=linkedin)](https://www.linkedin.com/in/alejandro-mart%C3%ADnez-bou-3666a6349/)

### Gabriel Sánchez Heredia

[![GitHub](https://img.shields.io/badge/GitHub-gabiisnchez-181717?style=for-the-badge&logo=github)](https://github.com/gabiisnchez)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Gabriel_Sánchez_Heredia-0A66C2?style=for-the-badge&logo=linkedin)](https://www.linkedin.com/in/gabrielsanher/)



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


