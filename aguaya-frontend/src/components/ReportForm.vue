<template>

<div class="container mt-4">

<h2>Reportar problema de agua</h2>

<form @submit.prevent="guardarReporte">

<div class="mb-3">
<label>Barrio</label>
<input v-model="barrio" class="form-control">
</div>

<div class="mb-3">
<label>Tipo de problema</label>

<select v-model="tipo" class="form-control">

<option>No hay agua</option>
<option>Baja presión</option>
<option>Agua sucia</option>
<option>Fuga de agua</option>

</select>

</div>

<div class="mb-3">
<label>Comentario</label>
<textarea v-model="comentario" class="form-control"></textarea>
</div>

<button class="btn btn-primary">
Enviar Reporte
</button>

</form>

</div>

</template>

<script>

export default {

data(){
return{
barrio:"",
tipo:"",
comentario:""
}
},

methods:{

guardarReporte(){

const reporte = {
barrio:this.barrio,
tipo:this.tipo,
comentario:this.comentario,
fecha:new Date()
}

let reportes = JSON.parse(localStorage.getItem("reportes")) || []

reportes.push(reporte)

localStorage.setItem("reportes",JSON.stringify(reportes))

alert("Reporte enviado")

}

}

}

</script>