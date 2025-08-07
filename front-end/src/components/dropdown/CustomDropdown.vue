<template>
  <div :class="['component-' + (props.dropdownState.layout ?? 'col'), props.dropdownState.width]">
    <p
      v-if="props.dropdownState.label"
      class="labelText"
      :class="props.dropdownState.labelClass ?? 'min-w-[100px]'"
    >
      {{ props.dropdownState.label }}
    </p>

    <div
      ref="inputDivRef"
      class="inputDiv"
      :class="[getClasses, props.dropdownState.inputDivClass]"
      @click="optionsState.openOptions"
    >
      <input
        class="input !w-[calc(100%-24px)] !text-xs text-ellipsis whitespace-nowrap overflow-hidden"
        type="text"
        :class="props.dropdownState.inputClass"
        :value="displayedValue"
        :placeholder="props.dropdownState.placeholder ?? '請選擇'"
        @input="handleInput"
        @change="props.dropdownState.inputChange"
        :disabled="props.dropdownState.disabled"
        :readonly="!props.dropdownState.editable"
      />
      <button class="button-arrow"></button>
    </div>
  </div>

  <Teleport to="body">
    <div
      v-if="optionsState.isShow"
      class="option-container"
      :style="{
        width: optionsState.width + 'px',
        left: optionsState.x + 'px',
        top: optionsState.y + 'px'
      }"
    >
      <input
        v-if="props.dropdownState.filter"
        class="filter-input"
        type="text"
        :class="props.dropdownState.filterClass"
        v-model="filterState.selectValue"
        :placeholder="props.dropdownState.filterPlaceholder"
      />
      <ScrollBar class="max-h-[160px]">
        <ul :class="{ 'mr-3': filterState.filteredOptions.length >= 4 }">
          <li
            v-for="option in filterState.filteredOptions"
            :key="option.value"
            :value="option.value"
            class="option-item"
            @click="selectOption(option)"
          >
            {{ option.name }}
          </li>
        </ul>
      </ScrollBar>
    </div>
  </Teleport>

  <div class="flex pl-2" v-if="props.dropdownState.errorMessage">
    <div
      v-if="props.dropdownState.layout === 'row' && props.dropdownState.label"
      class="mx-2 labelText"
      :class="props.dropdownState.labelClass ?? 'min-w-[100px]'"
    ></div>
    <p class="errorText">{{ props.dropdownState.errorMessage }}</p>
  </div>
</template>

<script lang="ts">
export class DropdownOption {
  name: string
  value: string | number
  constructor(name: any, value: any) {
    this.name = name
    this.value = value
  }
}
</script>

<script lang="ts" setup>
import { reactive, computed, watchEffect, ref, onMounted, onBeforeUnmount } from 'vue'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'

export type DropdownState = {
  modelValue: string | number | string[] | number[] | Object | boolean
  options: { name: string; value: string | number }[]
  layout?: string // 預設 'col'
  width?: string
  labelClass?: string // 預設 'min-w-[100px]'
  label?: string
  inputDivClass?: string
  inputClass?: string
  inputChange?: (event: Event) => void
  placeholder?: string // 預設 '請選擇'
  errorMessage?: string
  disabled?: boolean
  editable?: boolean
  readonly?: boolean
  dataTestId?: string
  filter?: boolean
  filterClass?: string
  filterPlaceholder?: string
}
const props = defineProps<{ dropdownState: DropdownState }>()
const inputDivRef = ref<HTMLElement | null>()
const emit = defineEmits(['update:modelValue'])

const displayedValue = computed(() => {
  if (!props.dropdownState.options) return ''
  const selectedOption = props.dropdownState.options.find(
    (option) => option.value === props.dropdownState.modelValue
  )
  return selectedOption ? selectedOption.name : ''
})

const getClasses = computed(() => ({
  '!bg-gray-100 !text-gray-500': props.dropdownState.disabled,
  inputDivError: props.dropdownState.errorMessage,
  inputDivReadonly: props.dropdownState.readonly
}))

const optionsState = reactive({
  isShow: false,
  width: 0,
  x: 0,
  y: 0,
  isUp: false,
  openOptions: () => (optionsState.isShow = !optionsState.isShow),
  closeOptions: () => (optionsState.isShow = false)
})

interface FilterState {
  selectValue: string
  filteredOptions: { name: string; value: string | number }[]
}

const filterState = reactive<FilterState>({
  selectValue: '',
  get filteredOptions() {
    if (!props.dropdownState.options) return []
    const filterValue = this.selectValue.toLowerCase()
    if (!filterValue) return props.dropdownState.options
    return props.dropdownState.options.filter((option) =>
      option.name.toLowerCase().includes(filterValue)
    )
  }
})

const selectOption = (option: DropdownOption) => {
  emit('update:modelValue', option.value)
  optionsState.closeOptions()
}

const calculatePosition = () => {
  const rect = inputDivRef.value?.getBoundingClientRect()
  if (!rect) return

  optionsState.width = Number(rect.width.toFixed(0))
  optionsState.x = Number(rect.x.toFixed(0))

  const viewportHeight = window.innerHeight
  const dropdownHeight = 40
  const optionContainerHeight = 160

  const spaceBelow = viewportHeight - rect.bottom

  if (spaceBelow < optionContainerHeight) {
    optionsState.isUp = true
    optionsState.y = Number(rect.top.toFixed(0)) + window.scrollY - optionContainerHeight + 56
  } else {
    optionsState.isUp = false
    optionsState.y = Number(rect.y.toFixed(0)) + window.scrollY + dropdownHeight - 1
  }
}

watchEffect(() => {
  if (optionsState.isShow && inputDivRef.value) {
    calculatePosition()
  }
})

const handleResize = () => {
  if (window.innerWidth < 768) optionsState.closeOptions()
  if (optionsState.isShow) calculatePosition()
}

const handleInput = (event: Event) => {
  if (props.dropdownState.editable) {
    props.dropdownState.modelValue = (event.target as HTMLInputElement).value
  }
}

watchEffect(() => {
  const rect = inputDivRef.value?.getBoundingClientRect()
  const dropdownHeight = 40
  if (rect) {
    optionsState.width = Number(rect.width.toFixed(0))
    optionsState.x = Number(rect.x.toFixed(0))
    optionsState.y = Number(rect.y.toFixed(0)) + window.scrollY + dropdownHeight - 1
  }
})

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  const optionContainer = target.closest('.option-container') && target instanceof HTMLElement
  if (inputDivRef.value && !inputDivRef.value?.contains(target) && !optionContainer) {
    optionsState.closeOptions()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('resize', handleResize)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/dropdownMultiSelect';
</style>
