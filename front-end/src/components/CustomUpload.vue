<template>
  <div
    class="inputUpload"
    :class="[
      'component-' + state.getLayout,
      state.uploadState.width
    ]"
  >
    <p
      v-if="state.uploadState.label"
      class="labelText"
      :class="state.getLabelClass"
    >
      {{ state.uploadState.label }}
    </p>
    <div class="inputUpload w-[80%]">
      <input
        ref="fileRef"
        type="file"
        class="inputFile"
        :accept="state.uploadState.acceptFileTypes"
        @change="state.uploadState.handleFileChange"
        :disabled="state.uploadState.disabled"
      />
      <div class="flex items-center" @click.prevent="state.handleButtonClick">
        <button type="button" class="uploadBtn">選擇檔案</button>
        <input
          :value="state.uploadState.modelValue"
          class="uploadFileName"
          placeholder="未選擇任何檔案"
          type="text"
          readonly
        />
      </div>
    </div>
    <!-- <a
      :href="state.uploadState.downloadUrl"
      download
      class="px-1.5 py-1 ml-2 bg-gray-500 text-white text-sm rounded-md whitespace-nowrap"
    >下載</a> -->
    <button
      type="button"
      class="px-1.5 py-1 ml-2 bg-gray-500 text-white text-sm rounded-md whitespace-nowrap"
      @click="state.uploadState.browseFolder"
    >選擇資料夾</button>
    <button
      type="button"
      class="px-1.5 py-1 ml-2 bg-gray-500 text-white text-sm rounded-md whitespace-nowrap"
      @click="state.uploadState.downloadToSelectedFolder"
    >下載到資料夾</button>
    <p v-if="state.uploadState.errorMessage" class="errorText">{{ state.uploadState.errorMessage }}</p>
    <p class="uploadFormat">{{ state.uploadState.uploadFormatTxt }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watchEffect } from 'vue'

export type UploadState = {
  modelValue: string
  layout?: string      // 預設 'row' 只有CustomUpload預設row排版
  width?: string
  labelClass?: string  // 預設 'min-w-[100px]'
  label?: string
  disabled?: boolean   // 預設 false
  errorMessage?: string
  uploadedFile?: File | null
  downloadUrl?: string
  handleFileChange?: (event: Event) => void
  folderHandle?: string
  downloadToSelectedFolder?: (event: Event) => void
  browseFolder?: (event: Event) => void
  acceptFileTypes?: string
  uploadFormatTxt?: string
}
const props = defineProps<{ uploadState: UploadState }>()
const state: any = reactive({
  uploadState: props.uploadState,
  getLayout: computed(() => state.uploadState.layout ? state.uploadState.layout : 'row'),
  getLabelClass: computed(() => ({
    [`${state.uploadState.labelClass}`]: props.uploadState.labelClass,
    'w-[100px]': state.getLayout === 'row',
    'w-full !ml-0': state.getLayout === 'col',
  })),
  handleButtonClick: () => fileRef.value?.click(), // 監聽input值變化(用戶選擇了一個新的檔案)時會調用
})
watchEffect(() => state.uploadState = props.uploadState)

const fileRef = ref<HTMLInputElement | null>(null)


</script>

<style lang="scss" scoped>
.inputUpload {
  @apply relative w-full;
  & > div:hover .uploadBtn {
    @apply bg-primary-500;
  }
  .uploadBtn,
  .uploadFileName {
    @apply z-10 h-10 p-2 text-sm leading-6;
  }
  .uploadBtn {
    @apply bg-primary-300 text-white whitespace-nowrap border border-transparent rounded-s-md;
  }
  .uploadFileName {
    @apply w-full text-gray-600 overflow-hidden border border-l-0 border-gray-200 rounded-e-lg cursor-pointer focus:outline-none placeholder:text-gray-300;
  }
  .uploadFormat {
    @apply text-xs text-gray-400;
  }
}

.inputFile {
  @apply absolute w-full h-10 opacity-0;

  &:disabled {
    ~ div {
      .uploadFileName,
      .uploadBtn {
        @apply text-gray-500 border-gray-200 cursor-no-drop focus:border-gray-200;
      }
      .uploadBtn {
        @apply bg-gray-100;
      }
    }
    ~ .uploadFormat {
      @apply text-gray-200;
    }
  }
}
</style>
