<template>
  <nav class="nav">
    <div class="nav-inner">
      <router-link to="/" class="nav-logo-link">
        <AguaYaLogoNav />
      </router-link>
      <div class="nav-links">
        <router-link to="/mapa">Mapa</router-link>
        <router-link to="/estadisticas">Estadísticas</router-link>
        <router-link to="/reportar" class="nav-cta">Reportar</router-link>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.nav {
  position: fixed;
  top: 0; left: 0; right: 0;
  z-index: 1000;
  backdrop-filter: blur(14px);
  background: rgba(255, 255, 255, 0.88);
  border-bottom: 1px solid #e2e8f0;
}

.nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
}

.nav-logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  flex-shrink: 0;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-links a {
  text-decoration: none;
  color: #334155;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s;
}

/* Vue Router agrega esta clase automáticamente al link de la ruta activa */
.nav-links a.router-link-active {
  color: #0369a1;
  font-weight: 600;
}

.nav-cta {
  background: #0369a1;
  color: white !important;
  padding: 8px 18px;
  border-radius: 20px;
  font-weight: 600 !important;
}

/* Cuando /reportar está activa, oscurecer el botón en vez de cambiar color */
.nav-cta.router-link-active {
  background: #075985;
  color: white !important;
}

@media (max-width: 600px) {
  .nav-links {
    gap: 14px;
  }

  .nav-links a {
    font-size: 13px;
  }

  .nav-cta {
    padding: 7px 14px;
  }
}
</style>

<script setup>
import AguaYaLogoNav from '@/components/AguaYaLogoNav.vue'
</script>