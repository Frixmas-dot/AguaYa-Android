// src/stores/waterReports.js
import { defineStore } from 'pinia'

// Con Nginx en producción, las llamadas a '/api' van automáticamente al backend
const API = process.env.VUE_APP_API_URL || '/api'

export const useWaterReportsStore = defineStore('waterReports', {
  state: () => ({
    reportes:  [],
    cargando:  false,
    error:     null,

    metaTipo: {
      'Sin agua':     { icon: '🚱', color: '#ef4444' },
      'Baja presión': { icon: '📉', color: '#f97316' },
      'Agua sucia':   { icon: '🟤', color: '#a16207' },
      'Fuga':         { icon: '🔧', color: '#0ea5e9' },
    },

    idToTipo: {
      'sin-agua':     'Sin agua',
      'baja-presion': 'Baja presión',
      'agua-sucia':   'Agua sucia',
      'fuga':         'Fuga',
    },
  }),

  getters: {
    reportesConMeta(state) {
      return state.reportes
    },
    total(state) {
      return state.reportes.length
    },
  },

  actions: {
    async cargarReportes(filtros = {}) {
      this.cargando = true
      this.error    = null
      try {
        const params = new URLSearchParams()
        if (filtros.tipo)     params.append('tipo',     filtros.tipo)
        if (filtros.estado)   params.append('estado',   filtros.estado)
        if (filtros.urgencia) params.append('urgencia', filtros.urgencia)
        if (filtros.fecha)    params.append('fecha',    filtros.fecha)

        const qs  = params.toString()
        const res = await fetch(`${API}/reportes${qs ? '?' + qs : ''}`)
        if (!res.ok) throw new Error('Error al cargar reportes')
        this.reportes = await res.json()
      } catch (err) {
        this.error = err.message
      } finally {
        this.cargando = false
      }
    },

    // Ahora acepta foto (File object opcional)
    async agregarReporte({ barrio, tipoId, desc, urgencia, foto }) {
      this.cargando = true
      this.error    = null
      try {
        const formData = new FormData()
        formData.append('barrio',   barrio)
        formData.append('tipoId',   tipoId)
        formData.append('desc',     desc)
        formData.append('urgencia', urgencia)
        if (foto) formData.append('foto', foto)

        const res = await fetch(`${API}/reportes`, {
          method: 'POST',
          body:   formData,  // No headers — el browser los pone solo con FormData
        })

        if (!res.ok) {
          const data = await res.json()
          throw new Error(data.error || 'Error al guardar')
        }

        const nuevo = await res.json()
        this.reportes.unshift(nuevo)
        return nuevo
      } catch (err) {
        this.error = err.message
        throw err
      } finally {
        this.cargando = false
      }
    },

    async confirmarReporte(id) {
      try {
        const res  = await fetch(`${API}/reportes/${id}/confirmar`, { method: 'POST' })
        const data = await res.json()
        const r    = this.reportes.find(r => r.id === id)
        if (r) r.confirmaciones = data.confirmaciones
        return data.confirmaciones
      } catch (err) {
        console.error('confirmarReporte:', err)
      }
    },

    async cambiarEstado(id, estado) {
      try {
        const res = await fetch(`${API}/reportes/${id}/estado`, {
          method:  'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body:    JSON.stringify({ estado }),
        })
        if (!res.ok) throw new Error('Error al actualizar estado')
        const r = this.reportes.find(r => r.id === id)
        if (r) r.estado = estado
      } catch (err) {
        console.error('cambiarEstado:', err)
      }
    },

    async eliminarReporte(id) {
      try {
        const res = await fetch(`${API}/reportes/${id}`, { method: 'DELETE' })
        if (!res.ok) throw new Error('Error al eliminar')
        this.reportes = this.reportes.filter(r => r.id !== id)
      } catch (err) {
        console.error('eliminarReporte:', err)
      }
    },

    async cargarEstadisticas() {
      try {
        const res = await fetch(`${API}/estadisticas`)
        if (!res.ok) throw new Error()
        return await res.json()
      } catch {
        return null
      }
    },

    tiempoRelativo(fecha) {
      const mins = Math.floor((Date.now() - new Date(fecha)) / 60000)
      if (mins < 60)   return `Hace ${mins}m`
      if (mins < 1440) return `Hace ${Math.floor(mins / 60)}h`
      return `Hace ${Math.floor(mins / 1440)}d`
    },
  },
})
