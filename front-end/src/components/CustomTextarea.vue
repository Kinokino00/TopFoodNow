<template>
  <div>
    <div
      :class="[
        'component-' + state.getLayout,
        state.textareaState.width
      ]"
    >
      <p
        v-if="state.textareaState.label"
        class="labelText"
        :class="state.getLabelClass"
      >
        {{ state.textareaState.label }}
      </p>
      <div
        class="inputDiv !px-1"
        :class="[
          state.getClasses,
          state.textareaState.inputDivClass
        ]"
      >
        <textarea
          type="text"
          class="min-h-[70px]"
          :value="state.textareaState.modelValue"
          :placeholder="state.textareaState.placeholder ?? '請輸入'"
          @input="inputEmits"
          @change="state.textareaState.inputChange"
          :disabled="state.textareaState.disabled"
          :readonly="state.textareaState.readonly"
        />
      </div>
    </div>
    <div class="flex pl-2" v-if="state.textareaState.errorMessage">
      <div
        v-if="state.textareaState.layout === 'row' && state.textareaState.label"
        class="mx-2 labelText"
        :class="state.getLabelClass"
      ></div>
      <p class="errorText">{{ state.textareaState.errorMessage }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, watchEffect } from 'vue'


export type TextareaState = {
  modelValue: string | number
  layout?: string     // 預設 'col'
  width?: string
  labelClass?: string  // 預設 'min-w-[100px]'
  label?: string
  inputDivClass?: string
  placeholder?: string
  disabled?: boolean
  readonly?: boolean
  errorMessage?: string
  inputChange?: (event: Event) => void
}
const props = defineProps<{ textareaState: TextareaState }>()
const state: any = reactive({
  textareaState: props.textareaState,
  getLayout: computed(() => state.textareaState.layout || 'col'),
  getClasses: computed(() => ({
    '!border-gray-200 !bg-gray-50': state.textareaState.disabled,
    errorText: state.textareaState.errorMessage,
    inputDivError: state.textareaState.errorMessage,
    inputDivReadonly: state.textareaState.readonly
  })),
  getLabelClass: computed(() => {
    const defaultClass = 'min-w-[100px]'
    const labelClass = state.textareaState.labelClass || ''
    const layoutRowClass = state.textareaState.layout === 'row' ? 'pt-2.5' : ''
    return labelClass ? `${labelClass} ${layoutRowClass}` : `${defaultClass} ${layoutRowClass}`
  })
})
watchEffect(() => state.inputState = props.textareaState)

const inputEmits = (event: Event) => {
  const inputVal = (event.target as HTMLInputElement)?.value
  emit('update:modelValue', inputVal)
}
const emit = defineEmits(['update:modelValue'])
</script>
