<template>
  <div>
    <div class="datePicker" :class="state.getClasses">
      <p
        v-if="state.datePickerState.label"
        class="labelText"
        :class="state.getLabelClass"
      >
        {{ state.datePickerState.label }}
      </p>
      <date-picker
        :value="state.datePickerState.modelValue"
        :type="state.getType"
        :range="state.datePickerState.type === 'range'"
        :placeholder="state.datePickerState.placeholder"
        :disabled="state.datePickerState.disabled"
        :disabledDate="state.datePickerState.disabledDate"
        :format="state.getFormat"
        :formatter="state.getFormatter"
        :lang="state.lang"
        :editable="state.datePickerState.editable"
        @update:value="emit('update:state.datePickerState.modelValue', $event)"
      />
    </div>
    <div class="flex pl-2" v-if="state.datePickerState.errorMessage">
      <div
        v-if="
          state.datePickerState.layout === 'row' &&
          state.datePickerState.label
        "
        class="mx-2 labelText"
        :class="state.datePickerState.labelClass"
      ></div>
      <p class="errorText">{{ state.datePickerState.errorMessage }}</p>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive, computed, watchEffect } from 'vue'
import DatePicker from 'vue-datepicker-next'
import * as dateHelper from '@/utils/dateHelper'


export type DatePickerState = {
  modelValue: string | Date
  type: string        // 'date' | 'time' | 'week' | 'season' | 'shortDate'
  layout?: string     // 預設 'col'
  width?: string
  labelClass?: string  // 預設 'min-w-[100px]'
  label?: string
  placeholder?: string
  error?: string
  errorMessage?: string
  disabled?: boolean   // 預設 false
  disabledDate?: Function
  editable?: boolean   // 預設 true
}
const props = defineProps<{ datePickerState: DatePickerState }>()

const state: any = reactive({
  datePickerState: props.datePickerState,
  getLayout: computed(() => state.datePickerState.layout ? state.datePickerState.layout : 'col'),
  getClasses: computed(() => ({
    [`component-${state.getLayout}`]: state.datePickerState.layout,
    [`${state.datePickerState.width}`]: state.datePickerState.width,
    'datePicker-time': state.datePickerState.type === 'time',
    datePickerError: state.datePickerState.errorMessage || state.datePickerState.error
  })),
  getType: computed(() =>
    state.datePickerState.type === 'season' ? 'month' : state.datePickerState.type === 'shortDate' ? 'date' : state.datePickerState.type
  ),
  lang: {
    formatLocale: { firstDayOfWeek: 1 },
    monthBeforeYear: false
  },
  // https://github.com/mengxiong10/vue-datepicker-next/blob/main/README.zh-CN.md
  weekFormat: { stringify: dateHelper.getWeekStartAndEndDisplay },
  singleSeasonFormat: { stringify: dateHelper.getSingleSeasonDisplay },
  getFormat: computed(() =>
    state.datePickerState.type === 'date'
      ? 'YYYY-MM-DD'
      : state.datePickerState.type === 'time'
        ? 'HH:mm'
        : state.datePickerState.type === 'shortDate'
          ? 'MM-DD'
          : ''
  ),
  getFormatter: computed(() =>
    state.datePickerState.type === 'week' ? state.weekFormat : state.datePickerState.type === 'season' ? state.singleSeasonFormat : ''
  ),
})
watchEffect(() => state.datePickerState = props.datePickerState)

const emit = defineEmits(['update:state.datePickerState.modelValue'])
// https://www.npmjs.com/package/vue-datepicker-next?activeTab=readme
</script>

<style lang="scss">
.datePicker {
  &, .mx-input-wrapper:hover {
    i {
      @apply hidden;
    }
  }
  .mx-datepicker {
    @apply w-full relative;
    &::after {
      @apply content-['\f133'] absolute top-1/2 right-3 -translate-y-1/2 font-fontAwesome font-black text-xs text-gray-600 leading-4;
    }
  }
  &-time .mx-datepicker::after {
    @apply content-['\f017'] font-fontAwesome;
  }
  &Error .mx-input {
    &, &:hover {
      @apply border-danger-500;
    }
  }
  .mx-input {
    @apply h-10 px-3 py-2 text-sm font-normal rounded-lg shadow-none;
    &:disabled {
      @apply bg-gray-50 cursor-auto;
      &, &::placeholder {
        @apply text-gray-500;
      }
    }
  }
}
.mx-datepicker-main.mx-datepicker-popup {
  @apply border-none rounded-b-md;
  box-shadow: 0 4px 6px -3px rgba(0, 0, 0, .1), 0 10px 15px -4px rgba(0, 0, 0, .1);

  .mx-table-date thead th {
    @apply bg-white;
  }
  .cell, .mx-time-item { // 一般與time picker
    &:hover {
      @apply text-primary-500 bg-primary-50 rounded-md;
    }
    &.active {
      @apply text-white bg-primary-500 rounded-md;
    }
  }
  .disabled, .disabled.not-current-month {
    &, &:hover {
      @apply bg-gray-100/60 text-gray-300 rounded-none;
    }
  }
  .mx-time-item { // time picker
    &:hover, &.active {
      @apply mx-1;
    }
  }
  .mx-date-row {
    .not-current-month {
      @apply text-gray-300;
    }
    &.mx-active-week {
      @apply rounded-md bg-primary-100;
      .mx-week-number {
        @apply rounded-l-md;
      }
      .cell:nth-last-of-type(1) {
        @apply rounded-r-md;
      }
    }
  }
}
</style>
