<template>
    <AuthLayout>
        <h2>註冊</h2>
        <form @submit.prevent="onSubmit">
            <div>
                <label for="email">Email</label>
                <input v-model="email" id="email" type="email" required />
            </div>
            <div>
                <label for="password">密碼</label>
                <input v-model="password" id="password" type="password" required />
            </div>
            <div>
                <label for="confirmPassword">確認密碼</label>
                <input v-model="confirmPassword" id="confirmPassword" type="password" required />
            </div>
            <button type="submit">註冊</button>
        </form>
        <p>
        已有帳號？<router-link to="/login">登入</router-link>
        </p>
    </AuthLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AuthLayout from '@/components/layout/AuthLayout.vue'

const email = ref('')
const password = ref('')
const confirmPassword = ref('')

function onSubmit() {
    if (password.value !== confirmPassword.value) {
        alert('密碼不一致')
        return
    }
    // TODO: 註冊邏輯
    alert('註冊成功')
}
</script>