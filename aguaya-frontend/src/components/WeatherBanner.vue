<template>
  <div class="weather-banner">

    <!-- SKELETON mientras carga -->
    <div v-if="cargando" class="skeleton-row">
      <div class="skeleton skeleton-icon"></div>
      <div class="skeleton skeleton-text"></div>
      <div class="skeleton skeleton-text short"></div>
      <div class="skeleton skeleton-text short"></div>
      <div class="skeleton skeleton-text short"></div>
    </div>

    <!-- ERROR -->
    <div v-else-if="error" class="weather-error">
      ⚠️ No se pudo cargar el clima actual
    </div>

    <!-- DATOS REALES -->
    <div v-else class="weather-data">
      <div class="weather-main">
        <span class="weather-icon">{{ iconoClima }}</span>
        <div class="weather-temp">
          <strong>{{ clima.temperatura }}°C</strong>
          <span>{{ clima.descripcion }}</span>
        </div>
      </div>
      <div class="weather-stats">
        <div class="wstat">
          <span class="wstat-label">💧 Humedad</span>
          <span class="wstat-val">{{ clima.humedad }}%</span>
        </div>
        <div class="wstat">
          <span class="wstat-label">💨 Viento</span>
          <span class="wstat-val">{{ clima.viento }} km/h</span>
        </div>
        <div class="wstat">
          <span class="wstat-label">🌧 Lluvia</span>
          <span class="wstat-val">{{ clima.lluvia }} mm</span>
        </div>
      </div>
      <span class="weather-badge">Santa Marta en vivo</span>
    </div>

  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'WeatherBanner',

  data() {
    return {
      cargando: true,
      error:    false,
      clima: {
        temperatura:  null,
        humedad:      null,
        viento:       null,
        lluvia:       null,
        descripcion:  '',
        codigoClima:  null,
      },
    }
  },

  computed: {
    iconoClima() {
      const c = this.clima.codigoClima
      if (c === 0)               return '☀️'
      if (c <= 3)                return '🌤'
      if (c <= 48)               return '🌫'
      if (c <= 67)               return '🌧'
      if (c <= 77)               return '🌨'
      if (c <= 82)               return '🌦'
      if (c <= 99)               return '⛈'
      return '🌡'
    },

    descripcionClima() {
      const c = this.clima.codigoClima
      if (c === 0)  return 'Despejado'
      if (c <= 3)   return 'Parcialmente nublado'
      if (c <= 48)  return 'Niebla'
      if (c <= 67)  return 'Lluvia'
      if (c <= 77)  return 'Nieve'
      if (c <= 82)  return 'Chubascos'
      if (c <= 99)  return 'Tormenta'
      return 'Variable'
    },
  },

  mounted() {
    this.obtenerClima()
  },

  methods: {
    async obtenerClima() {
      this.cargando = true
      this.error    = false

      try {
        // Open-Meteo 
        const res = await axios.get('https://api.open-meteo.com/v1/forecast', {
          params: {
            latitude:              11.2408,
            longitude:            -74.199,
            current:              'temperature_2m,relative_humidity_2m,wind_speed_10m,precipitation,weather_code',
            timezone:             'America/Bogota',
            forecast_days:         1,
          },
        })

        const c = res.data.current
        this.clima = {
          temperatura: Math.round(c.temperature_2m),
          humedad:     c.relative_humidity_2m,
          viento:      Math.round(c.wind_speed_10m),
          lluvia:      c.precipitation,
          codigoClima: c.weather_code,
          descripcion: this.descripcionClima,
        }

      } catch (e) {
        console.warn('Open-Meteo no respondió:', e)
        this.error = true
      } finally {
        this.cargando = false
      }
    },
  },
}
</script>

<style scoped>
.weather-banner {
  background: white;
  border: 1.5px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

/* ── SKELETON ── */
.skeleton-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.skeleton {
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% auto;
  animation: shimmer 1.4s linear infinite;
  border-radius: 8px;
}
@keyframes shimmer {
  from { background-position: 200% center; }
  to   { background-position:   0% center; }
}
.skeleton-icon  { width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0; }
.skeleton-text  { height: 14px; flex: 1; }
.skeleton-text.short { flex: 0 0 80px; }

/* ── ERROR ── */
.weather-error {
  font-size: 13px;
  color: #94a3b8;
  text-align: center;
  padding: 8px 0;
}

/* ── DATOS ── */
.weather-data {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}
.weather-main {
  display: flex;
  align-items: center;
  gap: 10px;
}
.weather-icon { font-size: 32px; }
.weather-temp strong {
  font-family: 'Sora', sans-serif;
  font-size: 24px;
  font-weight: 800;
  color: #0369a1;
  display: block;
  line-height: 1;
}
.weather-temp span { font-size: 12px; color: #64748b; }

.weather-stats {
  display: flex;
  gap: 16px;
  flex: 1;
  flex-wrap: wrap;
}
.wstat { display: flex; flex-direction: column; gap: 2px; }
.wstat-label { font-size: 11px; color: #94a3b8; }
.wstat-val { font-size: 14px; font-weight: 600; color: #0f172a; }

.weather-badge {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: #0369a1;
  background: #e0f2fe;
  padding: 4px 10px;
  border-radius: 20px;
  white-space: nowrap;
}
</style>