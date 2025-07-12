<template>
  <div>
    <div
      :class="[
        'component-' + (state.inputNumberState.layout ?? 'col'),
        state.inputNumberState.width
      ]"
    >
      <p
        v-if="state.inputNumberState.label"
        class="labelText"
        :class="state.getLabelClass"
      >
        <font-awesome-icon
          class="icon"
          :icon="state.inputNumberState.icon"
          :class="state.inputNumberState.iconClass"
        />
        {{ state.inputNumberState.label }}
      </p>
      <div
        class="inputDiv"
        :class="[
          state.getClasses,
          state.inputNumberState.inputDivClass
        ]"
      >
        <input
          type="number"
          class="input inputNumber"
          :class="state.inputNumberState.inputClass"
          :value="state.inputNumberState.modelValue"
          :placeholder="state.inputNumberState.placeholder ?? '請輸入'"
          @input="inputEmits"
          @change="state.inputNumberState.inputChange"
          :disabled="state.inputNumberState.disabled"
          :readonly="state.inputNumberState.readonly"
          :step="state.inputNumberState.step ?? 1"
          :min="state.inputNumberState.min ?? 0"
          :max="state.inputNumberState.max ?? Infinity"
          :dataTestId="state.inputNumberState.dataTestId"
        />
      </div>
    </div>
    <div v-if="state.inputNumberState.errorMessage" class="flex pl-2">
      <div
        v-if="state.inputNumberState.layout === 'row' && state.inputNumberState.label"
        class="mx-2 labelText"
        :class="state.getLabelClass"
      ></div>
      <p class="errorText">{{ state.inputNumberState.errorMessage }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, watchEffect } from 'vue'

export type InputNumberState = {
  modelValue: number
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
  isPaginate?: boolean
  step?: number        // 預設 1
  min?: number         // 預設 0
  max?: number         // 預設 Infinity
  dataTestId?: string
}
const props = defineProps<{ inputNumberState: InputNumberState }>()
const state: any = reactive({
  inputNumberState: props.inputNumberState,
  getClasses: computed(() => ({
    'inputNumberPaginate': state.inputNumberState.isPaginate,
    '!border-gray-200 !pr-2.5': state.inputNumberState.disabled,
    'inputDivError': state.inputNumberState.errorMessage,
    'inputDivReadonly': state.inputNumberState.readonly
  })),
  getLabelClass: computed(() => {
    const labelClass = state.inputNumberState.labelClass || ''
    if (state.inputNumberState.icon && labelClass.includes('text-end')) {
      return `flex items-center justify-end gap-1 ${labelClass}`
    }
    return labelClass || 'min-w-[100px]'
  })
})
watchEffect(() => state.inputNumberState = props.inputNumberState)

const inputEmits = (event: Event) => {
  emit('update:modelValue', (event.target as HTMLInputElement)?.value)
}
const emit = defineEmits(['update:modelValue'])
</script>

<style lang="scss" scoped>
@import '@/assets/style/inputTextArea.scss';
</style>