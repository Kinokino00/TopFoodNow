<template>
  <div
    v-if="state.dialogState.visible"
    class="dialog"
    :class="state.getDialogClasses"
  >
    <div class="dialog-mask"></div>
    <div class="dialog-container">
      <div v-if="state.dialogState.title" class="dialog-header">
        <p class="font-bold text-2xl">{{ state.dialogState.title }}</p>
        <ButtonComponent
          v-if="state.isCloseBtnShow"
          :buttonState="closeBtnState"
          @click="closeDialog"
          data-testid="close_dialog_button"
        />
      </div>
      <form
        v-if="$slots"
        @submit.prevent="state.dialogState.onSubmit"
        class="dialog-form"
      >
        <ScrollBar>
          <div
            class="dialog-scrollBar"
            :class="state.dialogState.scrollBarClass || 'max-h-[70vh]'"
          >
            <slot></slot>
          </div>
        </ScrollBar>
        <div class="dialog-footer">
          <ButtonComponent
            v-if="state.isCancelBtnShow"
            :buttonState="cancelBtnState"
            data-testid="cancel_dialog_button"
            @click="closeDialog"
          />
          <ButtonComponent
            v-if="state.isConfirmBtnShow"
            :buttonState="confirmBtnState"
            data-testid="confirm_dialog_button"
            @click="handleConfirmClick"
          />
        </div>
      </form>
    </div>
  </div>
</template>


<script lang="ts" setup>
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'
import ButtonComponent from '@/components/CustomButton.vue'
import { computed, reactive, defineExpose, watchEffect } from 'vue'

export type DialogState = {
  visible: boolean
  title: string
  scrollBarClass?: string  // 預設 'max-h-[70vh]'
  dialogClass?: string
  routerDialogTitle?: string
  routerDialogText?: string
  routerDialogSubText?: string
  routerLink?: string
  routerSubLink?: string
  routerQuery?: Object
  routerSubQuery?: Object
  isConfirmDialog?: boolean
  closeBtn?: boolean    // 預設 true
  cancelBtn?: boolean   // 預設 true
  confirmBtn?: boolean  // 預設 true
  confirmClick?: Function
  cancelClick?: Function
  confirmButtonType?: string  // 'button' | 'submit' | 'reset'
  onSubmit?: Function
}
const props = defineProps<{ dialogState: DialogState }>()
const state: any = reactive({
  dialogState: props.dialogState,
  getDialogClasses: computed(() => ({
    'show': state.dialogState.visible,
    'hidden': !state.dialogState.visible,
  })),
  isCloseBtnShow: computed(() => state.dialogState.closeBtn !== false),
  isCancelBtnShow: computed(() => state.dialogState.cancelBtn !== false),
  isConfirmBtnShow: computed(() => state.dialogState.confirmBtn !== false),
})
watchEffect(() => state.dialogState = props.dialogState)

const emits = defineEmits(['update:visible'])
const closeDialog = () => {
  if (typeof state.dialogState.cancelClick === 'function') {
    state.dialogState.cancelClick()
  } else {
    emits('update:visible', false)
  }
}
const handleConfirmClick = () => {
  if (state.dialogState.confirmClick) {
    state.dialogState.confirmClick()
  }
}

const closeBtnState = reactive({
  color: 'transparent',
  icon: ['fas', 'xmark'],
  iconClass: 'text-white',
})
const cancelBtnState = reactive({
  label: '取消',
  color: 'white',
  icon: ['fas', 'times'],
})
const confirmBtnState = reactive({
  label: '確認',
  color: 'primary',
  icon: ['fas', 'check'],
})

defineExpose({
  closeDialog
})
</script>


<style lang="scss" scoped>
.dialog {
  @apply z-[1000] fixed flex-col items-center justify-center left-0 top-0 w-screen h-screen;
  box-shadow: 0 1px 2px -1px rgba(0, 0, 0, .1), 0 4px 6px -1px rgba(0, 0, 0, .1);
  &.show {
    @apply opacity-100;
    animation: showOpacity .1s ease-in-out;
    @keyframes showOpacity {
      0%  { @apply opacity-0; }
      100%{ @apply opacity-100; }
    }
  }
  &.hidden {
    animation: hiddenOpacity .2s ease-in-out;
    @keyframes hiddenOpacity {
      0%  { @apply opacity-100; }
      100%{ @apply opacity-0; }
    }
  }

  &-mask {
    @apply w-full h-full bg-black/50;
    pointer-events: all !important;
  }
  &-container {
    @apply absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-white min-w-[45vw] max-h-[80vh] rounded-lg overflow-hidden;
  }
  &-form {
    @apply grid gap-3 w-full px-1 py-4;
  }
  &-scrollBar {
    @apply px-3 space-y-2 max-h-[50vh];
  }
  &-header {
    @apply flex items-center justify-between p-4 bg-primary-400 text-white;
  }
  &-footer {
    @apply flex items-center justify-center gap-4 pt-4;
  }
}
</style>
