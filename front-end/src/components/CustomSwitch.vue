<template>
  <label class="switchWrapper">
    <input
      type="checkbox"
      class="switch sr-only peer"
      :class="[
        { isChecked: state.switchState.modelValue },
        { isLocked: state.switchState.isLocked }
      ]"
      :checked="state.switchState.modelValue"
      :disabled="state.switchState.disabled"
      @change="handleChange"
    />
    <div
      class="switchStyle peer"
      :class="[
        { switchStyleError: state.switchState.isError && !state.switchState.modelValue },
        state.dashboardSwitch
      ]"
    ></div>
  </label>
</template>

<script setup lang="ts">
import { reactive, computed, watchEffect } from 'vue'
import { useRoute } from 'vue-router'

export type SwitchState = {
  modelValue: boolean
  disabled?: boolean
  isLocked?: boolean
  isError?: boolean
}
const props = defineProps<{ switchState: SwitchState }>()
const state = reactive({
  switchState: props.switchState,
  dashboardSwitch: computed(() =>
    route.name === 'Dashboard'
      ? '!bg-primary-500 peer-checked:after:translate-x-[140%] peer-checked:!bg-secondary-500'
      : 'switchDefault'
  )
})
watchEffect(() => state.switchState = props.switchState)

const route = useRoute()

const emit = defineEmits(['update:modelValue'])
const handleChange = (event: any) => {
  emit('update:modelValue', event.target.checked)
}
</script>

<style lang="scss" scoped>
.switchWrapper {
  @apply relative flex items-center rounded-full outline-none cursor-pointer;

  &:hover .switchStyle.switchDefault {
    @apply bg-gray-300;
  }
  &:focus,
  &:focus-within,
  &:focus-visible {
    .switchStyle.switchDefault {
      @apply shadow-gray;
    }
    &:has(.switch.isChecked) {
      .switchStyle.switchDefault {
        @apply shadow-secondary;
      }
    }
    &:has(.switchStyleError) {
      .switchStyle.switchDefault {
        @apply shadow-danger;
      }
    }
  }
}

.switch {
  &.isChecked {
    & ~ .switchStyle {
      @apply bg-secondary-500 hover:bg-secondary-700;
    }
    &.isLocked ~ .switchStyle.switchDefault {
      @apply bg-danger-500 hover:bg-danger-700;
    }
  }
  &:disabled ~ .switchStyle {
    &,
    &:hover {
      @apply bg-gray-100;
    }
    &:focus,
    &:focus-within,
    &:focus-visible {
      @apply shadow-none;
    }
  }
}

.switchStyle {
  @apply w-[42px] h-[22px] bg-gray-200 peer-focus:outline-1 rounded-full;
  &:after {
    @apply content-[''] absolute h-[14px] w-[14px] top-1 start-1 ring-0 ring-gray-100 bg-white rounded-full transition-all;
    filter: drop-shadow(0 10px 15px rgba(0, 0, 0, 0.1)) drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1));
  }

  &.switchDefault {
    @apply peer-checked:after:translate-x-[140%] peer-checked:bg-secondary-500;
  }

  &Error {
    @apply border border-danger-500;
  }
}
</style>
