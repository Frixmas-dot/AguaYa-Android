<template>
  <AppNavbar />
  <router-view />
</template>

<script setup>
import AppNavbar from '@/components/AppNavbar.vue'
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Sora:wght@400;600;700;800&family=Inter:wght@400;500&display=swap');

*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Inter', sans-serif;
  color: #0f172a;
  background: #fff;
}
</style>