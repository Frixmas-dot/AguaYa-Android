<template>
  <div class="stats-page">

    <!-- HEADER -->
    <section class="page-header">
      <div class="container">
        <p class="section-label">Datos en tiempo real</p>
        <h1 class="page-title">Estadísticas del servicio</h1>
        <p class="page-desc">Resumen de reportes ciudadanos sobre el agua en Santa Marta.</p>
      </div>
    </section>

    <!-- LOADING -->
    <div v-if="cargando" class="loading-state">
      <span class="spinner"></span> Cargando estadísticas...
    </div>

    <template v-else>

      <!-- KPI CARDS -->
      <section class="section">
        <div class="container">
          <div class="kpi-grid">
            <div v-for="(kpi, i) in kpis" :key="i" class="kpi-card" :style="{ animationDelay: i * 0.1 + 's' }">
              <div class="kpi-icon">{{ kpi.icon }}</div>
              <div class="kpi-body">
                <strong class="kpi-value">{{ kpi.value }}</strong>
                <span class="kpi-label">{{ kpi.label }}</span>
              </div>
              <div class="kpi-trend" :class="kpi.up ? 'up' : 'down'">
                {{ kpi.up ? '↑' : '↓' }} {{ kpi.trend }}
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- CHARTS ROW -->
      <section class="section section-alt">
        <div class="container charts-row">

          <div class="chart-card">
            <div class="chart-header">
              <h2 class="chart-title">Problemas más reportados</h2>
              <span class="chart-period">Total acumulado</span>
            </div>
            <StatsChart :datos="stats.porTipo || []" />
          </div>

          <div class="chart-card donut-card">
            <div class="chart-header">
              <h2 class="chart-title">Estado de reportes</h2>
              <span class="chart-period">Acumulado</span>
            </div>
            <div class="donut-wrapper">
              <svg class="donut-svg" viewBox="0 0 120 120">
                <circle cx="60" cy="60" r="48" fill="none" stroke="#e2e8f0" stroke-width="14"/>
                <circle cx="60" cy="60" r="48" fill="none" stroke="#0369a1" stroke-width="14"
                  :stroke-dasharray="`${pctResueltos * 3.01} ${301 - pctResueltos * 3.01}`"
                  stroke-dashoffset="72" stroke-linecap="round" class="donut-seg"/>
                <text x="60" y="56" text-anchor="middle" class="donut-center-val">{{ stats.pctResueltos || 0 }}%</text>
                <text x="60" y="68" text-anchor="middle" class="donut-center-label">resueltos</text>
              </svg>
              <div class="donut-legend">
                <div v-for="leg in donutLegend" :key="leg.label" class="legend-row">
                  <span class="legend-dot" :style="{ background: leg.color }"></span>
                  <span class="legend-label">{{ leg.label }}</span>
                  <span class="legend-val">{{ leg.val }}%</span>
                </div>
              </div>
            </div>
          </div>

        </div>
      </section>

      <!-- TABLA -->
      <section class="section">
        <div class="container">
          <div class="chart-card full-width">
            <div class="chart-header">
              <h2 class="chart-title">Barrios con más reportes</h2>
              <span class="chart-period">Total acumulado</span>
            </div>
            <table class="barrio-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Barrio</th>
                  <th>Reportes</th>
                  <th>Problema principal</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, i) in stats.porBarrio || []" :key="i">
                  <td class="rank">{{ i + 1 }}</td>
                  <td class="barrio-name">{{ row.nombre }}</td>
                  <td>
                    <div class="mini-bar-wrap">
                      <div class="mini-bar"
                        :style="{ width: maxBarrio ? (row.reportes / maxBarrio * 100) + '%' : '0%' }">
                      </div>
                      <span>{{ row.reportes }}</span>
                    </div>
                  </td>
                  <td>
                    <span class="tag" :style="{ '--tc': row.color }">{{ row.problema }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

    </template>

    <!-- FOOTER -->
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
import StatsChart from '@/components/StatsChart.vue'

export default {
  name: 'EstadisticasView',
  components: { StatsChart },

  data() {
    return {
      cargando: true,
      stats: {
        totalReportes: 0,
        barriosUnicos: 0,
        pctResueltos:  0,
        porTipo:       [],
        porBarrio:     [],
      },
      donutLegend: [
        { color: '#0369a1', label: 'Resueltos',   val: 0 },
        { color: '#38bdf8', label: 'En proceso',  val: 0 },
        { color: '#bae6fd', label: 'Sin atender', val: 0 },
      ],
    }
  },

  computed: {
    pctResueltos() {
      return this.stats.pctResueltos || 0
    },
    maxBarrio() {
      if (!this.stats.porBarrio?.length) return 1
      return this.stats.porBarrio[0].reportes
    },
    kpis() {
      return [
        { icon: '📋', value: this.stats.totalReportes, label: 'Reportes totales',    trend: 'Total',  up: true },
        { icon: '🏘️', value: this.stats.barriosUnicos, label: 'Barrios afectados',   trend: 'Únicos', up: true },
        { icon: '✅', value: `${this.stats.pctResueltos}%`, label: 'Problemas resueltos', trend: 'Resueltos', up: true },
        { icon: '⏱️', value: '2.4d',                  label: 'Tiempo de atención',   trend: 'Promedio', up: true },
      ]
    },
  },

  async mounted() {
    const store = useWaterReportsStore()
    const data  = await store.cargarEstadisticas()

    if (data) {
      this.stats = data
      // Actualizar donut legend con datos reales
      this.donutLegend[0].val = data.pctResueltos
      this.donutLegend[1].val = Math.round((1 - data.pctResueltos / 100) * 60)
      this.donutLegend[2].val = 100 - data.pctResueltos - this.donutLegend[1].val
    }

    this.cargando = false
  },
}
</script>

<style scoped>
* { box-sizing: border-box; margin: 0; padding: 0; }
.stats-page { font-family: 'Inter', sans-serif; color: #0f172a; background: #fff; min-height: 100vh; }
.container { max-width: 1100px; margin: 0 auto; padding: 0 24px; }

.loading-state {
  display: flex; align-items: center; gap: 12px;
  justify-content: center; padding: 80px 0;
  font-size: 15px; color: #0369a1;
}
.spinner {
  width: 18px; height: 18px;
  border: 2px solid #bae6fd; border-top-color: #0369a1;
  border-radius: 50%; animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.page-header {
  padding: 120px 0 56px;
  background: linear-gradient(160deg, #e0f2fe 0%, #f8fafc 60%);
  border-bottom: 1px solid #e2e8f0;
}
.page-header .container { display: flex; flex-direction: column; align-items: flex-start; }
.section-label {
  display: block; font-size: 12px; font-weight: 600;
  letter-spacing: 2px; text-transform: uppercase; color: #0369a1; margin-bottom: 10px;
}
.page-title {
  font-family: 'Sora', sans-serif;
  font-size: clamp(32px, 5vw, 52px); font-weight: 800; line-height: 1.1; margin-bottom: 14px;
}
.page-desc { color: #64748b; font-size: 17px; }

.section { padding: 72px 0; }
.section-alt { background: #f8fafc; }

.kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; }
.kpi-card {
  background: white; border: 1.5px solid #e2e8f0; border-radius: 18px;
  padding: 24px 20px; display: flex; align-items: flex-start; gap: 16px;
  animation: fadeUp 0.5s ease both; transition: transform .2s, box-shadow .2s;
}
.kpi-card:hover { transform: translateY(-3px); box-shadow: 0 8px 28px rgba(3,105,161,0.1); border-color: #bae6fd; }
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}
.kpi-icon { font-size: 30px; flex-shrink: 0; }
.kpi-body { display: flex; flex-direction: column; flex: 1; }
.kpi-value { font-family: 'Sora', sans-serif; font-size: 30px; font-weight: 800; color: #0369a1; line-height: 1; }
.kpi-label { font-size: 13px; color: #64748b; margin-top: 4px; }
.kpi-trend {
  font-size: 11px; font-weight: 600; padding: 4px 8px; border-radius: 20px;
  white-space: nowrap; flex-shrink: 0;
}
.kpi-trend.up   { background: #dcfce7; color: #16a34a; }
.kpi-trend.down { background: #fee2e2; color: #dc2626; }

.charts-row { display: grid; grid-template-columns: 1.4fr 1fr; gap: 20px; align-items: start; }
.chart-card { background: white; border: 1.5px solid #e2e8f0; border-radius: 20px; padding: 28px; }
.full-width { grid-column: 1 / -1; }
.chart-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 28px; }
.chart-title { font-family: 'Sora', sans-serif; font-size: 16px; font-weight: 700; }
.chart-period { font-size: 12px; background: #f1f5f9; color: #64748b; padding: 4px 10px; border-radius: 20px; }

.donut-wrapper { display: flex; flex-direction: column; align-items: center; gap: 24px; }
.donut-svg { width: 160px; height: 160px; }
.donut-seg { animation: spinIn 1s ease forwards; transform-origin: 60px 60px; }
@keyframes spinIn { from { stroke-dasharray: 0 301; } }
.donut-center-val { font-family: 'Sora', sans-serif; font-size: 22px; font-weight: 800; fill: #0369a1; }
.donut-center-label { font-size: 9px; fill: #64748b; font-family: 'Inter', sans-serif; }
.donut-legend { width: 100%; display: flex; flex-direction: column; gap: 10px; }
.legend-row { display: flex; align-items: center; gap: 10px; font-size: 13px; }
.legend-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.legend-label { flex: 1; color: #334155; }
.legend-val { font-weight: 600; color: #0f172a; }

.barrio-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.barrio-table th {
  text-align: left; font-size: 11px; font-weight: 600;
  letter-spacing: 1px; text-transform: uppercase; color: #94a3b8;
  padding: 0 12px 14px; border-bottom: 1.5px solid #e2e8f0;
}
.barrio-table td { padding: 14px 12px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.barrio-table tr:last-child td { border-bottom: none; }
.barrio-table tr:hover td { background: #f8fafc; }
.rank { font-family: 'Sora', sans-serif; font-weight: 700; color: #cbd5e1; font-size: 16px; }
.barrio-name { font-weight: 600; color: #0f172a; }
.mini-bar-wrap { display: flex; align-items: center; gap: 10px; }
.mini-bar { height: 6px; background: linear-gradient(90deg, #0369a1, #38bdf8); border-radius: 6px; max-width: 100px; min-width: 4px; transition: width 0.8s ease; }
.mini-bar-wrap span { font-weight: 600; font-size: 13px; }
.tag {
  display: inline-block; font-size: 12px; font-weight: 500; padding: 4px 10px; border-radius: 20px;
  background: color-mix(in srgb, var(--tc) 12%, white);
  color: var(--tc); border: 1px solid color-mix(in srgb, var(--tc) 25%, white);
}

.footer { background: #0f172a; color: #64748b; padding: 24px 0; font-size: 13px; }
.footer-inner { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px; }

@media (max-width: 768px) {
  .charts-row { grid-template-columns: 1fr; }
  .kpi-grid { grid-template-columns: 1fr 1fr; }
}
</style>
