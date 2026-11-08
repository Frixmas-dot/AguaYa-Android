<template>
  <div class="report-card" :style="{ '--rc': reporte.color }">
    <div class="report-dot"></div>
    <div class="report-body">
      <div class="report-top">
        <strong>{{ reporte.barrio }}</strong>
        <span class="report-time">{{ reporte.tiempo }}</span>
      </div>
      <p class="report-desc">{{ reporte.desc }}</p>

      <!-- Foto miniatura -->
      <div v-if="reporte.foto_url" class="report-foto" @click="verFoto = true">
        <img :src="reporte.foto_url" alt="foto del reporte" />
        <span class="foto-overlay">🔍 Ver foto</span>
      </div>

      <div class="report-bottom">
        <span class="report-tag">{{ reporte.icon }} {{ reporte.tipo }}</span>
        <span class="report-estado" :class="reporte.estado">{{ estadoLabel(reporte.estado) }}</span>
        <button class="btn-confirmar" @click="confirmar" :disabled="confirmado">
          {{ confirmado ? '✅ Confirmado' : `👍 ${reporte.confirmaciones || 0}` }}
        </button>
      </div>
    </div>

    <!-- Modal foto -->
    <div v-if="verFoto" class="foto-modal" @click.self="verFoto = false">
      <div class="foto-modal-inner">
        <img :src="reporte.foto_url" alt="foto"/>
        <button class="foto-close" @click="verFoto = false">✕</button>
      </div>
    </div>
  </div>
</template>

<script>
import { useWaterReportsStore } from '@/stores/waterReports'

export default {
  name: 'ReportCard',
  props: {
    reporte: { type: Object, required: true },
  },
  data() {
    return {
      confirmado: false,
      verFoto:    false,
    }
  },
  methods: {
    estadoLabel(estado) {
      const map = { pendiente: '🔴 Pendiente', en_proceso: '🟠 En proceso', resuelto: '🟢 Resuelto' }
      return map[estado] || estado
    },
    async confirmar() {
      if (this.confirmado) return
      const store = useWaterReportsStore()
      await store.confirmarReporte(this.reporte.id)
      this.confirmado = true
    },
  },
}
</script>

<style scoped>
.report-card {
  display: flex; gap: 12px; align-items: flex-start;
  padding: 14px; border-radius: 12px; border: 1.5px solid #f1f5f9;
  transition: border-color .2s, transform .2s; background: white; position: relative;
}
.report-card:hover { border-color: var(--rc); transform: translateX(3px); }
.report-dot { width: 10px; height: 10px; border-radius: 50%; background: var(--rc); flex-shrink: 0; margin-top: 5px; }
.report-body { flex: 1; min-width: 0; }
.report-top { display: flex; justify-content: space-between; align-items: baseline; gap: 8px; margin-bottom: 4px; }
.report-top strong { font-size: 14px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.report-time { font-size: 11px; color: #94a3b8; flex-shrink: 0; }
.report-desc { font-size: 12px; color: #64748b; line-height: 1.5; margin-bottom: 8px; }

/* ── Foto ── */
.report-foto {
  position: relative; border-radius: 8px; overflow: hidden;
  margin-bottom: 8px; cursor: pointer; max-height: 120px;
}
.report-foto img { width: 100%; height: 120px; object-fit: cover; display: block; transition: transform .2s; }
.report-foto:hover img { transform: scale(1.03); }
.foto-overlay {
  position: absolute; inset: 0; background: rgba(0,0,0,0.3);
  color: white; font-size: 12px; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity .2s;
}
.report-foto:hover .foto-overlay { opacity: 1; }

.report-bottom { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.report-tag { display: inline-block; font-size: 11px; font-weight: 500; padding: 3px 8px; border-radius: 20px; background: color-mix(in srgb, var(--rc) 12%, white); color: var(--rc); border: 1px solid color-mix(in srgb, var(--rc) 25%, white); }

.report-estado { font-size: 11px; font-weight: 500; padding: 3px 8px; border-radius: 20px; }
.report-estado.pendiente  { background: #fef2f2; color: #dc2626; }
.report-estado.en_proceso { background: #fff7ed; color: #ea580c; }
.report-estado.resuelto   { background: #f0fdf4; color: #16a34a; }

.btn-confirmar {
  margin-left: auto; font-size: 11px; font-weight: 600;
  padding: 3px 10px; border-radius: 20px; cursor: pointer;
  border: 1.5px solid #e2e8f0; background: white; color: #334155;
  transition: all .2s; font-family: 'Inter', sans-serif;
}
.btn-confirmar:hover:not(:disabled) { border-color: #0369a1; color: #0369a1; background: #f0f9ff; }
.btn-confirmar:disabled { opacity: 0.7; cursor: default; }

/* ── Modal foto ── */
.foto-modal {
  position: fixed; inset: 0; background: rgba(0,0,0,0.85);
  z-index: 9999; display: flex; align-items: center; justify-content: center;
  padding: 24px;
}
.foto-modal-inner { position: relative; max-width: 90vw; max-height: 90vh; }
.foto-modal-inner img { max-width: 100%; max-height: 85vh; border-radius: 12px; display: block; }
.foto-close {
  position: absolute; top: -14px; right: -14px;
  width: 32px; height: 32px; border-radius: 50%;
  background: white; border: none; font-size: 14px;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
}
</style>
