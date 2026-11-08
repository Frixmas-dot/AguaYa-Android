// ============================================================
//  AguaYa — Backend v2.0  (server.js)
//  Nuevas funciones: fotos Cloudinary, confirmaciones, admin
// ============================================================
require('dotenv').config()
const express    = require('express')
const cors       = require('cors')
const mysql      = require('mysql2/promise')
const cloudinary = require('cloudinary').v2
const multer     = require('multer')
const { CloudinaryStorage } = require('multer-storage-cloudinary')
const crypto     = require('crypto')

const app  = express()
const PORT = process.env.PORT || 3001

// ── Middlewares ──────────────────────────────────────────────
app.use(cors()) // Permite peticiones desde el frontend web (Nginx / localhost) y app móvil
app.use(express.json())

// ── Cloudinary ───────────────────────────────────────────────
cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
  api_key:    process.env.CLOUDINARY_API_KEY,
  api_secret: process.env.CLOUDINARY_API_SECRET,
})

const storage = new CloudinaryStorage({
  cloudinary,
  params: {
    folder:         'aguaya-reportes',
    allowed_formats: ['jpg', 'jpeg', 'png', 'webp'],
    transformation: [{ width: 1200, crop: 'limit', quality: 'auto' }],
  },
})
const upload = multer({ storage, limits: { fileSize: 25 * 1024 * 1024 } })

// ── Pool MySQL ───────────────────────────────────────────────
const pool = mysql.createPool({
  host:               process.env.DB_HOST     || 'localhost',
  port:               process.env.DB_PORT     || 3306,
  user:               process.env.DB_USER     || 'root',
  password:           process.env.DB_PASSWORD || '',
  database:           process.env.DB_NAME     || 'aguaya',
  waitForConnections: true,
  connectionLimit:    10,
})

async function checkDB() {
  try {
    const conn = await pool.getConnection()
    console.log('✅ Conectado a MySQL')
    conn.release()
  } catch (err) {
    console.error('❌ Error conectando a MySQL:', err.message)
    process.exit(1)
  }
}

// ── Metadatos ────────────────────────────────────────────────
const META_TIPO = {
  'Sin agua':     { icon: '🚱', color: '#ef4444' },
  'Baja presión': { icon: '📉', color: '#f97316' },
  'Agua sucia':   { icon: '🟤', color: '#a16207' },
  'Fuga':         { icon: '🔧', color: '#0ea5e9' },
}

const ID_TO_TIPO = {
  'sin-agua':     'Sin agua',
  'baja-presion': 'Baja presión',
  'agua-sucia':   'Agua sucia',
  'fuga':         'Fuga',
}


function tiempoRelativo(fecha) {
  const mins = Math.floor((Date.now() - new Date(fecha)) / 60000)
  if (mins < 60)   return `Hace ${mins}m`
  if (mins < 1440) return `Hace ${Math.floor(mins / 60)}h`
  return `Hace ${Math.floor(mins / 1440)}d`
}

function enrichReporte(r) {
  const meta = META_TIPO[r.tipo] || {}
  return {
    ...r,
    icon:            meta.icon  || '❓',
    color:           meta.color || '#94a3b8',
    tiempo:          tiempoRelativo(r.created_at),
    confirmaciones:  r.confirmaciones || 0,
  }
}
const COORDENADAS_BARRIOS = {}
async function geocodificar(barrio) {
  const key = barrio.trim().toLowerCase()
  if (COORDENADAS_BARRIOS[key]) return COORDENADAS_BARRIOS[key]

  try {
    const [rows] = await pool.query('SELECT lat, lng FROM barrios WHERE LOWER(nombre) = ? LIMIT 1', [key])
    if (rows && rows.length > 0) {
      COORDENADAS_BARRIOS[key] = { lat: parseFloat(rows[0].lat), lng: parseFloat(rows[0].lng) }
      return COORDENADAS_BARRIOS[key]
    }
  } catch (_) {}

  try {
    const query = encodeURIComponent(`${barrio}, Santa Marta, Colombia`)
    const url   = `https://nominatim.openstreetmap.org/search?q=${query}&format=json&limit=1`
    const resp  = await fetch(url, { headers: { 'Accept-Language': 'es', 'User-Agent': 'AguaYa/2.0' } })
    const data  = await resp.json()
    if (data?.length > 0) return { lat: parseFloat(data[0].lat), lng: parseFloat(data[0].lon) }
  } catch (_) {}

  return {
    lat: 11.2408 + (Math.random() - 0.5) * 0.06,
    lng: -74.199 + (Math.random() - 0.5) * 0.06,
  }
}

// ============================================================
//  RUTAS — REPORTES
// ============================================================

