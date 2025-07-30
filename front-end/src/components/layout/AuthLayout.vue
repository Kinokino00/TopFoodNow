<template>
    <div class="auth-layout">
        <main>
            <slot />
        </main>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

const props = defineProps({
    title: {
        type: String,
        default: '登入'
    }
})
</script>

<style scoped>
</style>