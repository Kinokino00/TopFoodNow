<template>
  <div
    v-if="state.toastState.visible"
    class="toast"
    :class="state.getClasses"
  >
    <font-awesome-icon
      :icon="['fas', state.getIcon]"
      class="icon text-5xl"
      :class="state.toastState.iconClass"
    />
    <p
      v-if="state.toastState.label"
      :class="state.toastState.labelClass"
    >
      {{ state.toastState.label }}
    </p>
  </div>
</template>


<script lang="ts" setup>
import { computed, onMounted, reactive, watch, watchEffect } from 'vue'

export type ToastState = {
  visible: boolean
  color: string
  label: string
  visibleTime?: number // 預設 2000
  icon?: string
  iconClass?: string
  labelClass?: string
}
const props = defineProps<{ toastState: ToastState }>()

const state: any = reactive({
  toastState: props.toastState,
  getClasses: computed(() => ({
    [`toast-${state.toastState.color}`]: state.toastState.color,
    'show': state.toastState.visible,
    'hidden': !state.toastState.visible,
  })),
  getIcon: computed(() => {
    const iconMap = {
      success: 'circle-check',
      failed: 'circle-xmark',
      nonstandard: 'circle-exclamation'
    }
    return iconMap[state.toastState.color as keyof typeof iconMap]
  }),
})
watchEffect(() => state.toastState = props.toastState)

watch(() => state.toastState.visible, (newVal) => {
  state.toastState.visible = newVal
  if (newVal) {
    setTimeout(() => {
      state.toastState.visible = false
      emit('update:state.toastState.visible', false)
    }, state.toastState.visibleTime)
  }
})
onMounted(() => {
  if (state.toastState.visible.value) {
    setTimeout(() => {
      state.toastState.visible.value = false
      emit('update:state.toastState.visible', false)
    }, state.toastState.visibleTime)
  }
})
const emit = defineEmits(['update:state.toastState.visible'])
</script>


<style lang="scss" scoped>
.toast {
  @apply z-[1500] fixed top-24 left-1/2 flex items-center justify-center gap-4 w-[420px] p-5 text-2xl border rounded-lg -translate-x-1/2 opacity-0;
  box-shadow: 0 4px 6px -3px rgba(0, 0, 0, .1), 0 10px 15px -4px rgba(0, 0, 0, .1);

  &-success {
    @apply bg-secondary-50 border-secondary-500 text-secondary-500;
  }
  &-failed {
    @apply bg-danger-50 border-danger-500 text-danger-500;
  }
  &-nonstandard {
    @apply bg-warning-50 border-warning-600 text-warning-600;
  }

  &.show {
    @apply opacity-100 flex;
    animation: showAnimation .5s ease-in-out;

    @keyframes showAnimation {
      0%  { @apply -top-24; }
      100%{ @apply top-24; }
    }
  }
  &.hidden {
    animation: hiddenAnimation .5s ease-in-out;

    @keyframes hiddenAnimation {
      0%  { @apply opacity-100 top-24; }
      100%{ @apply opacity-0 top-0; }
    }
  }
}
</style>
