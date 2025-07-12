<template>
  <div
    :class="[
      'component-' + (state.multiSelectState.layout ?? 'col'),
      state.multiSelectState.width
    ]"
  >
    <p
      v-if="state.multiSelectState.label"
      class="labelText"
      :class="state.multiSelectState.labelClass ?? 'min-w-[100px]'"
    >{{ state.multiSelectState.label }}</p>

    <div
      ref="inputDivRef"
      class="inputDiv"
      :class="[ state.getClasses, state.multiSelectState.inputDivClass ]"
      @click="state.optionsState.openOptions"
    >
      <div class="selectedItems" ref="selectedItemsRef">
        <div v-for="item in state.multiSelectState.modelValue" :key="item" class="selectedItem">
          {{ item.name }}
          <font-awesome-icon :icon="['far', 'circle-xmark']" @click="state.removeSelectedItem(item)"/>
        </div>
      </div>
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
          :class="state.multiSelectState.filterClass"
          v-model="state.filterState.selectValue"
          :placeholder="state.multiSelectState.filterPlaceholder ?? '請輸入'"
        />
        <button class="button-cancel" @click="state.filterState.selectValue = ''">
          <font-awesome-icon :icon="['fas', 'xmark']" />
        </button>
      </div>
      <ScrollBar class="max-h-[160px]">
        <ul :class="{ 'mr-3': state.filterState.filteredOptions.length >= 4 }">
          <li
            v-if="
              state.multiSelectState.hasAllOption &&
              state.filterState.selectValue == ''
            "
            class="option-item"
          >
            <CustomCheckbox
              v-model="state.allOption.modelValue"
              :checkboxState="state.allOption"
              @click="state.checkAllOption"
            />
          </li>
          <li
            v-for="option in state.filterState.filteredOptions"
            :key="option.value"
            :value="option.value"
            class="option-item"
          >
            <CustomCheckbox
              v-model="option.checked"
              :checkboxState="{
                modelValue: option.checked,
                name: option.name,
                id: option.value,
                labelTextClass: '!w-full',
                disabled:
                  state.multiSelectState.maxOption &&
                  state.multiSelectState.maxOption <= state.multiSelectState.modelValue.length
              }"
            />
          </li>
        </ul>
      </ScrollBar>
    </div>
  </Teleport>
  <div class="flex pl-2" v-if="state.multiSelectState.errorMessage">
    <div
      v-if="state.multiSelectState.layout === 'row' && state.multiSelectState.label"
      class="mx-2 labelText"
      :class="state.getLabelClass"
    ></div>
    <p class="errorText">{{ state.multiSelectState.errorMessage }}</p>
  </div>
</template>


<script setup lang="ts">
import { reactive, computed, watchEffect, ref, onMounted, watch, nextTick, onBeforeUnmount } from 'vue'
import CustomCheckbox from '@/components/CustomCheckbox.vue'
import { DropdownOption } from '@/components/CustomDropdown.vue'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'

