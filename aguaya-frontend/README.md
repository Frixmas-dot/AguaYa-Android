# AguaYa — Guía de instalación con MySQL

## Estructura del proyecto

```
aguaya/
├── aguaya-backend/      ← Node.js + Express (este paquete)
│   ├── server.js
│   ├── schema.sql
│   ├── .env.example
│   └── package.json
└── aguaya-frontend/     ← Tu proyecto Vue existente
    └── src/
        ├── stores/waterReports.js   ← Reemplazar
        └── views/
            ├── MapaView.vue         ← Reemplazar
            └── EstadisticasView.vue ← Reemplazar
```

---

## Paso 1 — Base de datos MySQL

Abre MySQL Workbench o tu terminal y ejecuta:

```bash
mysql -u root -p < schema.sql
```

O copia y pega el contenido de `schema.sql` directamente en MySQL Workbench.

---

## Paso 2 — Configurar el backend

```bash
# Entra a la carpeta del backend
cd aguaya-backend

# Instala dependencias
npm install

# Crea tu archivo .env con tus datos reales
cp .env.example .env
```

Edita `.env` y pon tu contraseña de MySQL:

```
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=tu_password_aqui
DB_NAME=aguaya
PORT=3001
```

---

## Paso 3 — Arrancar el backend

```bash
# Modo normal
node server.js

# Modo desarrollo (se reinicia automáticamente al guardar)
npx nodemon server.js
```

Deberías ver:
```
✅ Conectado a MySQL
🚀 AguaYa API corriendo en http://localhost:3001
```

Verifica que funciona abriendo: http://localhost:3001/api/reportes

---

## Paso 4 — Actualizar el frontend Vue

Copia los archivos al proyecto Vue:

```bash
# Store (reemplaza el existente)
cp waterReports.js  ../aguaya-frontend/src/stores/waterReports.js

# Vistas (reemplaza las existentes)
cp MapaView.vue         ../aguaya-frontend/src/views/MapaView.vue
cp EstadisticasView.vue ../aguaya-frontend/src/views/EstadisticasView.vue
```

---

## Paso 5 — Arrancar todo

**Terminal 1 — Backend:**
```bash
cd aguaya-backend
node server.js
```

**Terminal 2 — Frontend Vue:**
```bash
cd aguaya-frontend
npm run dev
```

Abre http://localhost:5173 — los reportes ahora vienen de MySQL.

---

## Endpoints de la API

| Método | Ruta                          | Descripción                    |
|--------|-------------------------------|--------------------------------|
| GET    | /api/reportes                 | Todos los reportes             |
| GET    | /api/reportes?tipo=Sin agua   | Filtrar por tipo               |
| GET    | /api/reportes/:id             | Un reporte específico          |
| POST   | /api/reportes                 | Crear reporte nuevo            |
| PATCH  | /api/reportes/:id/estado      | Cambiar estado del reporte     |
| DELETE | /api/reportes/:id             | Eliminar reporte               |
| GET    | /api/estadisticas             | Datos agregados para gráficas  |
| GET    | /api/health                   | Verificar que la API funciona  |

---

## Solución de problemas

**Error: ECONNREFUSED al conectar a MySQL**
- Verifica que MySQL está corriendo
- Revisa usuario y contraseña en `.env`

**Error: CORS**
- El backend permite solo `http://localhost:5173` (Vite)
- Si usas otro puerto, edita la línea `cors({ origin: '...' })` en `server.js`

**Nominatim no geocodifica**
- Si no hay internet, se usan coordenadas aleatorias cerca de Santa Marta
- El reporte igual se guarda correctamente
