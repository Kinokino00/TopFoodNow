<template>
  <div>
    <div
      :class="[
        'component-' + state.getLayout,
        state.radioState.width
      ]"
    >
      <p
        v-if="state.radioState.label"
        class="labelText"
        :class="state.getLabelClass"
      >
        {{ state.radioState.label }}
      </p>
      <div class="flex flex-auto gap-3">
        <div
          v-for="item in state.radioState.value"
          :key="item.value"
          class="flex items-center space-x-2 py-1"
        >
          <label
            :htmlFor="state.radioState.id || state.radioState.name"
            class="checkboxRadio"
            :class="state.getRadioButtonClass"
          >
            <input
              type="radio"
              class="peer"
              :name="state.radioState.id || state.radioState.name"
              :id="state.radioState.id || state.radioState.name"
              :value="item.value"
              :disabled="state.radioState.disabled"
              :checked="state.radioState.modelValue === item.value"
              @change="emit('update:modelValue', item.value)"
            />
            <span class="peer-checked:!opacity-100">
              <svg viewBox="0 0 16 16">
                <circle data-name="ellipse" cx="8" cy="8" r="8" />
              </svg>
            </span>
          </label>
          <label
            :htmlFor="state.radioState.id || state.radioState.name"
            class="labelText leading-none cursor-pointer select-none !min-w-fit"
            :class="{ '!cursor-auto': state.radioState.disabledName === item.name }"
          >
            {{ item.name }}
          </label>
        </div>
      </div>
    </div>
    <div
      v-if="state.radioState.errorMessage"
      class="flex"
      :class="{ 'pl-2': state.radioState.layout === 'row' && state.radioState.label }"
    >
      <div
        v-if="state.radioState.layout === 'row' && state.radioState.label"
        class="ml-2"
        :class="state.getLabelClass"
      ></div>
      <p class="errorText !pl-0">{{ state.radioState.errorMessage }}</p>
    </div>
  </div>
</template>


<script lang="ts" setup>
import { computed, reactive, watchEffect } from 'vue'

export type RadioValue = String | Number | Boolean | null
export type RadioState = {
  modelValue: RadioValue
  value: { name: string; value: RadioValue }[]
  id?: string          // 預設 'radioButton'
  width?: string
  layout?: string      // 預設 'row' 本專案沒有col的情況，radioButton多用在popup
  label?: string
  labelClass?: string  // 預設 'w-[100px]'
  disabled?: boolean
  disabledName?: string
  errorMessage?: string
}
const props = defineProps<{ radioState: RadioState }>()
const state: any = reactive({
  radioState: props.radioState,
  getLayout: computed(() => state.radioState.layout || 'row'),
  getLabelClass: computed(() => ({
    [`${state.radioState.labelClass}`]: state.radioState.labelClass,
    'w-[100px]': state.getLayout === 'row',
    'w-full !ml-0': state.getLayout === 'col'
  })),
  getRadioButtonClass: computed(() => ({
    'checkboxRadioError': state.radioState.errorMessage
  }))
})
watchEffect(() => state.radioState = props.radioState)

const emit = defineEmits(['update:modelValue'])
</script>


<style lang="scss" scoped>
@import '@/assets/style/checkboxRadio.scss';
</style>
