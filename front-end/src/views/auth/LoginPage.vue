<template>
    <AuthLayout>
        <h2>登入</h2>
        <form @submit.prevent="onSubmit">
            <div>
                <label for="email">Email</label>
                <input v-model="email" id="email" type="email" required />
            </div>
            <div>
                <label for="password">密碼</label>
                <input v-model="password" id="password" type="password" required />
            </div>
            <button type="submit">登入</button>
        </form>
        <p>
        沒有帳號？<router-link to="/register">註冊</router-link>
        </p>
    </AuthLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AuthLayout from '@/components/layout/AuthLayout.vue'

const email = ref('')
const password = ref('')

function onSubmit() {
    // TODO: 登入邏輯
    alert('登入成功')
}
</script>