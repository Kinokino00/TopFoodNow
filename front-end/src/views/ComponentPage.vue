<template>
  <!-- <YoutubePlayer src="https://www.youtube.com/watch?v=uRzs2kS3Blg" /> -->
  <div class="p-3">
    <CustomButton
      :buttonState="buttonState"
      @click="dialogState.isDialogShow"
    />
    <CustomDropdown :dropdownState="dropdownState"/>
    <CustomMultiSelect :multiSelectState="multiSelectState"/>
    <CustomInputText :inputTextState="inputTextState"/>
    <CustomInputNumber :inputNumberState="inputNumberState"/>
    <CustomPassword :passwordState="passwordState"/>
    <DatePickerComponent :datePickerState="datePickerState"/>
    <CustomTextarea :textareaState="textareaState"/>
    <CustomSwitch :switchState="switchState"/>
    <CustomUpload :uploadState="uploadState"/>
    <CustomCheckbox :checkboxState="checkboxState"/>
    <CustomRadio :radioState="radioState"/>
    <CustomTooltip :tooltipState="tooltipState"/>
    <CustomToast :toastState="toastState"/>
    
    <CustomDialog :dialogState="dialogState">
      <CustomPassword
        v-for="(_, i) in 10" :key="i"
        :passwordState="passwordState2"
      />
    </CustomDialog>
  </div>
</template>


<script lang="ts" setup>
import { reactive } from 'vue'
// import YoutubePlayer from '@/components/YoutubePlayer.vue'
import CustomButton from '@/components/CustomButton.vue'
import CustomDropdown from '@/components/CustomDropdown.vue'
import CustomMultiSelect from '@/components/CustomMultiSelect.vue'
import CustomInputText from '@/components/CustomInputText.vue'
import CustomInputNumber from '@/components/CustomInputNumber.vue'
import CustomPassword from '@/components/CustomPassword.vue'
import DatePickerComponent from '@/components/DatePickerComponent.vue'
import CustomSwitch from '@/components/CustomSwitch.vue'
import CustomTextarea from '@/components/CustomTextarea.vue'
import CustomUpload from '@/components/CustomUpload.vue'
import CustomCheckbox from '@/components/CustomCheckbox.vue'
import CustomRadio from '@/components/CustomRadio.vue'
import CustomTooltip from '@/components/CustomTooltip.vue'
import CustomToast from '@/components/CustomToast.vue'
import CustomDialog from '@/components/CustomDialog.vue'

const buttonState = reactive({
  label: 'Show Dialog',
})
const dropdownState = reactive({
  label: 'dropdown',
  options: [
    { name: '選項1', value: '1', checked: true },
    { name: '選項22', value: '2', checked: true },
    { name: '選項333', value: '3', checked: false },
    { name: '選項4', value: '4', checked: false },
    { name: '選項5', value: '5', checked: false },
  ],
  width: 'w-[220px]',
})
const multiSelectState = reactive({
  label: 'multiSelect',
  options: [
    { name: '選項1', value: '1', checked: true },
    { name: '選項22', value: '2', checked: true },
    { name: '選項333', value: '3', checked: false },
    { name: '選項4', value: '4', checked: false },
    { name: '選項5', value: '5', checked: false },
  ],
  hasAllOption: true,
  maxOption: 5,
  width: 'w-[220px]',
})
const inputTextState = reactive({
  modelValue: '文字內容',
  label: 'inputText',
})
const inputNumberState = reactive({
  modelValue: 11,
  label: 'inputNumber',
})
const passwordState = reactive({
  modelValue: 'TEST',
  label: 'password',
  labelClass: 'min-w-[120px]',
})
const datePickerState = reactive({
  modelValue: new Date(),
  label: '日期',
  type: 'season',
})
const textareaState = reactive({
  modelValue: '',
  label: 'textarea',
  placeholder: '請輸入'
})
const switchState = reactive({
  modelValue: false,
  disabled: true,
  isError: true
})
const uploadState = reactive({
  modelValue: '',
  label: '選擇匯入檔案',
  uploadedFile: null as File | null,
  downloadUrl: '',
  folderHandle: undefined as string | undefined,
  handleFileChange: (event: Event) => {
    const target = event.target as HTMLInputElement
    uploadState.modelValue = target.files ? target.files[0].name : ''
    uploadState.uploadedFile = target.files ? target.files[0] : null
    uploadState.downloadUrl = target.files ? URL.createObjectURL(target.files[0]) : ''
  },
  browseFolder: async () => {
    try {
      const dirHandle = await (window as any).showDirectoryPicker() // 現代瀏覽器 API
      uploadState.folderHandle = dirHandle
    } catch (err) {
      console.error('資料夾選擇失敗:', err)
    }
  },
  downloadToSelectedFolder: async () => {
    if (!uploadState.folderHandle || !uploadState.uploadedFile) {
      alert('請先選擇檔案和資料夾')
      return
    }

    try {
      const fileHandle = await uploadState.folderHandle.getFileHandle(uploadState.modelValue, {
        create: true,
      })
      const writable = await fileHandle.createWritable()
      const blob = await fetch(uploadState.downloadUrl).then((res) => res.blob())
      await writable.write(blob)
      await writable.close()
      alert('檔案已成功下載到選擇的資料夾')
    } catch (err) {
      console.error('下載到資料夾失敗:', err)
    }
  }
})
const checkboxState = reactive({
  modelValue: true,
  name: 'checkbox',
  id: 'checkbox2', //不傳 預設name
  isError: true
})
const radioState = reactive({
  modelValue: 1,
  value: [
    { name: 'radio1', value: 1 },
    { name: 'radio2', value: 2 },
    { name: 'radio3', value: 3 },
  ],
})
const tooltipState = reactive({
  label: '當前方案',
  labelBold: '實際',
  subLabel: '用量',
  tooltipLabel: "currentElectricityPrice"
})
const toastState = reactive({
  visible: false,
  color: 'success',
  label: '成功',
})
const dialogState = reactive({
  isDialogShow: () => dialogState.visible = !dialogState.visible,
  visible: false,
  title: '標題',
  cancelClick: () => dialogState.visible = false,
  confirmClick: () => dialogState.visible = false,
})
const passwordState2 = reactive({
  modelValue: 'TEST',
  label: '請輸入密碼：',
  labelClass: passwordState.labelClass,
  layout: 'row'
})
</script>
