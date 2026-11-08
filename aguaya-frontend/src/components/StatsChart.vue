<template>
  <div class="stats-chart">
    <div v-for="(item, i) in datos" :key="i" class="bar-row">
      <div class="bar-meta">
        <span class="bar-icon">{{ item.icon }}</span>
        <span class="bar-name">{{ item.label }}</span>
        <span class="bar-pct">{{ item.count }} ({{ item.pct }}%)</span>
      </div>
      <div class="bar-track">
        <div
          class="bar-fill"
          :style="{
            width: item.pct + '%',
            background: item.color,
            animationDelay: i * 0.15 + 's'
          }"
        ></div>
      </div>
    </div>

    <!-- Estado vacío -->
    <div v-if="datos.length === 0" class="empty-state">
      <span>📭</span>
      <p>No hay reportes aún</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StatsChart',
  props: {
    datos: {
      type: Array,
      required: true,
    },
  },
}
</script>

<style scoped>
.stats-chart { display: flex; flex-direction: column; gap: 18px; }

.bar-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 7px;
}
.bar-icon { font-size: 15px; }
.bar-name { font-size: 14px; color: #334155; flex: 1; }
.bar-pct  { font-size: 13px; font-weight: 600; color: #0369a1; }

.bar-track {
  height: 10px;
  background: #f1f5f9;
  border-radius: 10px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  border-radius: 10px;
  width: 0;
  animation: growBar 0.8s ease forwards;
}
@keyframes growBar { from { width: 0 !important; } }

.empty-state {
  text-align: center;
  padding: 32px 0;
  color: #94a3b8;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.empty-state span { font-size: 32px; }
.empty-state p { font-size: 14px; }
</style>