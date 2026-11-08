<template>
  <div class="admin-page">

    <!-- ── LOGIN ── -->
    <div v-if="!autenticado" class="login-wrapper">
      <div class="login-card">
        <div class="login-icon">🔐</div>
        <h2>Panel de administración</h2>
        <p>Ingresa la contraseña para continuar</p>

        <div class="login-field">
          <input
            v-model="password"
            :type="mostrarPass ? 'text' : 'password'"
            placeholder="Contraseña"
            @keyup.enter="entrar"
            :class="{ error: errorLogin }"
            autofocus
          />
          <button class="toggle-pass" type="button" @click="mostrarPass = !mostrarPass">
            {{ mostrarPass ? 'X' : '👁️' }}
          </button>
        </div>

        <p v-if="errorLogin" class="login-error">❌ Contraseña incorrecta</p>

        <button class="btn-entrar" @click="entrar">Entrar</button>
      </div>
    </div>

    <!-- ── PANEL ADMIN ── -->
    <template v-else>

      <section class="page-header">
        <div class="container header-inner">
          <div>
            <p class="section-label">Panel de administración</p>
            <h1 class="page-title">Gestión de reportes</h1>
            <p class="page-desc">Actualiza el estado de los reportes ciudadanos.</p>
          </div>
          <button class="btn-salir" @click="salir">🔒 Cerrar sesión</button>
        </div>
      </section>

      <!-- FILTROS -->
      <div class="filters-bar">
        <div class="container filters-inner">
          <span class="filters-label">Estado:</span>
          <button
            v-for="f in filtrosEstado"
            :key="f.id"
            class="filter-btn"
            :class="{ active: filtroActivo === f.id }"
            :style="{ '--fc': f.color }"
            @click="cambiarFiltro(f.id)"
          >
            {{ f.icon }} {{ f.label }}
          </button>
        </div>
      </div>

      <!-- LOADING -->
      <div v-if="store.cargando" class="loading-state">
        <span class="spinner"></span> Cargando reportes...
      </div>

      <!-- TABLA -->
      <div v-else class="container table-wrapper">
        <div v-if="reportes.length === 0" class="empty-state">
          📭 No hay reportes para este filtro
        </div>

        <table v-else class="admin-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Dirección</th>
              <th>Barrio</th>
              <th>Tipo</th>
              <th>Descripción</th>
              <th>Urgencia</th>
              <th>👍</th>
              <th>Fecha</th>
              <th>Foto</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in reportes" :key="r.id" :class="`row-${r.estado}`">
              <td class="td-id">#{{ r.id }}</td>
              <td class="td-dir">{{ r.direccion || '—' }}</td>
              <td class="td-barrio">{{ r.barrio }}</td>
              <td>
                <span class="tag" :style="{ '--tc': r.color }">{{ r.icon }} {{ r.tipo }}</span>
              </td>
              <td class="td-desc">{{ r.descripcion }}</td>
              <td>
                <span class="urgencia-badge" :class="r.urgencia">{{ r.urgencia }}</span>
              </td>
              <td class="td-confirm">{{ r.confirmaciones || 0 }}</td>
              <td class="td-fecha">{{ formatFecha(r.created_at) }}</td>
              <td>
                <a v-if="r.foto_url" :href="r.foto_url" target="_blank" class="foto-link">📷 Ver</a>
                <span v-else class="no-foto">—</span>
              </td>
              <td>
                <span class="estado-badge" :class="r.estado">{{ estadoLabel(r.estado) }}</span>
              </td>
              <td>
                <div class="action-btns">
                  <button v-if="r.estado !== 'en_proceso'" class="btn-accion" @click="cambiarEstado(r.id, 'en_proceso')" title="Marcar en proceso">🟠</button>
                  <button v-if="r.estado !== 'resuelto'"   class="btn-accion" @click="cambiarEstado(r.id, 'resuelto')"   title="Marcar resuelto">✅</button>
                  <button v-if="r.estado !== 'pendiente'"  class="btn-accion" @click="cambiarEstado(r.id, 'pendiente')"  title="Volver a pendiente">🔄</button>
                  <button class="btn-accion btn-eliminar"  @click="eliminar(r.id)" title="Eliminar">🗑️</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

    </template>

    <footer class="footer">
      <div class="container footer-inner">
        <span class="nav-logo">💧 AguaYa</span>
        <p>Proyecto académico · Santa Marta, Colombia</p>
      </div>
    </footer>

  </div>
</template>

<script>
import { useWaterReportsStore } from '@/stores/waterReports'

const ADMIN_PASSWORD = '08042003'

