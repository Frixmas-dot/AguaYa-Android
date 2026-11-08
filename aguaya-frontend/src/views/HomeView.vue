<template>
  <div class="home">

    <!-- HERO -->
    <section class="hero">
      <div class="hero-bg">
        <div class="wave wave-1"></div>
        <div class="wave wave-2"></div>
        <div class="wave wave-3"></div>
      </div>
      <div class="container hero-content">
        <div class="hero-badge">🌊 Santa Marta, Colombia</div>
        <h1 class="hero-title">
          El agua es<br>
          <span class="hero-highlight">un derecho.</span><br>
          Exigámoslo.
        </h1>
        <p class="hero-sub">
          Reporta problemas del servicio de agua en tu barrio y ayuda a visibilizar las zonas más afectadas de la ciudad.
        </p>
        <div class="hero-actions">
          <router-link to="/reportar" class="btn-main">
            <span>📍</span> Hacer un reporte
          </router-link>
          <router-link to="/mapa" class="btn-ghost">
            Ver mapa en vivo →
          </router-link>
        </div>

        <div class="hero-stats">
          <div class="stat">
            <strong>{{ counters.reportes }}</strong>
            <span>Reportes este mes</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat">
            <strong>{{ counters.barrios }}</strong>
            <span>Barrios afectados</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat">
            <strong>{{ counters.verificados }}%</strong>
            <span>Reportes verificados</span>
          </div>
        </div>
      </div>
    </section>

    <!-- PROBLEMAS -->
    <section class="section" id="problemas">
      <div class="container">
        <div class="section-header">
          <p class="section-label">Categorías</p>
          <h2 class="section-title">¿Qué problema tienes?</h2>
          <p class="section-desc">Selecciona el tipo de falla para reportarla rápidamente.</p>
        </div>
        <div class="problem-grid">
          <router-link
            v-for="p in problemas"
            :key="p.id"
            to="/reportar"
            class="problem-card"
            :style="{ '--accent': p.color }"
          >
            <div class="problem-icon">{{ p.icon }}</div>
            <div class="problem-body">
              <h3>{{ p.title }}</h3>
              <p>{{ p.desc }}</p>
            </div>
            <div class="problem-arrow">→</div>
          </router-link>
        </div>
      </div>
    </section>

    <!-- COMO FUNCIONA -->
    <section class="section section-alt">
      <div class="container">
        <div class="section-header">
          <p class="section-label">Proceso</p>
          <h2 class="section-title">¿Cómo funciona AguaYa?</h2>
        </div>
        <div class="steps-row">
          <div v-for="(step, i) in pasos" :key="i" class="step">
            <div class="step-number">{{ String(i + 1).padStart(2, '0') }}</div>
            <div class="step-icon">{{ step.icon }}</div>
            <h3>{{ step.title }}</h3>
            <p>{{ step.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- MAPA TEASER -->
    <section class="map-teaser">
      <div class="container map-inner">
        <div class="map-text">
          <p class="section-label">Mapa en tiempo real</p>
          <h2>Visualiza las zonas afectadas</h2>
          <p>Consulta el mapa interactivo para ver dónde se concentran los reportes y qué barrios necesitan atención urgente.</p>
          <router-link to="/mapa" class="btn-main">Ver mapa completo →</router-link>
        </div>
        <div class="map-preview">
          <div class="map-mock">
            <div class="map-pin pin-red" style="top:30%;left:45%">
              <span>🔴</span><div class="pin-label">Sin agua</div>
            </div>
            <div class="map-pin pin-orange" style="top:55%;left:60%">
              <span>🟠</span><div class="pin-label">Baja presión</div>
            </div>
            <div class="map-pin pin-yellow" style="top:40%;left:25%">
              <span>🟡</span><div class="pin-label">Agua sucia</div>
            </div>
            <div class="map-pulse" style="top:30%;left:45%"></div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA FINAL -->
    <section class="cta-section">
      <div class="container cta-inner">
        <h2>Tu reporte hace la diferencia</h2>
        <p>Cada denuncia ciudadana ayuda a presionar a las autoridades para una solución más rápida.</p>
        <router-link to="/reportar" class="btn-main btn-large">
          💧 Reportar ahora
        </router-link>
      </div>
    </section>

    <!-- FOOTER -->
    <footer class="footer">
      <div class="container footer-inner">
        <span class="footer-logo">💧 AguaYa</span>
        <p>Proyecto académico · Santa Marta, Colombia</p>
      </div>
    </footer>

  </div>
</template>

<script>
export default {
  name: 'HomeView',
  data() {
    return {
      counters: {
        reportes: 0,
        barrios: 0,
        verificados: 0,
      },
      problemas: [
        { id: 1, icon: '🚱', title: 'Sin agua',       desc: 'Corte total del servicio en el sector.',   color: '#ef4444' },
        { id: 2, icon: '📉', title: 'Baja presión',   desc: 'El agua llega con muy poca fuerza.',       color: '#f97316' },
        { id: 3, icon: '🟤', title: 'Agua sucia',     desc: 'Agua con color, olor o sedimentos.',       color: '#a16207' },
        { id: 4, icon: '🔧', title: 'Fuga o ruptura', desc: 'Tubería rota o fuga visible en la calle.', color: '#0ea5e9' },
      ],
      pasos: [
        { icon: '📝', title: 'Reporta',       desc: 'Describe el problema e indica tu dirección o barrio.' },
        { icon: '🗺️', title: 'Se publica',    desc: 'El reporte aparece en el mapa público en segundos.' },
        { icon: '👥', title: 'Se visibiliza', desc: 'Otros ciudadanos confirman el problema en su zona.' },
        { icon: '📢', title: 'Se escala',     desc: 'Las zonas críticas reciben mayor atención y presión.' },
      ],
    }
  },
  mounted() {
    setTimeout(() => {
      this.animateCounter('reportes',   312, 1800)
      this.animateCounter('barrios',     18, 1400)
      this.animateCounter('verificados', 94, 1600)
    }, 300)
  },
  methods: {
    animateCounter(key, target, duration) {
      const start = performance.now()
      const update = (now) => {
        const progress = Math.min((now - start) / duration, 1)
        const eased = 1 - Math.pow(1 - progress, 3)
        this.counters[key] = Math.round(eased * target)
        if (progress < 1) requestAnimationFrame(update)
      }
      requestAnimationFrame(update)
    },
  },
}
</script>

<style scoped>
/* ── BASE ── */
.home {
  font-family: 'Inter', sans-serif;
  color: #0f172a;
  background: #fff;
}
.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
}

/* ── HERO ── */
.hero {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(160deg, #e0f2fe 0%, #f0f9ff 50%, #e0f2fe 100%);
  padding-top: 60px;
}
.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.wave {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
  animation: float 8s ease-in-out infinite;
}
.wave-1 {
  width: 700px; height: 700px;
  background: radial-gradient(circle, #0ea5e9, transparent 70%);
  bottom: -200px; right: -100px;
  animation-duration: 10s;
}
.wave-2 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, #38bdf8, transparent 70%);
  top: 10%; left: -100px;
  animation-duration: 7s; animation-delay: -3s;
}
.wave-3 {
  width: 300px; height: 300px;
  background: radial-gradient(circle, #0369a1, transparent 70%);
  top: 40%; right: 20%;
  animation-duration: 12s; animation-delay: -5s;
}
@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50%       { transform: translateY(-30px) scale(1.05); }
}

.hero-content {
  position: relative;
  z-index: 1;
  padding: 80px 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 680px;
  animation: heroIn 0.8s ease both;
}
@keyframes heroIn {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}

.hero-badge {
  display: inline-block;
  background: rgba(3, 105, 161, 0.1);
  color: #0369a1;
  border: 1px solid rgba(3, 105, 161, 0.2);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 24px;
  animation: heroIn 0.8s ease 0.1s both;
}

.hero-title {
  font-family: 'Sora', sans-serif;
  font-size: clamp(42px, 6vw, 72px);
  font-weight: 800;
  line-height: 1.1;
  color: #0f172a;
  margin: 0 0 20px;
  animation: heroIn 0.8s ease 0.2s both;
}

.hero-highlight {
  color: #0369a1;
  position: relative;
  display: inline-block;
}
.hero-highlight::after {
  content: '';
  position: absolute;
  bottom: 4px; left: 0;
  height: 6px;
  background: #bae6fd;
  z-index: -1;
  border-radius: 4px;
  width: 0;
  animation: underlineGrow 0.7s ease 0.9s forwards;
}
@keyframes underlineGrow { to { width: 100%; } }

.hero-sub {
  font-size: 18px;
  color: #475569;
  max-width: 520px;
  line-height: 1.7;
  margin: 0 0 36px;
  animation: heroIn 0.8s ease 0.3s both;
}

.hero-actions {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 56px;
  animation: heroIn 0.8s ease 0.4s both;
}

.btn-main {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #0369a1;
  color: white;
  padding: 14px 28px;
  border-radius: 30px;
  text-decoration: none;
  font-weight: 600;
  font-size: 15px;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.35);
}
.btn-main:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(3, 105, 161, 0.45);
}
.btn-ghost {
  color: #0369a1;
  text-decoration: none;
  font-weight: 600;
  font-size: 15px;
  transition: opacity 0.2s;
}
.btn-ghost:hover { opacity: 0.7; }