// GET /api/reportes
app.get('/api/reportes', async (req, res) => {
  try {
    const { tipo, estado, urgencia, fecha } = req.query
    let sql = `
      SELECT r.*, COUNT(c.id) AS confirmaciones
      FROM reportes r
      LEFT JOIN confirmaciones c ON c.reporte_id = r.id
      WHERE 1=1
    `
    const params = []

    if (estado === 'archivado') {
      sql += ' AND r.activo = 0'
    } else {
      sql += ' AND r.activo = 1'
      if (estado)   { sql += ' AND r.estado = ?';   params.push(estado) }
    }

    if (tipo)     { sql += ' AND r.tipo = ?';     params.push(tipo) }
    if (urgencia) { sql += ' AND r.urgencia = ?'; params.push(urgencia) }
    if (fecha === 'hoy') {
      sql += ' AND DATE(r.created_at) = CURDATE()'
    } else if (fecha === 'semana') {
      sql += ' AND r.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)'
    }

    sql += ' GROUP BY r.id ORDER BY r.created_at DESC'

    const [rows] = await pool.query(sql, params)
    res.json(rows.map(enrichReporte))
  } catch (err) {
    console.error(err)
    res.status(500).json({ error: 'Error al obtener reportes' })
  }
})

// GET /api/reportes/:id
app.get('/api/reportes/:id', async (req, res) => {
  try {
    const [rows] = await pool.query(
      `SELECT r.*, COUNT(c.id) AS confirmaciones
       FROM reportes r
       LEFT JOIN confirmaciones c ON c.reporte_id = r.id
       WHERE r.id = ?
       GROUP BY r.id`,
      [req.params.id]
    )
    if (rows.length === 0) return res.status(404).json({ error: 'No encontrado' })
    res.json(enrichReporte(rows[0]))
  } catch (err) {
    res.status(500).json({ error: 'Error al obtener reporte' })
  }
})