export default {
  name: 'AdminView',

  data() {
    return {
      autenticado:  sessionStorage.getItem('aguaya_admin') === 'true',
      password:     '',
      errorLogin:   false,
      mostrarPass:  false,
      filtroActivo: 'todos',
      filtrosEstado: [
        { id: 'todos',      label: 'Todos',      icon: '📋', color: '#0369a1' },
        { id: 'pendiente',  label: 'Pendientes', icon: '🔴', color: '#dc2626' },
        { id: 'en_proceso', label: 'En proceso', icon: '🟠', color: '#ea580c' },
        { id: 'resuelto',   label: 'Resueltos',  icon: '🟢', color: '#16a34a' },
      ],
    }
  },

  computed: {
    store()    { return useWaterReportsStore() },
    reportes() { return this.store.reportesConMeta },
  },

  mounted() {
    if (this.autenticado) this.store.cargarReportes()
  },

  methods: {
    entrar() {
      if (this.password === ADMIN_PASSWORD) {
        this.autenticado = true
        this.errorLogin  = false
        sessionStorage.setItem('aguaya_admin', 'true')
        this.store.cargarReportes()
      } else {
        this.errorLogin = true
        this.password   = ''
      }
    },

    salir() {
      sessionStorage.removeItem('aguaya_admin')
      this.autenticado = false
      this.password    = ''
    },

    async cambiarFiltro(id) {
      this.filtroActivo = id
      await this.store.cargarReportes(id !== 'todos' ? { estado: id } : {})
    },

    async cambiarEstado(id, estado) {
      await this.store.cambiarEstado(id, estado)
    },

    async eliminar(id) {
      if (!confirm('¿Eliminar este reporte? Esta acción no se puede deshacer.')) return
      await this.store.eliminarReporte(id)
    },

    estadoLabel(e) {
      return { pendiente: 'Pendiente', en_proceso: 'En proceso', resuelto: 'Resuelto' }[e] || e
    },

    formatFecha(fecha) {
      return new Date(fecha).toLocaleDateString('es-CO', {
        day: '2-digit', month: '2-digit', year: '2-digit',
        hour: '2-digit', minute: '2-digit',
      })
    },
  },
}
</script>