/* ── STATS ── */
.hero-stats {
  display: flex;
  align-items: center;
  gap: 32px;
  flex-wrap: wrap;
  padding-top: 28px;
  border-top: 1px solid #cbd5e1;
  width: 100%;
  animation: heroIn 0.8s ease 0.55s both;
}
.stat strong {
  display: block;
  font-family: 'Sora', sans-serif;
  font-size: 32px;
  font-weight: 800;
  color: #0369a1;
  background: linear-gradient(90deg, #0369a1 0%, #0ea5e9 50%, #0369a1 100%);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: shimmer 2.5s linear 0.3s 2;
}
@keyframes shimmer {
  from { background-position: 200% center; }
  to   { background-position:   0% center; }
}
.stat span {
  font-size: 13px;
  color: #64748b;
  margin-top: 2px;
  display: block;
}
.stat-divider {
  width: 1px;
  height: 40px;
  background: #cbd5e1;
}

/* ── SECTIONS ── */
.section { padding: 96px 0; }
.section-alt { background: #f8fafc; }
.section-header { text-align: center; margin-bottom: 56px; }
.section-label {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: #0369a1;
  margin-bottom: 8px;
}
.section-title {
  font-family: 'Sora', sans-serif;
  font-size: clamp(28px, 4vw, 40px);
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 12px;
}
.section-desc { color: #64748b; font-size: 16px; }

/* ── PROBLEM CARDS ── */
.problem-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
  gap: 16px;
}
.problem-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border: 1.5px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  text-decoration: none;
  color: inherit;
  transition: border-color 0.2s, transform 0.2s, box-shadow 0.2s;
  position: relative;
  overflow: hidden;
}
.problem-card::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 4px;
  background: var(--accent);
  border-radius: 4px 0 0 4px;
}
.problem-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--accent);
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
}
.problem-card:hover {
  border-color: var(--accent);
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}
.problem-card:hover::after { opacity: 0.04; }
.problem-icon { font-size: 32px; flex-shrink: 0; }
.problem-body h3 {
  font-family: 'Sora', sans-serif;
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 4px;
}
.problem-body p { font-size: 13px; color: #64748b; margin: 0; }
.problem-arrow {
  margin-left: auto;
  color: #94a3b8;
  font-size: 18px;
  flex-shrink: 0;
  transition: transform 0.2s, color 0.2s;
}
.problem-card:hover .problem-arrow {
  transform: translateX(5px);
  color: var(--accent);
}

/* ── STEPS ── */
.steps-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 32px;
}
.step {
  background: white;
  border: 1.5px solid #e2e8f0;
  border-radius: 20px;
  padding: 32px 24px;
  text-align: center;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}
