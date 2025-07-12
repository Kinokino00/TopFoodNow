<template>
  <div>
    <div
      :class="[
        'component-' + (state.passwordState.layout ?? 'col'),
        state.passwordState.width
      ]"
    >
      <p
        v-if="state.passwordState.label"
        class="labelText"
        :class="state.getLabelClass"
      >
        <font-awesome-icon
          class="icon"
          :icon="state.passwordState.icon"
          :class="state.passwordState.iconClass"
        />
        {{ state.passwordState.label }}
      </p>
      <div
        class="inputDiv flex items-center"
        :class="[
          state.getClasses,
          state.passwordState.inputDivClass
        ]"
      >
        <input
          :type="state.isActive ? 'password' : 'text'"
          class="input"
          :class="state.passwordState.inputClass"
          :value="state.passwordState.modelValue"
          :placeholder="state.passwordState.placeholder ?? '請輸入'"
          @input="inputEmits"
          @change="state.passwordState.inputChange"
          :disabled="state.passwordState.disabled"
          :readonly="state.passwordState.readonly"
          :dataTestId="state.passwordState.dataTestId"
        />
        <font-awesome-icon
          class="text-sm cursor-pointer"
          :icon="['fas', state.isActive ? 'eye-slash' : 'eye']"
          @click="state.isActive = !state.isActive"
        />
      </div>
    </div>
    <div v-if="state.passwordState.errorMessage" class="flex pl-2">
      <div
        v-if="state.passwordState.layout === 'row' && state.passwordState.label"
        class="mx-2 labelText"
        :class="state.getLabelClass"
      ></div>
      <p class="errorText">{{ state.passwordState.errorMessage }}</p>
    </div>
  </div>
</template>


<script setup lang="ts">
import { reactive, computed, watchEffect } from 'vue'

export type PasswordState = {
  modelValue: string
  layout?: string      // 預設 'col'
  width?: string
  iconClass?: string
  icon?: string[]
  labelClass?: string  // 預設 'min-w-[100px]'
  label?: string
  inputDivClass?: string
  inputClass?: string
  inputChange?: (event: Event) => void
  placeholder?: string // 預設 '請輸入'
  disabled?: boolean   // 預設 false
  errorMessage?: string
  readonly?: boolean
  dataTestId?: string
}
const props = defineProps<{ passwordState: PasswordState }>()
const state: any = reactive({
  isActive: true,
  passwordState: props.passwordState,
  getClasses: computed(() => ({
    '!border-gray-200 !pr-2.5': state.passwordState.disabled,
    'inputDivError': state.passwordState.errorMessage,
    'inputDivReadonly': state.passwordState.readonly
  })),
  getLabelClass: computed(() => {
    const { labelClass = '', icon } = state.passwordState
    return icon && labelClass.includes('text-end') 
      ? `flex items-center justify-end gap-1 ${labelClass}` 
      : labelClass || 'min-w-[100px]'
  })
})
watchEffect(() => state.passwordState = props.passwordState)

const inputEmits = (event: Event) => {
  emit('update:modelValue', (event.target as HTMLInputElement)?.value)
}
const emit = defineEmits(['update:modelValue'])
</script>


<style lang="scss" scoped>
@import '@/assets/style/inputTextArea.scss';
</style>
