<template>
  <HeaderNav />
  <ScrollBar class="max-h-[calc(100vh-104px)]">
    <RouterView />
  </ScrollBar>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import HeaderNav from '@/components/layout/HeaderNav.vue'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'
import '@fortawesome/fontawesome-free/css/all.css'
import 'vue-datepicker-next/index.css'
import '@/assets/style/all.scss'
</script>