class MultiSelectOptionChecked extends DropdownOption {
  checked
  constructor(name: any, value: any, checked: any) {
    super(name, value)
    this.checked = checked
  }
}
export type MultiSelectState = {
  modelValue: string | number | string[] | number[] | Object | boolean
  options: { name: string, value: string | number }[]
  hasAllOption?: boolean // 預設 false
  maxOption?: number     // 預設 5
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
const props = defineProps<{ multiSelectState: MultiSelectState }>()
const inputDivRef = ref<HTMLElement | null>()
const state: any = reactive({
  multiSelectState: props.multiSelectState,
  getClasses: computed(() => ({
    '!bg-gray-50 !text-gray-500': state.multiSelectState.disabled,
    'inputDivError': state.multiSelectState.errorMessage,
    'inputDivReadonly': state.multiSelectState.readonly
  })),
  /* 選項們的狀態 */
  optionsState: {
    isShow: false,
    width: 0,
    x: 0,
    y: 0,
    openOptions: () => state.optionsState.isShow = !state.optionsState.isShow,
    closeOptions: () => state.optionsState.isShow = false
  },
  /* 篩選 */
  filterState: {
    selectValue: '',
    filteredOptions: computed(() => {
      const filterValue = state.filterState.selectValue.toLowerCase()
      if (!filterValue) return state.multiSelectState.options
      return state.multiSelectState.options.filter(
        (option: MultiSelectOptionChecked) => option.name.toLowerCase().includes(filterValue)
      )
    })
  },
  /* 全選 */
  allOption: {
    modelValue: false,
    name: '全部',
    id: 'all',
    labelTextClass: '!w-full'
  },
  checkAllOption: () => {
    const isChecked = state.allOption.modelValue
    state.multiSelectState.options.forEach(
      (option: MultiSelectOptionChecked) => option.checked = !isChecked
    )
  },
  removeSelectedItem: (item: string) => {
    const option = state.multiSelectState.options.find(
      (option: MultiSelectOptionChecked) => option.name === item
    )
    if (option) option.checked = false
    const index = state.multiSelectState.modelValue.indexOf(item)
    if (index > -1) state.multiSelectState.modelValue.splice(index, 1)
    getSelectedItem()
  }
})

const selectedItemsRef = ref<HTMLDivElement | null>()
const calculateSelectedItems = (childItems: HTMLDivElement[]) => {
  const buttonWidth = 28
  const padding = 8
  const dropdownWidth = state.multiSelectState.width?.replace('w-[', '').replace('px]', '')
  if (!dropdownWidth) return
  const contentWidth = Math.ceil(Number(dropdownWidth) - buttonWidth - padding * 3)
  let currentWidth = 0
  let lastVisibleIndex = -1
  
  childItems.forEach((item, index) => {
    if (currentWidth + item.clientWidth + padding <= contentWidth) {
      currentWidth += item.clientWidth + padding
      item.style.display = ''
      lastVisibleIndex = index
    }
  })

  if (lastVisibleIndex !== -1) {
    for (let i = lastVisibleIndex + 1; i < childItems.length; i++) {
      console.log('lastVisibleIndex', lastVisibleIndex)
      console.log('i',i)
      childItems[i].style.display = 'none'
      const parent = selectedItemsRef.value
      if (parent) {
        const existingEllipsis = parent.querySelector('.ellipsis')
        if (existingEllipsis) existingEllipsis.remove()
        const ellipsisDiv = document.createElement('div')
        ellipsisDiv.textContent = '...'
        ellipsisDiv.classList.add('ellipsis')
        if (childItems.length > 0) parent.appendChild(ellipsisDiv)
      }
    }
  }
}
const getSelectedItem = async () => {
  await nextTick()
  const childItems = Array.from(selectedItemsRef.value?.children ?? []) as HTMLDivElement[]
  if (childItems) calculateSelectedItems(childItems)
}
watchEffect(() => state.multiSelectState = props.multiSelectState)
watch(
  () => state.multiSelectState.modelValue,
  () => getSelectedItem()
)
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
watchEffect(() => {
  state.multiSelectState.modelValue = state.multiSelectState.options
    .filter((o: MultiSelectOptionChecked) => o.checked)
})

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
  getSelectedItem()
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>


<style lang="scss" scoped>
@import '@/assets/style/dropdownMultiSelect.scss';

.button-cancel {
  @apply absolute px-2 top-1/2 right-1 text-base -translate-y-1/2;
}
.selectedItem {
  &s {
    @apply flex items-center gap-1 w-[calc(100%-28px)] text-sm whitespace-nowrap overflow-hidden;
  }
  @apply px-2 py-0.5 bg-gray-50 rounded-lg;
}
</style>

<style lang="scss">
.option-item:hover .labelText {
  @apply text-white;
}
</style>
