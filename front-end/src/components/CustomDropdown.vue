<template>
  <div
    :class="[
      'component-' + (state.dropdownState.layout ?? 'col'),
      state.dropdownState.width
    ]"
  >
    <p
      v-if="state.dropdownState.label"
      class="labelText"
      :class="state.dropdownState.labelClass ?? 'min-w-[100px]'"
    >
      {{ state.dropdownState.label }}
    </p>

    <div
      ref="inputDivRef"
      class="inputDiv"
      :class="[ state.getClasses, state.dropdownState.inputDivClass ]"
      @click="state.optionsState.openOptions"
    >
      <input
        class="input textMoreOneLine !w-[calc(100%-24px)]"
        type="text"
        :class="state.dropdownState.inputClass"
        v-model="state.dropdownState.modelValue"
        :placeholder="state.dropdownState.placeholder ?? '請選擇'"
        @input="inputEmits"
        @change="state.dropdownState.inputChange"
        :disabled="state.dropdownState.disabled"
        :readonly="!state.dropdownState.editable"
      />
      <button class="button-arrow"></button>
    </div>
  </div>

  <Teleport to="body">
    <div
      v-if="state.optionsState.isShow"
      class="option-container"
      :style="{
        width: state.optionsState.width + 'px',
        left: state.optionsState.x + 'px',
        top: state.optionsState.y + 'px'
      }"
    >
      <div class="filter-container">
        <input
          class="filter-input"
          type="text"
          :class="state.dropdownState.filterClass"
          v-model="state.filterState.selectValue"
          :placeholder="state.dropdownState.filterPlaceholder ?? '請輸入'"
        />
      </div>
      <ScrollBar class="max-h-[160px]">
        <ul :class="{ 'mr-3': state.filterState.filteredOptions.length >= 4 }">
          <li
            v-for="option in state.filterState.filteredOptions"
            :key="option.value"
            :value="option.value"
            class="option-item"
            @click="state.optionsState.clickOption(option)"
          >{{ option.name }}</li>
        </ul>
      </ScrollBar>
    </div>
  </Teleport>

  <div class="flex pl-2" v-if="state.dropdownState.errorMessage">
    <div
      v-if="state.dropdownState.layout === 'row' && state.dropdownState.label"
      class="mx-2 labelText"
      :class="state.getLabelClass"
    ></div>
    <p class="errorText">{{ state.dropdownState.errorMessage }}</p>
  </div>
</template>

<script lang="ts">
export class DropdownOption {
  name: string;
  value: string | number;
  constructor(name: any, value: any) {
    this.name = name;
    this.value = value;
  }
}
</script>

<script lang="ts" setup>
import { reactive, computed, watchEffect, ref, onMounted, onBeforeUnmount } from 'vue'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'

export type DropdownState = {
  modelValue: string | number | string[] | number[] | Object | boolean
  options: { name: string, value: string | number }[]
  layout?: string        // 預設 'col'
  width?: string
  labelClass?: string    // 預設 'min-w-[100px]'
  label?: string
  inputDivClass?: string
  inputClass?: string
  inputChange?: (event: Event) => void
  placeholder?: string   // 預設 '請選擇'
  errorMessage?: string
  disabled?: boolean
  editable?: boolean
  readonly?: boolean
  dataTestId?: string
  filterClass?: string
  filterPlaceholder?: string
}
const props = defineProps<{ dropdownState: DropdownState }>()
const inputDivRef = ref<HTMLElement | null>()
const state: any = reactive({
  dropdownState: props.dropdownState,
  getClasses: computed(() => ({
    '!bg-gray-50 !text-gray-500': state.dropdownState.disabled,
    'inputDivError': state.dropdownState.errorMessage,
    'inputDivReadonly': state.dropdownState.readonly
  })),
  /* 選項們的狀態 */
  optionsState: {
    isShow: false,
    width: 0,
    x: 0,
    y: 0,
    clickOption: (o: DropdownOption) => {
      state.dropdownState.modelValue = o.name
      state.optionsState.closeOptions()
    },
    openOptions: () => state.optionsState.isShow = !state.optionsState.isShow,
    closeOptions: () => state.optionsState.isShow = false
  },
  /* 篩選 */
  filterState: {
    selectValue: '',
    filteredOptions: computed(() => {
      const filterValue = state.filterState.selectValue.toLowerCase()
      if (!filterValue) return state.dropdownState.options
      return state.dropdownState.options.filter(
        (option: DropdownOption) => option.name.toLowerCase().includes(filterValue)
      )
    })
  }
})

watchEffect(() => state.dropdownState = props.dropdownState)
watchEffect(() => {
  const rect = inputDivRef.value?.getBoundingClientRect()
  const dropdownHeight = 40
  if (rect) {
    const s = state.optionsState
    s.width = Number(rect.width.toFixed(0))
    s.x = Number(rect.x.toFixed(0))
    s.y = Number(rect.y.toFixed(0)) + s.y + dropdownHeight - 1
  }
})

const inputEmits = (event: Event) => {
  const inputVal = (event.target as HTMLInputElement)?.value
  emit('update:modelValue', inputVal)
}
const emit = defineEmits(['update:modelValue'])

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  const optionContainer = target.closest('.option-container') && target instanceof HTMLElement
  if (inputDivRef.value && !inputDivRef.value?.contains(target) && !optionContainer) {
    state.optionsState.closeOptions()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style lang="scss" scoped>
@import '@/assets/style/dropdownMultiSelect.scss';
</style>