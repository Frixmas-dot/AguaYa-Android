# 💧 AguaYa — Plataforma Ciudadana para la Gestión de la Crisis Hídrica en Santa Marta

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Node.js](https://img.shields.io/badge/Backend-Node.js%20Express-brightgreen.svg)](https://nodejs.org/)
[![Database](https://img.shields.io/badge/Database-MySQL%208.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> Plataforma cívica colaborativa diseñada para visibilizar, georreferenciar y atender el desabastecimiento de agua potable, fugas y mala calidad del suministro en Santa Marta, Magdalena.

---

## 🏛️ Arquitectura del Sistema (Cliente / Servidor)

El sistema implementa una arquitectura distribuida desacoplada en tres capas:

```
[ Cliente Móvil Android (Java - MVVM) ]
                 │
                 ▼  (HTTP REST / JSON / Multipart)
[ Servidor Backend (Node.js + Express API) ]
                 │
                 ▼  (TCP Pool 3306)
[ Base de Datos Relacional (MySQL 8.0) ]
```

1. **Cliente Móvil:** Desarrollado en Android nativo (Java) bajo el patrón **MVVM** (Model-View-ViewModel), utilizando `LiveData`, `ViewBinding` y `Navigation Component`. Incorpora un **Selector Dinámico de IP** para cambiar de servidor en red local o nube sin recompilar.
2. **Servidor Backend:** API RESTful modular construida con Node.js y Express, con soporte para subida de fotos a CDN (Cloudinary) y saneamiento de entradas con consultas preparadas.
3. **Persistencia:** Base de datos relacional MySQL 8.0 desplegada sobre **Ubuntu Server 24.04 LTS** en red de área local (LAN).

---

## 📦 Dependencias y Requerimientos del Sistema (Equivalente a `requirements.txt`)

En este ecosistema tecnológico (Node.js y Android), las dependencias equivalentes a `requirements.txt` se gestionan a través de los siguientes manifiestos:

### 1. Backend Node.js (`package.json`)
| Paquete | Versión | Propósito en el Proyecto |
|---|:---:|---|
| `express` | `^4.19.2` | Framework web y enrutador de la API RESTful |
| `mysql2` | `^3.9.7` | Cliente de conexión a la base de datos MySQL con soporte de Promises y Pool |
| `dotenv` | `^16.4.5` | Carga segura de variables de entorno (`.env`) |
| `cors` | `^2.8.5` | Habilitación de Cross-Origin Resource Sharing |
| `multer` | `^1.4.5-lts.1` | Procesamiento de formularios `multipart/form-data` para subida de imágenes |
| `cloudinary` | `^2.2.0` | SDK oficial para almacenamiento y optimización de imágenes en la nube |
| `multer-storage-cloudinary` | `^4.0.0` | Motor de almacenamiento directo entre Multer y Cloudinary |
| `nodemon` *(dev)* | `^3.1.3` | Recarga automática del servidor durante desarrollo |

### 2. Cliente Móvil Android (`build.gradle` / `libs.versions.toml`)
| Dependencia / Librería | Versión | Propósito en el Proyecto |
|---|:---:|---|
| `com.squareup.retrofit2:retrofit` | `3.0.0` | Cliente HTTP tipo-seguro para consumir la API REST |
| `com.squareup.retrofit2:converter-gson` | `3.0.0` | Serialización y deserialización automática de JSON |
| `com.squareup.okhttp3:okhttp` | `4.12.0` | Interceptores de red (logging y reescritura dinámica de Host/IP) |
| `org.osmdroid:osmdroid-android` | `6.1.20` | Cartografía interactiva OpenStreetMap sin costo de licencias |
| `com.github.PhilJay:MPAndroidChart` | `v3.1.0` | Gráficas analíticas de barras, donas de resolución y métricas |
| `com.github.bumptech.glide:glide` | `4.16.0` | Carga asíncrona y almacenamiento en caché de imágenes |
| `androidx.navigation:navigation-fragment` | `2.9.0` | Enrutamiento y navegación declarativa entre fragmentos |
| `androidx.lifecycle:lifecycle-viewmodel` | `2.9.0` | Separación de lógica de negocio y ciclo de vida (MVVM) |
| `com.google.android.material:material` | `1.14.0` | Componentes visuales bajo lineamientos Material Design 3 |

---

## 🚀 Despliegue y Ejecución

### Requisitos Previos
* **Node.js:** Versión 18.x o superior.
* **MySQL Server:** Versión 8.0 o superior.
* **Android:** Dispositivo físico o emulador con Android 8.0 (API 26) o superior.

### 1. Puesta en Marcha del Backend (Ubuntu Server o Local)
```bash
# 1. Clonar el repositorio o ingresar a la carpeta del backend
cd aguaya-backend

# 2. Instalar dependencias
npm install

# 3. Configurar variables de entorno (.env)
# DB_HOST=localhost
# DB_USER=root
# DB_PASSWORD=tu_clave
# DB_NAME=aguaya
# PORT=3001

# 4. Importar base de datos
mysql -u root -p < schema.sql

# 5. Iniciar el servidor
node server.js
```

### 2. Puesta en Marcha de la App Móvil (Android)
1. Instalar el archivo `AguaYa-Final.apk` en el dispositivo móvil.
2. Asegurarse de que el dispositivo esté en la misma red Wi-Fi o Hotspot del servidor.
3. Abrir la aplicación, tocar el icono de configuración **(⚙️)** en la cabecera principal e ingresar la IP del servidor (ej: `http://192.168.x.x:3001/`).
4. Presionar **Guardar y Conectar**.

---

## 👥 Autores
* **Friedrick**
* **Jesús Torregroza**

Proyecto desarrollado para el **2do Seguimiento de Proyecto de Software**, Universidad del Magdalena.
