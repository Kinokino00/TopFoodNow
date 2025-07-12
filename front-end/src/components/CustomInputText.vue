<template>
  <div>
    <div
      :class="[
        'component-' + (state.inputTextState.layout ?? 'col'),
        state.inputTextState.width
      ]"
    >
      <p
        v-if="state.inputTextState.label"
        class="labelText"
        :class="state.getLabelClass"
      >
        <font-awesome-icon
          class="icon"
          :icon="state.inputTextState.icon"
          :class="state.inputTextState.iconClass"
        />
        {{ state.inputTextState.label }}
      </p>
      <div
        class="inputDiv"
        :class="[
          state.getClasses,
          state.inputTextState.inputDivClass
        ]"
      >
        <input
          type="text"
          class="input"
          :class="state.inputTextState.inputClass"
          :value="state.inputTextState.modelValue"
          :placeholder="state.inputTextState.placeholder ?? '請輸入'"
          @input="inputEmits"
          @change="state.inputTextState.inputChange"
          :disabled="state.inputTextState.disabled"
          :readonly="state.inputTextState.readonly"
          :dataTestId="state.inputTextState.dataTestId"
        />
      </div>
    </div>
    <div v-if="state.inputTextState.errorMessage" class="flex pl-2">
      <div
        v-if="state.inputTextState.layout === 'row' && state.inputTextState.label"
        class="mx-2 labelText"
        :class="state.getLabelClass"
      ></div>
      <p class="errorText">{{ state.inputTextState.errorMessage }}</p>
    </div>
  </div>
</template>


<script setup lang="ts">
import { reactive, computed, watchEffect } from 'vue'

export type InputTextState = {
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
const props = defineProps<{ inputTextState: InputTextState }>()
const state: any = reactive({
  inputTextState: props.inputTextState,
  getClasses: computed(() => ({
    '!border-gray-200 !pr-2.5': state.inputTextState.disabled,
    'inputDivError': state.inputTextState.errorMessage,
    'inputDivReadonly': state.inputTextState.readonly
  })),
  getLabelClass: computed(() => {
    const labelClass = state.inputTextState.labelClass || ''
    if (state.inputTextState.icon && labelClass.includes('text-end')) {
      return `flex items-center justify-end gap-1 ${labelClass}`
    }
    return labelClass || 'min-w-[100px]'
  })
})
watchEffect(() => state.inputTextState = props.inputTextState)

const inputEmits = (event: Event) => {
  emit('update:modelValue', (event.target as HTMLInputElement)?.value)
}
const emit = defineEmits(['update:modelValue'])
</script>


<style lang="scss" scoped>
@import '@/assets/style/inputTextArea.scss';
</style>