// POST /api/reportes  (con foto opcional)
app.post('/api/reportes', upload.single('foto'), async (req, res) => {
  try {
    const { barrio, tipoId, desc, urgencia } = req.body

    if (!barrio || !tipoId || !desc)
      return res.status(400).json({ error: 'Faltan campos obligatorios' })
    if (desc.trim().length < 10)
      return res.status(400).json({ error: 'Descripción mínimo 10 caracteres' })

    const tipo = ID_TO_TIPO[tipoId] || tipoId
    if (!META_TIPO[tipo])
      return res.status(400).json({ error: 'Tipo inválido' })

    // 1. Si el celular envió coordenadas GPS reales, usarlas prioritariamente
    let lat = req.body.lat ? parseFloat(req.body.lat) : null
    let lng = req.body.lng ? parseFloat(req.body.lng) : null

    // 2. Si no hay GPS, geocodificar por el nombre del barrio (BD o OpenStreetMap)
    if (!lat || !lng || isNaN(lat) || isNaN(lng)) {
      const geo = await geocodificar(barrio)
      lat = geo.lat
      lng = geo.lng
    }

    const foto_url = req.file?.path || null

    const [result] = await pool.query(
      `INSERT INTO reportes (barrio, tipo, descripcion, urgencia, lat, lng, foto_url)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [barrio.trim(), tipo, desc.trim(), urgencia || 'media', lat, lng, foto_url]
    )

    const [rows] = await pool.query(
      `SELECT r.*, 0 AS confirmaciones FROM reportes r WHERE r.id = ?`,
      [result.insertId]
    )
    res.status(201).json(enrichReporte(rows[0]))
  } catch (err) {
    console.error(err)
    res.status(500).json({ error: 'Error al guardar reporte' })
  }
})

// PATCH /api/reportes/:id/estado
app.patch('/api/reportes/:id/estado', async (req, res) => {
  try {
    const { estado } = req.body
    if (!['pendiente', 'en_proceso', 'resuelto'].includes(estado))
      return res.status(400).json({ error: 'Estado inválido' })

    const [result] = await pool.query(
      'UPDATE reportes SET estado = ? WHERE id = ?',
      [estado, req.params.id]
    )
    if (result.affectedRows === 0)
      return res.status(404).json({ error: 'No encontrado' })

    res.json({ ok: true, estado })
  } catch (err) {
    res.status(500).json({ error: 'Error al actualizar estado' })
  }
})

// DELETE /api/reportes/:id  (Soft Delete / Borrado Lógico)
app.delete('/api/reportes/:id', async (req, res) => {
  try {
    const [result] = await pool.query(
      'UPDATE reportes SET activo = 0, deleted_at = NOW() WHERE id = ?',
      [req.params.id]
    )
    if (result.affectedRows === 0)
      return res.status(404).json({ error: 'No encontrado' })

    res.json({ ok: true, mensaje: 'Reporte archivado exitosamente' })
  } catch (err) {
    console.error(err)
    res.status(500).json({ error: 'Error al archivar reporte' })
  }
})

// PATCH /api/reportes/:id/restaurar  (Restaurar Reporte Archivado)
app.patch('/api/reportes/:id/restaurar', async (req, res) => {
  try {
    const [result] = await pool.query(
      'UPDATE reportes SET activo = 1, deleted_at = NULL WHERE id = ?',
      [req.params.id]
    )
    if (result.affectedRows === 0)
      return res.status(404).json({ error: 'No encontrado' })

    res.json({ ok: true, mensaje: 'Reporte restaurado exitosamente' })
  } catch (err) {
    console.error(err)
    res.status(500).json({ error: 'Error al restaurar reporte' })
  }
})

// GET /api/barrios  (Catálogo Oficial de Barrios por Localidad)
app.get('/api/barrios', async (req, res) => {
  try {
    const [rows] = await pool.query('SELECT * FROM barrios ORDER BY nombre ASC')
    res.json(rows)
  } catch (err) {
    console.error(err)
    res.status(500).json({ error: 'Error al obtener barrios' })
  }
})

// ============================================================
//  RUTAS — CONFIRMACIONES
// ============================================================

// POST /api/reportes/:id/confirmar
app.post('/api/reportes/:id/confirmar', async (req, res) => {
  try {
    const ip     = req.headers['x-forwarded-for'] || req.socket.remoteAddress || 'unknown'
    const ipHash = crypto.createHash('sha256').update(ip).digest('hex')

    await pool.query(
      'INSERT IGNORE INTO confirmaciones (reporte_id, ip_hash) VALUES (?, ?)',
      [req.params.id, ipHash]
    )

    const [[{ total }]] = await pool.query(
      'SELECT COUNT(*) AS total FROM confirmaciones WHERE reporte_id = ?',
      [req.params.id]
    )

    res.json({ ok: true, confirmaciones: total })
  } catch (err) {
    console.error(err)
    res.status(500).json({ error: 'Error al confirmar' })
  }
})

// ============================================================
//  RUTAS — ESTADÍSTICAS
// ============================================================
app.get('/api/estadisticas', async (req, res) => {
  try {
    const [[totales]]   = await pool.query('SELECT COUNT(*) AS total FROM reportes WHERE activo = 1')
    const [[barrios]]   = await pool.query('SELECT COUNT(DISTINCT barrio) AS total FROM reportes WHERE activo = 1')
    const [[resueltos]] = await pool.query("SELECT COUNT(*) AS total FROM reportes WHERE activo = 1 AND estado = 'resuelto'")
    const [[enProceso]] = await pool.query("SELECT COUNT(*) AS total FROM reportes WHERE activo = 1 AND estado = 'en_proceso'")
    const [[pendientes]]= await pool.query("SELECT COUNT(*) AS total FROM reportes WHERE activo = 1 AND estado = 'pendiente'")

    const total = totales.total || 1
    const pctResueltos  = Math.round((resueltos.total  / total) * 100)
    const pctEnProceso  = Math.round((enProceso.total  / total) * 100)
    const pctPendientes = 100 - pctResueltos - pctEnProceso

    const [porTipo] = await pool.query(
      'SELECT tipo, COUNT(*) AS count FROM reportes WHERE activo = 1 GROUP BY tipo ORDER BY count DESC'
    )

    const [porBarrio] = await pool.query(`
      SELECT barrio, COUNT(*) AS reportes,
             MAX(tipo) AS problema
      FROM reportes
      WHERE activo = 1
      GROUP BY barrio
      ORDER BY reportes DESC
      LIMIT 6
    `)

    res.json({
      totalReportes: total,
      barriosUnicos: barrios.total,
      pctResueltos,
      pctEnProceso,
      pctPendientes,
      porTipo: porTipo.map(r => ({
        tipo:  r.tipo,
        label: r.tipo,
        count: r.count,
        pct:   Math.round((r.count / total) * 100),
        icon:  META_TIPO[r.tipo]?.icon  || '❓',
        color: META_TIPO[r.tipo]?.color || '#94a3b8',
      })),
      porBarrio: porBarrio.map(r => ({
        nombre:   r.barrio,
        reportes: r.reportes,
        problema: r.problema,
        color:    META_TIPO[r.problema]?.color || '#94a3b8',
      })),
    })
  } catch (err) {
    console.error(err)
    res.status(500).json({ error: 'Error estadísticas' })
  }
})

// ── Health ───────────────────────────────────────────────────
app.get('/api/health', (_, res) => res.json({ ok: true, ts: new Date() }))

// ── Middleware de Manejo de Errores (Multer y Generales) ──────
app.use((err, req, res, next) => {
  if (err instanceof multer.MulterError) {
    if (err.code === 'LIMIT_FILE_SIZE') {
      return res.status(400).json({ error: 'La foto es demasiado grande (máximo 25 MB)' })
    }
    return res.status(400).json({ error: `Error subiendo archivo: ${err.message}` })
  }
  if (err) {
    console.error('Error del servidor:', err)
    return res.status(500).json({ error: err.message || 'Error interno del servidor' })
  }
  next()
})

// ── Arrancar ─────────────────────────────────────────────────
checkDB().then(() => {
  app.listen(PORT, () => console.log(`🚀 AguaYa API en http://localhost:${PORT}`))
})
