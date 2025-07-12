<template>
  <div>
    <div
      :class="[
        'component-' + state.getLayout,
        state.passwordState.width
      ]"
    >
      <p
        v-if="state.passwordState.label"
        class="labelText"
        :class="state.getLabelClass"
      >
        {{ state.passwordState.label }}
      </p>
      <div class="inputDiv" :class="[state.getClasses, state.passwordState.inputDivClass]">
        <Password
          v-bind="$attrs"
          :feedback="false"
          toggleMask
          v-model="state.passwordState.modelValue"
          :placeholder="state.passwordState.placeholder ?? '請輸入'"
        />
      </div>
    </div>
    <div class="flex pl-2" v-if="state.passwordState.errorMessage">
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
import Password from 'primevue/password'

export type PasswordState = {
  modelValue: string
  layout?: string
  width?: string
  labelClass?: string
  label?: string
  inputChange?: (event: Event) => void
  placeholder?: string
  errorMessage?: string
}
const props = defineProps<{ passwordState: PasswordState }>()
const state: any = reactive({
  passwordState: props.passwordState,
  getLayout: computed(() => state.passwordState.layout ? state.passwordState.layout : 'col'),
  getClasses: computed(() => ({
    'inputDivError': state.passwordState.errorMessage
  })),
  getLabelClass: computed(() => {
    const defaultClass = 'min-w-[100px]';
    const labelClass = state.passwordState.labelClass || ''
    if (labelClass.includes('text-end')) {
      return `flex items-center justify-end gap-1 ${labelClass}`
    }
    return labelClass ? labelClass : defaultClass
  })
})
watchEffect(() => state.passwordState = props.passwordState)
</script>


<style lang="scss">
.p-password {
  @apply w-full;
}
.p-password-input {
  @apply pr-0 w-full overflow-hidden focus-within:shadow-none;
  &~svg {
    @apply -mr-2.5;
  }
}
</style>
