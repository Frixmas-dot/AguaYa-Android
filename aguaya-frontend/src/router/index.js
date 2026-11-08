import { createRouter, createWebHistory } from "vue-router"

import HomeView         from "../views/HomeView.vue"
import ReportarView     from "../views/ReportarView.vue"
import MapaView         from "../views/MapaView.vue"
import EstadisticasView from "../views/EstadisticasView.vue"
import AdminView        from "../views/AdminView.vue"

const routes = [
  { path: "/",            component: HomeView },
  { path: "/reportar",   component: ReportarView },
  { path: "/mapa",       component: MapaView },
  { path: "/estadisticas", component: EstadisticasView },
  { path: "/admin",      component: AdminView },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
