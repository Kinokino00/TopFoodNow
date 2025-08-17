<template>
  <button
    ref="headerButtonRef"
    class="header flex items-center justify-center top-5 left-5 w-7 h-7 p-1 bg-white rounded shadow-gray sm:hidden"
    @click.stop="isOpen = !isOpen"
  >
    <font-awesome-icon icon="fa-solid fa-bars" class="text-lg text-primary-500" />
  </button>

  <div class="header hidden justify-between top-0 p-5 w-full sm:flex md:p-7">
    <img
      src="@/assets/images/logo.svg"
      alt="logo"
      class="w-[90px] cursor-pointer md:w-[102px]"
      style="
        filter: drop-shadow(0 0 4px white) drop-shadow(0 0 4px white) drop-shadow(0 0 4px white);
      "
      @click="router.push({ name: 'Home' })"
    />
    <button
      v-if="userStore.isAuthenticated"
      class="flex items-center gap-1.5 h-fit"
      @click.stop="isOpen = !isOpen"
    >
      <p class="p-shadow text-shadow-black">{{ userStore.user?.name || '用戶名' }}</p>
      <font-awesome-icon
        icon="fa-solid fa-caret-down"
        style="filter: drop-shadow(0 0 4px black) drop-shadow(0 0 4px black)"
      />
    </button>
    <RouterLink v-else to="/login">
      <p class="p-shadow text-shadow-black">登入</p>
    </RouterLink>
  </div>

  <ul v-if="isOpen" ref="menuRef" class="options">
    <li v-if="!userStore.isAuthenticated">
      <RouterLink to="/login">登入</RouterLink>
    </li>
    <template v-else>
      <li @click="goToUserRecommendations">個人推薦總覽</li>
      <li @click="handleLogout">登出</li>
    </template>
  </ul>

  <CustomDialog :dialogState="logoutState">
    <p class="text-center">登出中...</p>
  </CustomDialog>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import CustomDialog from '@/components/CustomDialog.vue'

const router = useRouter()
const isOpen = ref(false)
const userStore = useUserStore()

const headerButtonRef = ref<HTMLButtonElement | null>(null)
const menuRef = ref<HTMLUListElement | null>(null)

const goToUserRecommendations = () => {
  isOpen.value = false
  const userId = userStore.user?.id
  if (userId) {
    router.push({ name: 'userRecommendations', params: { userId: userId.toString() } })
  } else {
    router.push({ name: 'Login' })
  }
}

const handleLogout = async () => {
  isOpen.value = false
  logoutState.visible = true
  await userStore.handleLogout()
  logoutState.visible = false
  router.push({ name: 'Home' })
}

const handleOutsideClick = (event: MouseEvent) => {
  if (!isOpen.value) return

  const isClickInsideButton = headerButtonRef.value?.contains(event.target as Node)
  const isClickInsideMenu = menuRef.value?.contains(event.target as Node)

  if (!isClickInsideButton && !isClickInsideMenu) isOpen.value = false
}

const logoutState = reactive({
  visible: false,
  closeBtn: false,
  cancelBtn: false,
  confirmBtn: false
})

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
})
onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick)
})
</script>

<style lang="scss" scoped>
.header,
.options {
  @apply fixed z-[5000];
}
.header {
  @apply text-white;
  .p-shadow {
    @apply text-sm md:text-base;
  }
}
.options {
  @apply top-[3.5rem] left-5 p-1.5 w-fit bg-white text-sm text-gray-600 rounded shadow-gray sm:top-16 sm:left-auto sm:right-6 sm:p-2;
  li {
    @apply py-1.5 px-2 cursor-pointer;
    &:hover {
      @apply bg-primary-100 rounded;
    }
  }
}
</style>
