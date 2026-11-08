<template>
  <div class="map-wrapper">
    <div id="map"></div>
    <div class="map-legend">
      <span v-for="f in leyenda" :key="f.label" class="legend-item">
        <i :style="{ background: f.color }"></i> {{ f.label }}
      </span>
    </div>
  </div>
</template>

<script>
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

export default {
  name: 'MapView',

  props: {
    reportes: {
      type: Array,
      required: true,
    },
  },

  data() {
    return {
      map:       null,
      marcadores: [],
      leyenda: [
        { label: 'Sin agua',     color: '#ef4444' },
        { label: 'Baja presión', color: '#f97316' },
        { label: 'Agua sucia',   color: '#a16207' },
        { label: 'Fugas',        color: '#0ea5e9' },
      ],
    }
  },

  watch: {
    // Redibuja marcadores cuando cambia la lista de reportes
    reportes() {
      this.actualizarMarcadores()
    },
  },

  mounted() {
  this.$nextTick(() => {
    this.map = L.map('map', {
      zoomAnimation: false,
      maxZoom: 18
    }).setView([11.2408, -74.199], 13)
    
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
    }).addTo(this.map)
    
    this.actualizarMarcadores()
  })
},

beforeUnmount() {
  if (this.map) {
    this.map.off()
    this.map.remove()
    this.map = null
  }
},

  methods: {
    actualizarMarcadores() {
      this.marcadores.forEach(m => m.remove())
      this.marcadores = []

      this.reportes.forEach(r => {
        const icon = L.divIcon({
          className: '',
          html: `<div style="
            background:${r.color};
            width:14px; height:14px;
            border-radius:50%;
            border:3px solid white;
            box-shadow:0 2px 8px rgba(0,0,0,0.3);
          "></div>`,
          iconSize:   [14, 14],
          iconAnchor: [7, 7],
        })

        const m = L.marker([r.lat, r.lng], { icon })
          .addTo(this.map)
          .bindPopup(`
            <strong>${r.barrio}</strong><br>
            ${r.icon} ${r.tipo}<br>
            <small>${r.desc}</small><br>
            <small style="color:#94a3b8">${r.tiempo}</small>
          `)

        this.marcadores.push(m)
      })
    },
  },
}
</script>

<style scoped>
.map-wrapper { position: relative; }

#map {
  height: 580px;
  border-radius: 18px;
  border: 1.5px solid #e2e8f0;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(3,105,161,0.1);
  z-index: 1;
}

.map-legend {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 12px;
  padding: 0 4px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
}
.legend-item i {
  width: 10px; height: 10px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  #map { height: 380px; }
}
</style>