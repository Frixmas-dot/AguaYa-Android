<template>
  <div class="report-page">
    <div class="page-body">

      <!-- COLUMNA IZQUIERDA -->
      <div class="info-col">
        <p class="section-label">Ciudadanía activa</p>
        <h1 class="info-title">Reporta un problema de agua</h1>
        <p class="info-desc">
          Tu reporte aparece en el mapa público y ayuda a visibilizar las zonas más afectadas de Santa Marta.
        </p>
        <div class="info-steps">
          <div v-for="(s, i) in pasos" :key="i" class="info-step">
            <div class="step-num">{{ i + 1 }}</div>
            <div>
              <strong>{{ s.title }}</strong>
              <p>{{ s.desc }}</p>
            </div>
          </div>
        </div>
        <div class="info-badge">
          <span>🔒</span>
          <p>Tu información es anónima. Solo se usa para ubicar el problema en el mapa.</p>
        </div>
      </div>

      <!-- COLUMNA DERECHA: formulario -->
      <div class="form-col">
        <div class="form-card">

          <!-- Éxito -->
          <div v-if="enviado" class="success-state">
            <div class="success-icon">✅</div>
            <h3>¡Reporte enviado!</h3>
            <p>Tu reporte ya aparece en el mapa. Gracias por contribuir.</p>
            <router-link to="/mapa" class="btn-main">Ver en el mapa →</router-link>
            <button class="btn-ghost" @click="resetForm">Hacer otro reporte</button>
          </div>

          <!-- Formulario -->
          <div v-else>
            <h2 class="form-title">💧 Nuevo reporte</h2>
            <p class="form-sub">Completa los campos para enviar tu reporte.</p>

            <div class="form-body">

              <!-- Barrio -->
              <div class="field">
                <label>Barrio <span class="required">*</span></label>
                <input
                  v-model="form.barrio"
                  type="text"
                  placeholder="Ej: El Rodadero"
                  :class="{ error: errors.barrio }"
                  @input="errors.barrio = false"
                />
                <span v-if="errors.barrio" class="field-error">Este campo es obligatorio</span>
              </div>

              <!-- Dirección exacta -->
              <div class="field">
                <label>Dirección exacta <span class="optional">(solo visible para administración)</span></label>
                <input
                  v-model="form.direccion"
                  type="text"
                  placeholder="Ej: Calle 15 #8-42, casa azul con reja"
                />
                <small class="field-hint">🔒 Esta información no se muestra en el mapa público</small>
              </div>

              <!-- Tipo -->
              <div class="field">
                <label>Tipo de problema <span class="required">*</span></label>
                <div class="tipo-grid">
                  <button
                    v-for="t in tipos"
                    :key="t.id"
                    type="button"
                    class="tipo-btn"
                    :class="{ selected: form.tipo === t.id }"
                    :style="{ '--tc': t.color }"
                    @click="seleccionarTipo(t.id)"
                  >
                    <span class="tipo-icon">{{ t.icon }}</span>
                    <span class="tipo-label">{{ t.label }}</span>
                  </button>
                </div>
                <span v-if="errors.tipo" class="field-error">Selecciona un tipo</span>
              </div>

              <!-- Descripción -->
              <div class="field">
                <label>Descripción <span class="required">*</span></label>
                <textarea
                  v-model="form.descripcion"
                  rows="4"
                  placeholder="Describe el problema con detalle: desde cuándo ocurre, qué tan grave es..."
                  :class="{ error: errors.descripcion }"
                  @input="errors.descripcion = false"
                  maxlength="300"
                ></textarea>
                <div class="field-footer">
                  <span v-if="errors.descripcion" class="field-error">Mínimo 10 caracteres</span>
                  <span v-else class="char-count">{{ form.descripcion.length }} / 300</span>
                </div>
              </div>

              <!-- Urgencia -->
              <div class="field">
                <label>Nivel de urgencia</label>
                <div class="urgencia-row">
                  <button
                    v-for="u in urgencias"
                    :key="u.id"
                    type="button"
                    class="urgencia-btn"
                    :class="{ selected: form.urgencia === u.id }"
                    :style="{ '--uc': u.color }"
                    @click="form.urgencia = u.id"
                  >
                    {{ u.icon }} {{ u.label }}
                  </button>
                </div>
              </div>

              <!-- Foto -->
              <div class="field">
                <label>Foto del problema <span class="optional">(opcional)</span></label>
                <div
                  class="foto-drop"
                  :class="{ 'foto-drop--active': dragging, 'foto-drop--filled': fotoPreview }"
                  @dragover.prevent="dragging = true"
                  @dragleave="dragging = false"
                  @drop.prevent="onDrop"
                  @click="$refs.fotoInput.click()"
                >
                  <img v-if="fotoPreview" :src="fotoPreview" class="foto-preview" alt="preview"/>
                  <div v-else class="foto-placeholder">
                    <span>📷</span>
                    <p>Arrastra una foto aquí o <u>haz clic para seleccionar</u></p>
                    <small>JPG, PNG o WebP · máx 5MB</small>
                  </div>
                  <button v-if="fotoPreview" class="foto-remove" type="button" @click.stop="removeFoto">✕</button>
                </div>
                <input
                  ref="fotoInput"
                  type="file"
                  accept="image/jpeg,image/png,image/webp"
                  style="display:none"
                  @change="onFileChange"
                />
              </div>

              <!-- Geo status -->
              <div v-if="geocodificando" class="geo-status">
                <span class="geo-spinner"></span>
                Buscando ubicación de "{{ form.barrio }}"...
              </div>

              <button class="btn-submit" @click="enviar" :disabled="cargando">
                <span v-if="!cargando">📍 Enviar reporte</span>
                <span v-else class="loader"></span>
              </button>

            </div>
          </div>
        </div>
      </div>
    </div>

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