.step:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 28px rgba(3, 105, 161, 0.1);
  border-color: #bae6fd;
}
.step-number {
  font-family: 'Sora', sans-serif;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #0369a1;
  background: #e0f2fe;
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
  margin-bottom: 14px;
}
.step-icon { font-size: 36px; margin-bottom: 12px; }
.step h3 {
  font-family: 'Sora', sans-serif;
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 8px;
}
.step p { font-size: 13px; color: #64748b; line-height: 1.6; margin: 0; }

/* ── MAP TEASER ── */
.map-teaser { background: #0f172a; padding: 96px 0; color: white; }
.map-inner {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 64px;
  align-items: center;
}
.map-text .section-label { color: #38bdf8; }
.map-text h2 {
  font-family: 'Sora', sans-serif;
  font-size: clamp(28px, 4vw, 38px);
  font-weight: 800;
  margin: 8px 0 16px;
}
.map-text p { color: #94a3b8; font-size: 16px; line-height: 1.7; margin: 0 0 28px; }
.map-preview { display: flex; justify-content: center; }
.map-mock {
  width: 100%;
  max-width: 420px;
  aspect-ratio: 4/3;
  background: #1e293b;
  border-radius: 20px;
  border: 1px solid #334155;
  position: relative;
  overflow: hidden;
  background-image:
    linear-gradient(rgba(51,65,85,0.5) 1px, transparent 1px),
    linear-gradient(90deg, rgba(51,65,85,0.5) 1px, transparent 1px);
  background-size: 40px 40px;
}
.map-pin { position: absolute; transform: translate(-50%, -50%); cursor: default; }
.map-pin span { font-size: 22px; display: block; }
.pin-label {
  position: absolute;
  top: -28px; left: 50%;
  transform: translateX(-50%);
  background: #1e293b;
  border: 1px solid #475569;
  color: #e2e8f0;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 8px;
  white-space: nowrap;
}
.map-pulse {
  position: absolute;
  width: 40px; height: 40px;
  border-radius: 50%;
  background: rgba(239,68,68,0.3);
  transform: translate(-50%, -50%);
  animation: pulse 2s ease-out infinite;
}
@keyframes pulse {
  0%   { transform: translate(-50%,-50%) scale(0.5); opacity: 1; }
  100% { transform: translate(-50%,-50%) scale(2.5); opacity: 0; }
}

/* ── CTA ── */
.cta-section {
  padding: 96px 0;
  background: linear-gradient(135deg, #0369a1, #0ea5e9);
  text-align: center;
  color: white;
}
.cta-inner h2 {
  font-family: 'Sora', sans-serif;
  font-size: clamp(28px, 4vw, 44px);
  font-weight: 800;
  margin: 0 0 14px;
}
.cta-inner p { font-size: 18px; opacity: 0.85; margin: 0 0 36px; }
.btn-large {
  font-size: 17px;
  padding: 16px 36px;
  background: white;
  color: #0369a1;
  box-shadow: 0 8px 32px rgba(0,0,0,0.25);
}
.btn-large:hover { box-shadow: 0 12px 40px rgba(0,0,0,0.3); }

/* ── FOOTER ── */
.footer { background: #0f172a; color: #64748b; padding: 24px 0; font-size: 13px; }
.footer-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}
.footer-logo {
  font-family: 'Sora', sans-serif;
  font-weight: 800;
  font-size: 16px;
  color: #94a3b8;
}

/* ── RESPONSIVE ── */
@media (max-width: 768px) {
  .map-inner { grid-template-columns: 1fr; }
  .map-preview { display: none; }
  .hero-stats { gap: 20px; }
  .stat-divider { display: none; }
}
</style>