<style scoped>
* { box-sizing: border-box; margin: 0; padding: 0; }
.admin-page { font-family: 'Inter', sans-serif; color: #0f172a; background: #f8fafc; min-height: 100vh; display: flex; flex-direction: column; }
.container { max-width: 1200px; margin: 0 auto; padding: 0 24px; }

/* ── LOGIN ── */
.login-wrapper { flex: 1; display: flex; align-items: center; justify-content: center; padding: 60px 24px; background: linear-gradient(160deg, #e0f2fe 0%, #f8fafc 60%); }
.login-card { background: white; border: 1.5px solid #e2e8f0; border-radius: 24px; padding: 48px 40px; width: 100%; max-width: 400px; box-shadow: 0 8px 40px rgba(3,105,161,0.1); display: flex; flex-direction: column; align-items: center; gap: 16px; text-align: center; }
.login-icon { font-size: 48px; }
.login-card h2 { font-family: 'Sora', sans-serif; font-size: 22px; font-weight: 800; }
.login-card > p { font-size: 14px; color: #64748b; }
.login-field { width: 100%; position: relative; margin-top: 8px; }
.login-field input { width: 100%; padding: 13px 44px 13px 16px; border: 1.5px solid #e2e8f0; border-radius: 12px; font-size: 15px; font-family: 'Inter', sans-serif; outline: none; background: #f8fafc; transition: border-color .2s, box-shadow .2s; }
.login-field input:focus { border-color: #0369a1; box-shadow: 0 0 0 3px rgba(3,105,161,0.1); background: white; }
.login-field input.error { border-color: #ef4444; }
.toggle-pass { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; font-size: 16px; }
.login-error { font-size: 13px; color: #ef4444; }
.btn-entrar { width: 100%; padding: 13px; background: #0369a1; color: white; border: none; border-radius: 12px; font-size: 15px; font-weight: 600; font-family: 'Inter', sans-serif; cursor: pointer; transition: background .2s, transform .2s; box-shadow: 0 4px 20px rgba(3,105,161,0.3); }
.btn-entrar:hover { background: #0284c7; transform: translateY(-1px); }

/* ── HEADER ── */
.page-header { padding: 100px 0 40px; background: linear-gradient(160deg, #e0f2fe 0%, #f8fafc 60%); border-bottom: 1px solid #e2e8f0; }
.header-inner { display: flex; align-items: flex-end; justify-content: space-between; flex-wrap: wrap; gap: 16px; }
.section-label { font-size: 12px; font-weight: 600; letter-spacing: 2px; text-transform: uppercase; color: #0369a1; margin-bottom: 10px; }
.page-title { font-family: 'Sora', sans-serif; font-size: clamp(28px, 4vw, 44px); font-weight: 800; margin-bottom: 8px; }
.page-desc { color: #64748b; font-size: 16px; }
.btn-salir { padding: 10px 20px; background: #0f172a; color: white; border: none; border-radius: 20px; font-size: 13px; font-weight: 600; cursor: pointer; font-family: 'Inter', sans-serif; transition: background .2s; }
.btn-salir:hover { background: #1e293b; }

/* ── FILTROS ── */
.filters-bar { background: white; border-bottom: 1px solid #e2e8f0; padding: 12px 0; position: sticky; top: 60px; z-index: 90; }
.filters-inner { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.filters-label { font-size: 13px; color: #94a3b8; font-weight: 500; }
.filter-btn { border: 1.5px solid #e2e8f0; background: white; color: #334155; padding: 6px 14px; border-radius: 20px; font-size: 13px; font-weight: 500; cursor: pointer; transition: all .2s; font-family: 'Inter', sans-serif; }
.filter-btn:hover { border-color: var(--fc); color: var(--fc); }
.filter-btn.active { background: var(--fc); border-color: var(--fc); color: white; }

.loading-state { display: flex; align-items: center; gap: 12px; justify-content: center; padding: 80px 0; font-size: 15px; color: #0369a1; }
.spinner { width: 18px; height: 18px; border: 2px solid #bae6fd; border-top-color: #0369a1; border-radius: 50%; animation: spin .7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ── TABLA ── */
.table-wrapper { padding: 32px 24px 60px; overflow-x: auto; }
.empty-state { text-align: center; padding: 60px 0; color: #94a3b8; font-size: 15px; }
.admin-table { width: 100%; border-collapse: collapse; font-size: 13px; background: white; border-radius: 16px; overflow: hidden; border: 1.5px solid #e2e8f0; }
.admin-table th { text-align: left; font-size: 11px; font-weight: 600; letter-spacing: 1px; text-transform: uppercase; color: #94a3b8; padding: 14px 12px; border-bottom: 1.5px solid #e2e8f0; background: #f8fafc; white-space: nowrap; }
.admin-table td { padding: 14px 12px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.admin-table tr:last-child td { border-bottom: none; }
.admin-table tr:hover td { background: #f8fafc; }
.row-resuelto td { opacity: 0.65; }
.td-id { font-family: 'Sora', sans-serif; font-weight: 700; color: #cbd5e1; }
.td-dir { font-size: 12px; color: #64748b; max-width: 160px; }
.td-barrio { font-weight: 600; white-space: nowrap; }
.td-desc { max-width: 180px; color: #64748b; font-size: 12px; }
.td-confirm { font-weight: 600; color: #0369a1; text-align: center; }
.td-fecha { white-space: nowrap; color: #64748b; font-size: 12px; }
.tag { display: inline-block; font-size: 11px; font-weight: 500; padding: 3px 8px; border-radius: 20px; background: color-mix(in srgb, var(--tc) 12%, white); color: var(--tc); border: 1px solid color-mix(in srgb, var(--tc) 25%, white); white-space: nowrap; }
.urgencia-badge { font-size: 11px; font-weight: 600; padding: 3px 8px; border-radius: 20px; text-transform: capitalize; }
.urgencia-badge.alta  { background: #fef2f2; color: #dc2626; }
.urgencia-badge.media { background: #fff7ed; color: #ea580c; }
.urgencia-badge.baja  { background: #f0fdf4; color: #16a34a; }
.estado-badge { font-size: 11px; font-weight: 600; padding: 3px 8px; border-radius: 20px; white-space: nowrap; }
.estado-badge.pendiente  { background: #fef2f2; color: #dc2626; }
.estado-badge.en_proceso { background: #fff7ed; color: #ea580c; }
.estado-badge.resuelto   { background: #f0fdf4; color: #16a34a; }
.foto-link { color: #0369a1; font-size: 12px; text-decoration: none; font-weight: 500; }
.foto-link:hover { text-decoration: underline; }
.no-foto { color: #cbd5e1; }
.action-btns { display: flex; gap: 4px; }
.btn-accion { width: 30px; height: 30px; border-radius: 8px; border: 1.5px solid #e2e8f0; background: white; cursor: pointer; font-size: 14px; display: flex; align-items: center; justify-content: center; transition: all .2s; }
.btn-accion:hover { transform: scale(1.15); border-color: #0369a1; }
.btn-eliminar:hover { border-color: #ef4444 !important; }



.footer { background: #0f172a; color: #64748b; padding: 24px 0; font-size: 13px; margin-top: auto; }
.footer-inner { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px; }
</style>
