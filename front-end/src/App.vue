<template>
  <HeaderNav />
  <RouterView />
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import HeaderNav from '@/components/layout/HeaderNav.vue'
import '@fortawesome/fontawesome-free/css/all.css'
import 'vue-datepicker-next/index.css'
import '@/assets/style/all.scss'
</script>
