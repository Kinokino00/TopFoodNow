<template>
  <div class="checkBoxItems">
    <label class="checkboxRadio" :class="state.getCheckBoxClass">
      <input
        type="checkbox"
        class="peer"
        :checked="state.checkboxState.modelValue"
        :id="state.checkboxState.id || state.checkboxState.name"
        :disabled="state.checkboxState.disabledName === state.checkboxState.name || state.checkboxState.disabled"
        @change="handleCheckboxChange"
      />
      <span class="peer-checked:!opacity-100">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 20 20"
          fill="currentColor"
          stroke="currentColor"
          stroke-width="1"
        >
          <path
            fill-rule="evenodd"
            d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
            clip-rule="evenodd"
          ></path>
        </svg>
      </span>
    </label>
    <label
      :for="state.checkboxState.id || state.checkboxState.name"
      class="labelText !m-0 !min-w-fit"
      :class="[
        { '!cursor-auto': state.checkboxState.disabledName === state.checkboxState.name },
        state.checkboxState.labelTextClass
      ]"
    >
      {{ state.checkboxState.name }}
    </label>
  </div>
</template>


<script lang="ts" setup>
import { computed, reactive, watchEffect } from 'vue'

export type CheckboxState = {
  modelValue: boolean
  name: string
  id?: string
  labelTextClass?: string
  disabledName?: string
  disabled?: boolean
  isError?: boolean
}
const props = defineProps<{ checkboxState: CheckboxState }>()
const state: any = reactive({
  checkboxState: props.checkboxState,
  getCheckBoxClass: computed(() => ({
    'checkboxRadioError': state.checkboxState.isError
  }))
})
watchEffect(() => state.checkboxState = props.checkboxState)

const emit = defineEmits(['update:modelValue'])
const handleCheckboxChange = () => {
  state.checkboxState.modelValue = !state.checkboxState.modelValue
  emit('update:modelValue', state.checkboxState.modelValue)
}
</script>


<style lang="scss" scoped>
@import '@/assets/style/checkboxRadio.scss';
</style>