export default {
  name: 'ReportarView',
  data() {
    return {
      enviado:        false,
      cargando:       false,
      geocodificando: false,
      dragging:       false,
      fotoPreview:    null,
      fotoFile:       null,

      form: { barrio: '', direccion: '', tipo: '', descripcion: '', urgencia: 'media' },
      errors: { barrio: false, tipo: false, descripcion: false },

      tipos: [
        { id: 'sin-agua',     label: 'Sin agua',     icon: '🚱', color: '#ef4444' },
        { id: 'baja-presion', label: 'Baja presión', icon: '📉', color: '#f97316' },
        { id: 'agua-sucia',   label: 'Agua sucia',   icon: '🟤', color: '#a16207' },
        { id: 'fuga',         label: 'Fuga',         icon: '🔧', color: '#0ea5e9' },
      ],
      urgencias: [
        { id: 'baja',  label: 'Baja',  icon: '🟢', color: '#16a34a' },
        { id: 'media', label: 'Media', icon: '🟠', color: '#ea580c' },
        { id: 'alta',  label: 'Alta',  icon: '🔴', color: '#dc2626' },
      ],
      pasos: [
        { title: 'Completa el formulario', desc: 'Indica tu barrio, tipo de problema y descripción.' },
        { title: 'Se publica en el mapa',  desc: 'El reporte aparece de inmediato para todos.' },
        { title: 'La comunidad lo ve',     desc: 'Otros ciudadanos pueden confirmar el problema.' },
      ],
    }
  },

  methods: {
    seleccionarTipo(id) {
      this.form.tipo   = id
      this.errors.tipo = false
    },

    onFileChange(e) {
      const file = e.target.files[0]
      if (file) this.setFoto(file)
    },

    onDrop(e) {
      this.dragging = false
      const file = e.dataTransfer.files[0]
      if (file && file.type.startsWith('image/')) this.setFoto(file)
    },

    setFoto(file) {
      if (file.size > 5 * 1024 * 1024) {
        alert('La foto no puede superar 5MB')
        return
      }
      this.fotoFile    = file
      this.fotoPreview = URL.createObjectURL(file)
    },

    removeFoto() {
      this.fotoFile    = null
      this.fotoPreview = null
      this.$refs.fotoInput.value = ''
    },

    async enviar() {
      this.errors.barrio      = !this.form.barrio.trim()
      this.errors.tipo        = !this.form.tipo
      this.errors.descripcion = this.form.descripcion.trim().length < 10
      if (this.errors.barrio || this.errors.tipo || this.errors.descripcion) return

      this.cargando       = true
      this.geocodificando = true
      const store = useWaterReportsStore()

      try {
        await store.agregarReporte({
          barrio:   this.form.barrio,
          tipoId:   this.form.tipo,
          desc:     this.form.descripcion,
          urgencia: this.form.urgencia,
          foto:     this.fotoFile,
          direccion: this.form.direccion,
        })
        this.geocodificando = false
        this.cargando       = false
        this.enviado        = true
      } catch (e) {
        this.geocodificando = false
        this.cargando       = false
        alert('Error al enviar el reporte. Intenta de nuevo.')
      }
    },

    resetForm() {
      this.enviado     = false
      this.fotoFile    = null
      this.fotoPreview = null
      this.form        = { barrio: '', tipo: '', descripcion: '', urgencia: 'media' }
    },
  },
}
</script>

