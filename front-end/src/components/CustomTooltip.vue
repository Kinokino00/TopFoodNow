<template>
  <div
    ref="tooltipHoverRef"
    class="tooltip-hover"
    @mouseenter="state.tooltipHover = true"
    @mouseleave="state.tooltipHover = false"
  >
    <p class="labelAllClass" :class="state.tooltipState.labelAllClass">
      <span :class="state.tooltipState.labelClass">
        <div
          v-if="state.tooltipState.iconClass"
          class="icon"
          :class="state.tooltipState.iconClass"
        ></div>
        {{ state.tooltipState.label }}
      </span>
      <b v-if="state.tooltipState.labelBold">
        {{ state.tooltipState.labelBold }}
      </b>
      <span
        v-if="state.tooltipState.subLabelClass"
        :class="state.tooltipState.subLabelClass"
      >
        {{ state.tooltipState.subLabel }}
      </span>
    </p>
    <Teleport to="body">
      <div
        v-if="state.tooltipState.tooltipLabel && state.tooltipHover"
        class="tooltip"
        :class="[
          state.tooltipState.tooltipClass,
          { 'showTop': state.tooltipState.showTop }
        ]"
        :style="{
          left: state.tooltipX + 'px',
          top: state.tooltipY + 'px'
        }"
      >
        <p>{{ state.tooltipState.tooltipLabel }}
          <span v-if="state.tooltipState.tooltipSubLabel">
            <br>{{ state.tooltipState.tooltipSubLabel }}
          </span>
          <br v-if="$slots.labelBottom">
          <slot name="labelBottom"></slot>
        </p>
      </div>
    </Teleport>
  </div>
</template>


<script lang="ts" setup>
import { reactive, ref, watchEffect } from 'vue'

export type TooltipState = {
  tooltipLabel: string
  iconClass?: string
  label: string
  labelBold?: string
  subLabel?: string
  labelAllClass?: string
  labelClass?: string
  subLabelClass?: string
  tooltipClass?: string
  tooltipSubLabel?: string
  tooltipX?: number
  tooltipY?: number
  showTop?: boolean
}
const props = defineProps<{ tooltipState: TooltipState }>()

const tooltipHoverRef = ref<HTMLElement | null>()
const state = reactive({
  tooltipState: props.tooltipState,
  tooltipHover: false,
  tooltipWidth: 0,
  tooltipX: 0,
  tooltipY: 0,
})
watchEffect(() => state.tooltipState = props.tooltipState)
watchEffect(() => {
  const rect = tooltipHoverRef.value?.getBoundingClientRect()
  if (state.tooltipHover && rect) {
    state.tooltipWidth = Number(rect.width.toFixed(0))
    state.tooltipX = Number(rect.x.toFixed(0)) + state.tooltipWidth / 2 + state.tooltipX
    state.tooltipY = Number(rect.y.toFixed(0)) + state.tooltipY
  }
})
</script>


<style lang="scss" scoped>
.tooltip {
  @apply absolute z-[5000] mt-8 -ml-1.5 -translate-x-1/2 w-max text-left whitespace-nowrap;

  &:after {
    @apply content-[''] absolute w-0 h-0 -top-4 left-1/2 border-8 border-b-gray-600 border-transparent;
  }
  &.showTop {
    @apply -mt-[68px];
    &:after {
      @apply top-auto -rotate-180;
    }
  }
  p {
    @apply bg-gray-600 text-white text-sm rounded-lg p-2.5;
  }

  &-hover {
    @apply relative cursor-pointer w-fit m-auto;
    .icon {
      @apply relative;
      &:after {
        @apply content-['\f05a'] absolute top-1/2 left-1/2 -translate-y-1/2 font-fontAwesome font-black text-xs text-gray-500 leading-4;
      }
    }
  }
}
</style>
