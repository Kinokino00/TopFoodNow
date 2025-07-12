<template>
  <button
    class="button"
    :class="state.getClasses"
    :id="state.buttonState.id || `button-${state.buttonState.label}`"
    :disabled="state.buttonState.disabled"
    :type="state.buttonState.btnType || 'button'"
  >
    <font-awesome-icon
      v-if="state.buttonState.icon"
      :icon="state.buttonState.icon"
      class="icon"
      :class="state.buttonState.iconClass"
    />
    <img
      v-if="state.getImg"
      :src="state.getImg"
      :class="[
        state.buttonState.imgClass,
        { 'contrast-60': state.buttonState.disabled }
      ]"
    />
    <span
      v-if="state.buttonState.label"
      :class="state.buttonState.labelClass"
    >
      {{ state.buttonState.label }}
    </span>
  </button>
</template>


<script lang="ts" setup>
import { computed, reactive, watchEffect } from 'vue'
import chartSearchIcon from '@/assets/images/chartSearchIcon.png'

export type ButtonState = {
  id?: string
  icon?: string[]
  iconClass?: string
  img?: string
  imgClass?: string
  label?: string
  labelClass?: string
  disabled?: boolean
  size?: string    // 'lg' | 'md'
  color?: string   // 'primary' | 'secondary' | 'danger' | 'white'
  btnType?: string // 'button' | 'submit' | 'reset'
}
const props = defineProps<{ buttonState: ButtonState }>()
const state: any = reactive({
  buttonState: props.buttonState,
  getClasses: computed(() => ({
    'button-icon': state.buttonState.icon && !state.buttonState.label,
    'button-iconLabel': (state.buttonState.icon || state.buttonState.img) && state.buttonState.label,
    [`button-${state.buttonState.size || 'md'}`]: state.buttonState.size,
    [`button-${state.buttonState.color}`]: state.buttonState.color,
    'button-search': state.isButtonSearch,
  })),
  getImg: computed(() => state.buttonState.img || (state.isButtonSearch ? chartSearchIcon : '')),
  isButtonSearch: computed(() => state.buttonState.label == '搜尋' || state.buttonState.label == '分析'),
})
watchEffect(() => state.buttonState = props.buttonState)
</script>


<style lang="scss" scoped>
.button {
  @apply h-10 px-3 bg-gray-600 text-white whitespace-nowrap rounded-lg outline-none hover:bg-gray-500 active:bg-gray-700;
  &:disabled {
    &, &:hover {
      @apply bg-gray-100 text-gray-500 border-none;
    }
  }

  &-lg {
    @apply py-3 text-base font-bold leading-4;
  }
  &-md {
    @apply h-[30px] py-[7px] text-sm font-normal leading-3;
  }

  &-primary {
    @apply bg-primary-500 hover:bg-primary-400 active:bg-primary-700;
    &-light {
      @apply text-gray-600 bg-primary-50 hover:bg-primary-100;
    }
  }
  &-secondary {
    @apply bg-secondary-500 hover:bg-secondary-400 active:bg-secondary-700;
  }
  &-danger {
    @apply bg-danger-500 hover:bg-danger-400 active:bg-danger-700;
  }
  &-white {
    @apply bg-white text-gray-600 border border-gray-200 active:bg-gray-100;
    &:hover {
      @apply bg-white text-primary-500;
    }
  }

  &-icon {
    @apply h-10 px-4;
    svg {
      @apply text-lg;
    }
    &Label {
      @apply flex items-center gap-1 py-2;
    }
  }

  &-search {
    @apply flex items-center justify-center gap-1 px-7;
    @extend .button-lg;
    @extend .button-primary;
  }

  &-transparent {
    @apply w-fit bg-transparent text-gray-600 hover:bg-primary-50;
    &:active {
      @apply bg-primary-500 text-white;
    }
    &:disabled {
      @apply bg-gray-200 text-gray-500;
      .icon {
        @apply opacity-40;
      }
    }
    &:has(.icon) {
      @apply w-6 h-6 px-1 py-0 hover:bg-transparent;
      .icon.text-primary-500 {
        @apply hover:text-primary-400 active:text-primary-700;
      }
      .icon.text-secondary-500 {
        @apply hover:text-secondary-400 active:text-secondary-700;
      }
      .icon.text-danger-500 {
        @apply hover:text-danger-400 active:text-danger-700;
      }
    }
    &:disabled, &:disabled:hover {
      @apply bg-transparent;
      .icon {
        &, &:hover {
          @apply text-gray-500;
        }
      }
    }
  }
}
</style>