<style scoped>
* { box-sizing: border-box; margin: 0; padding: 0; }
.report-page { font-family: 'Inter', sans-serif; color: #0f172a; background: #f8fafc; min-height: 100vh; display: flex; flex-direction: column; }
.container { max-width: 1100px; margin: 0 auto; padding: 0 24px; }

.page-body {
  flex: 1; display: grid; grid-template-columns: 1fr 1fr;
  max-width: 1100px; margin: 0 auto; width: 100%;
  padding: 100px 24px 60px; gap: 64px; align-items: center;
}

.field-hint { font-size: 11px; color: #94a3b8; margin-top: 2px; }
.section-label { display: block; font-size: 12px; font-weight: 600; letter-spacing: 2px; text-transform: uppercase; color: #0369a1; margin-bottom: 12px; }
.info-title { font-family: 'Sora', sans-serif; font-size: clamp(28px, 4vw, 42px); font-weight: 800; line-height: 1.15; margin-bottom: 16px; }
.info-desc { color: #64748b; font-size: 16px; line-height: 1.7; margin-bottom: 36px; }
.info-steps { display: flex; flex-direction: column; gap: 20px; margin-bottom: 32px; }
.info-step { display: flex; gap: 16px; align-items: flex-start; }
.step-num { width: 30px; height: 30px; border-radius: 50%; background: #e0f2fe; color: #0369a1; font-family: 'Sora', sans-serif; font-weight: 700; font-size: 13px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.info-step strong { display: block; font-size: 14px; font-weight: 600; margin-bottom: 2px; }
.info-step p { font-size: 13px; color: #64748b; line-height: 1.5; }
.info-badge { display: flex; gap: 12px; align-items: flex-start; background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 12px; padding: 14px 16px; }
.info-badge span { font-size: 18px; flex-shrink: 0; }
.info-badge p { font-size: 13px; color: #0369a1; line-height: 1.5; }

.form-card { background: white; border: 1.5px solid #e2e8f0; border-radius: 24px; padding: 36px; box-shadow: 0 8px 40px rgba(3,105,161,0.08); }
.form-title { font-family: 'Sora', sans-serif; font-size: 20px; font-weight: 800; margin-bottom: 6px; }
.form-sub { font-size: 14px; color: #64748b; margin-bottom: 28px; }
.form-body { display: flex; flex-direction: column; gap: 22px; }

.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 13px; font-weight: 600; color: #334155; }
.required { color: #ef4444; margin-left: 2px; }
.optional { color: #94a3b8; font-weight: 400; }
.field input, .field textarea {
  padding: 11px 14px; border: 1.5px solid #e2e8f0; border-radius: 10px;
  font-size: 14px; font-family: 'Inter', sans-serif; color: #0f172a;
  background: #f8fafc; transition: border-color .2s, box-shadow .2s; resize: none; outline: none;
}
.field input:focus, .field textarea:focus { border-color: #0369a1; box-shadow: 0 0 0 3px rgba(3,105,161,0.1); background: white; }
.field input.error, .field textarea.error { border-color: #ef4444; box-shadow: 0 0 0 3px rgba(239,68,68,0.1); }
.field-footer { display: flex; justify-content: flex-end; }
.char-count { font-size: 11px; color: #94a3b8; }
.field-error { font-size: 12px; color: #ef4444; }

.tipo-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.tipo-btn { display: flex; align-items: center; gap: 8px; padding: 12px 14px; border: 1.5px solid #e2e8f0; border-radius: 12px; background: #f8fafc; cursor: pointer; font-family: 'Inter', sans-serif; transition: all .2s; }
.tipo-btn:hover { border-color: var(--tc); background: color-mix(in srgb, var(--tc) 6%, white); }
.tipo-btn.selected { border-color: var(--tc); background: color-mix(in srgb, var(--tc) 10%, white); box-shadow: 0 0 0 3px color-mix(in srgb, var(--tc) 15%, transparent); }
.tipo-icon { font-size: 18px; }
.tipo-label { font-size: 13px; font-weight: 500; color: #334155; }

.urgencia-row { display: flex; gap: 10px; }
.urgencia-btn { flex: 1; padding: 9px 8px; border: 1.5px solid #e2e8f0; border-radius: 10px; background: #f8fafc; cursor: pointer; font-size: 13px; font-family: 'Inter', sans-serif; font-weight: 500; color: #334155; transition: all .2s; }
.urgencia-btn:hover { border-color: var(--uc); }
.urgencia-btn.selected { border-color: var(--uc); background: color-mix(in srgb, var(--uc) 10%, white); color: var(--uc); font-weight: 600; }

/* ── FOTO ── */
.foto-drop {
  border: 2px dashed #e2e8f0; border-radius: 12px; padding: 24px;
  text-align: center; cursor: pointer; transition: all .2s;
  background: #f8fafc; position: relative; min-height: 120px;
  display: flex; align-items: center; justify-content: center;
}
.foto-drop:hover, .foto-drop--active { border-color: #0369a1; background: #f0f9ff; }
.foto-drop--filled { padding: 0; border-style: solid; border-color: #bae6fd; }
.foto-placeholder span { font-size: 32px; display: block; margin-bottom: 8px; }
.foto-placeholder p { font-size: 13px; color: #64748b; margin-bottom: 4px; }
.foto-placeholder small { font-size: 11px; color: #94a3b8; }
.foto-preview { width: 100%; max-height: 200px; object-fit: cover; border-radius: 10px; display: block; }
.foto-remove {
  position: absolute; top: 8px; right: 8px;
  width: 28px; height: 28px; border-radius: 50%;
  background: rgba(0,0,0,0.6); color: white; border: none;
  font-size: 13px; cursor: pointer; display: flex; align-items: center; justify-content: center;
}

.geo-status { display: flex; align-items: center; gap: 10px; font-size: 13px; color: #0369a1; background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 10px; padding: 10px 14px; }
.geo-spinner { width: 14px; height: 14px; border: 2px solid #bae6fd; border-top-color: #0369a1; border-radius: 50%; animation: spin .7s linear infinite; flex-shrink: 0; }

.btn-submit { width: 100%; padding: 14px; background: #0369a1; color: white; border: none; border-radius: 12px; font-size: 15px; font-weight: 600; font-family: 'Inter', sans-serif; cursor: pointer; transition: background .2s, transform .2s, box-shadow .2s; display: flex; align-items: center; justify-content: center; min-height: 50px; box-shadow: 0 4px 20px rgba(3,105,161,0.3); }
.btn-submit:hover:not(:disabled) { background: #0284c7; transform: translateY(-1px); box-shadow: 0 6px 24px rgba(3,105,161,0.4); }
.btn-submit:disabled { opacity: 0.7; cursor: not-allowed; }
.loader { width: 18px; height: 18px; border: 2px solid rgba(255,255,255,0.4); border-top-color: white; border-radius: 50%; animation: spin .7s linear infinite; display: inline-block; }
@keyframes spin { to { transform: rotate(360deg); } }

.success-state { text-align: center; padding: 20px 0; display: flex; flex-direction: column; align-items: center; gap: 12px; }
.success-icon { font-size: 52px; }
.success-state h3 { font-family: 'Sora', sans-serif; font-size: 22px; font-weight: 800; }
.success-state p { color: #64748b; font-size: 15px; }
.btn-main { display: inline-block; background: #0369a1; color: white; padding: 12px 28px; border-radius: 30px; text-decoration: none; font-weight: 600; font-size: 14px; margin-top: 8px; }
.btn-ghost { background: none; border: none; color: #64748b; font-size: 13px; cursor: pointer; font-family: 'Inter', sans-serif; text-decoration: underline; }

.footer { background: #0f172a; color: #64748b; padding: 24px 0; font-size: 13px; }
.footer-inner { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px; max-width: 1100px; margin: 0 auto; padding: 0 24px; }

@media (max-width: 768px) {
  .page-body { grid-template-columns: 1fr; padding-top: 80px; gap: 32px; }
  .info-col { display: none; }
}
</style>
