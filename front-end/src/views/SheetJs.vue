<template>
  <div class="p-3">
    <input type="file" @change="handleFile" />
    <button class="px-2 py-1 border rounded" @click="processFile">上傳</button>
    <!-- <ScrollBar class="p-3 max-h-screen"></ScrollBar> -->
  </div>
</template>


<script lang="ts" setup>
import { ref } from 'vue'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'
import * as XLSX from 'xlsx'

const fileData = ref<ArrayBuffer | null>(null)
const excelData = ref()
const table1 = ref()
const table2 = ref()
const table3 = ref()
const table4 = ref()
const table5 = ref()
const table6 = ref()
const table7 = ref()
const table8_1 = ref()

const handleFile = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => fileData.value = e.target?.result as ArrayBuffer
  reader.readAsArrayBuffer(file)
}

const processFile = () => {
  if (!fileData.value) return
  const data = new Uint8Array(fileData.value)
  const workbook = XLSX.read(data, { type: 'array' })
  excelData.value = workbook.SheetNames.map(sheetName => {
    const worksheet = workbook.Sheets[sheetName]
    const sheetData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
    const filteredData = (sheetData as any[][]).map(row => 
      row.map(cell => typeof cell === 'string' ? cell.replace(/[\r\n]/g, '').trim() : cell)
    )
    return { name: sheetName, data: filteredData }
  })
  console.log('excelData.value', excelData.value)

  if (excelData.value.length > 0) {
    table1.value = organizeTable1(excelData.value[0].data)
    table2.value = organizeTable2(excelData.value[1].data)
    table3.value = organizeTable3(excelData.value[2].data)
    table4.value = organizeTable4(excelData.value[3].data)
    table5.value = organizeTable5(excelData.value[4].data)
    table6.value = organizeTable6(excelData.value[5].data)
    table7.value = organizeTable7(excelData.value[6].data)
    table8_1.value = organizeTable8_1(excelData.value[7].data)
    console.log('table8_1.value', table8_1.value)
  }
}

function organizeTable1(data: any) {
  if (!Array.isArray(data)) return
  const headers = data[2]
  const subHeaders = data[3]
  const dataRows = data.slice(4)

  const headerMap: { [key: string]: number } = {}
  headers.forEach((header: string, index: number) => {
    if (header) headerMap[header] = index
  })
  subHeaders.forEach((subHeader: string, index: number) => {
    if (subHeader) headerMap[subHeader] = index
  })

  return dataRows.map((row: any) => ({
    inventoryYear: { year: row[0] + row[1] },
    basicInformation: {
      controlNumber: row[headerMap['管制編號']],
      publicPrivateName: row[headerMap['公私場所名稱']],
      taxID: row[headerMap['統一編號']],
      factoryRegistrationCertificate: row[headerMap['工廠登記證編號']],
      countyAndCity: row[headerMap['縣市別']],
      township: row[headerMap['鄉鎮別']],
      postalCode: row[headerMap['郵遞區號']],
      village: row[headerMap['里別']] || null,
      neighborhood: row[headerMap['鄰別']] || null,
      address: row[headerMap['地址']],
      employees: row[headerMap['員工人數']],
      chargeName: row[headerMap['負責人姓名']],
      publicPrivateEmail: row[headerMap['公私場所電子信箱(忘記密碼寄發信箱)']],
      contactInformation: {
        name: row[headerMap['姓名']],
        telephone: row[headerMap['電話']],
        email: row[headerMap['電子信箱']],
        fax: row[headerMap['傳真']],
        callPhone: row[headerMap['手機']],
      },
      industryClassification: {
        industryCode1: row[headerMap['行業代碼1']] || null,
        industryName1: row[headerMap['行業名稱1']] || null,
        industryCode2: row[headerMap['行業代碼2']] || null,
        industryName2: row[headerMap['行業名稱2']] || null,
      }
    },
    investigationInfo: {
      registrationReason: row[headerMap['登錄原因']] || null,
      investigationBasis: row[headerMap['盤查依據規範']] || null,
      thirdPartyVerification: row[headerMap['是否經第三者查證']] === "是",
      verificationInstitution: row[headerMap['查驗機構名稱']] || null,
    },
    thresholdSetting: {
      significanceThreshold: row[headerMap['顯著性門檻']],
      materialityThreshold: row[headerMap['實質性門檻']],
      exclusionThreshold: row[headerMap['排除門檻']],
    },
    note: row[headerMap['備註']] || null
  }))
}

function organizeTable2(data: any) {
  if (!Array.isArray(data)) return
  const formattedData = []
  let currentBuilding = null

  for (let i = 1; i < data.length; i++) {
    const row = data[i]
    if (!row[2]) row[2] = "無"
    if (!row[1]) {
      row[0] = data[i - 1][0]
      row[1] = data[i - 1][1]
    } else if (row[1] === "場址外涵蓋區域*") {
      currentBuilding = {
        building: row[4] || "",
        coveredArea: { position: row[2], name: row[3] },
        coveredArea2: { position: "", name: "" },
        deductionArea: { position: "", name: "" },
        deductionArea2: { position: "", name: "" },
        settingMethod: ""
      }
      formattedData.push(currentBuilding)
    } else if (row[1] === "設定方法" && currentBuilding) {
      currentBuilding.settingMethod = row[2]
    }
    if (row[1] === "場址內扣除區域*" && currentBuilding) {
      if (!currentBuilding.deductionArea.name) {
        currentBuilding.deductionArea.position = row[2]
        currentBuilding.deductionArea.name = row[3]
      } else {
        currentBuilding.deductionArea2.position = row[2]
        currentBuilding.deductionArea2.name = row[3]
      }
    }
    if (row[1] === "場址外涵蓋區域*" && currentBuilding) {
      currentBuilding.coveredArea2.position = row[2]
      currentBuilding.coveredArea2.name = row[3]
    }
  }
  return formattedData
}

function organizeTable3(data: any) {
  if (!Array.isArray(data)) return
  const headers = data[1]
  const subHeaders = data[2]
  const dataRows = data.slice(3)

  const headerMap: any = {
    process: {},
    equipment: {},
    rawFuelMaterialsOrProducts: {},
    emissionSourceInformation: {},
    ghgProducedType: {},
    isSteamElectricity: false,
    note: ''
  }

  const headerSections = [
    { key: 'process', start: 0, end: 3 },
    { key: 'equipment', start: 3, end: 6 },
    { key: 'rawFuelMaterialsOrProducts', start: 6, end: 9 },
    { key: 'emissionSourceInformation', start: 10, end: 13 },
  ]
  headerSections.forEach(section => {
    subHeaders.slice(section.start, section.end).forEach((subHeader: string, subIndex: number) => {
      if (subHeader) headerMap[section.key][subHeader] = section.start + subIndex
    })
  })
  headers.forEach((header: string, index: number) => {
    if (header && !headerMap[header]) headerMap[header] = index
  })

  return dataRows.map((row: any) => ({
    process: {
      serialNumber: row[headerMap.process['編號']],
      code: row[headerMap.process['代碼']],
      name: row[headerMap.process['名稱']],
    },
    equipment: {
      serialNumber: row[headerMap.equipment['編號']],
      code: row[headerMap.equipment['代碼']],
      name: row[headerMap.equipment['名稱']],
    },
    rawFuelMaterialsOrProducts: {
      category: row[headerMap.rawFuelMaterialsOrProducts['類別']],
      code: row[headerMap.rawFuelMaterialsOrProducts['代碼']],
      name: row[headerMap.rawFuelMaterialsOrProducts['名稱']],
      isBiomassEnergySource: row[headerMap.rawFuelMaterialsOrProducts['是否屬生質能源']] === "是",
    },
    emissionSourceInformation: {
      category: row[headerMap.emissionSourceInformation['類別']],
      emissionType: row[headerMap.emissionSourceInformation['排放型式']],
      processOrEscapeOrPurchased: row[headerMap.emissionSourceInformation['製程/逸散/外購電力類別']] || null,
    },
    ghgProducedType: {
      CO2: row[headerMap.ghgProducedType['CO2']] === "v",
      CH4: row[headerMap.ghgProducedType['CH4']] === "v",
      N2O: row[headerMap.ghgProducedType['N2O']] === "v",
      HFCS: row[headerMap.ghgProducedType['HFCS']] === "v",
      PFCS: row[headerMap.ghgProducedType['PFCS']] === "v",
      SF6: row[headerMap.ghgProducedType['SF6']] === "v",
      NF3: row[headerMap.ghgProducedType['NF3']] === "v",
    },
    isSteamElectricity: row[headerMap['是否屬汽電共生設備']] === "否",
    note: row[headerMap['備註']] || null,
  }))
}

function organizeTable4(data: any) {
  if (!Array.isArray(data)) return
  const headers = data[1]
  const subHeaders = data[2]
  const dataRows = data.slice(3)

  const headerMap: any = {
    process: {},
    equipment: {},
    rawFuelMaterialsOrProducts: {},
    emissionSourceInformation: {},
    annualActivityData: {},
    note: ''
  }

  const headerSections = [
    { key: 'process', start: 0, end: 3 },
    { key: 'equipment', start: 3, end: 6 },
    { key: 'rawFuelMaterialsOrProducts', start: 6, end: 9 },
    { key: 'emissionSourceInformation', start: 9, end: 13 },
    { key: 'annualActivityData', start: 13, end: 24 },
  ]
  headerSections.forEach(section => {
    subHeaders.slice(section.start, section.end).forEach((subHeader: string, subIndex: number) => {
      if (subHeader) headerMap[section.key][subHeader] = section.start + subIndex
    })
  })
  headers.forEach((header: string, index: number) => {
    if (header && !headerMap[header]) headerMap[header] = index
  })

  return dataRows.map((row: any) => ({
    process: {
      serialNumber: row[headerMap.process['編號']],
      code: row[headerMap.process['代碼']],
      name: row[headerMap.process['名稱']],
    },
    equipment: {
      serialNumber: row[headerMap.equipment['編號']],
      code: row[headerMap.equipment['代碼']],
      name: row[headerMap.equipment['名稱']],
    },
    rawFuelMaterialsOrProducts: {
      code: row[headerMap.rawFuelMaterialsOrProducts['原燃物料或產品代碼']],
      name: row[headerMap.rawFuelMaterialsOrProducts['原燃物料或產品名稱']],
      isBiomassEnergySource: row[headerMap.rawFuelMaterialsOrProducts['是否為生質能源']] === "是",
    },
    emissionSourceInformation: {
      category: row[headerMap.emissionSourceInformation['類別']],
      emissionType: row[headerMap.emissionSourceInformation['排放型式']],
      processOrEscapeOrPurchased: row[headerMap.emissionSourceInformation['製程/逸散/外購電力類別']] || null,
      supplierName: row[headerMap.emissionSourceInformation['電力/蒸汽供應商名稱']] || null,
    },
    annualActivityData: {
      activityData: row[headerMap.annualActivityData['活動數據']],
      activityDataDistributionRatio: row[headerMap.annualActivityData['活動數據分配比率%']],
      activityDataUnit: row[headerMap.annualActivityData['活動數據單位']],
      otherUnitName: row[headerMap.annualActivityData['其他單位名稱']] || null,
      dataSourceFormName: row[headerMap.annualActivityData['數據來源表單名稱']] || null,
      preservationUnit: row[headerMap.annualActivityData['保存單位']] || null,
      activityDataType: row[headerMap.annualActivityData['活動數據種類']] || null,
      fuelHeatValue: row[headerMap.annualActivityData['燃料熱值數值']] || null,
      fuelHeatValueUnit: row[headerMap.annualActivityData['燃料熱值單位']] || null,
      moistureContent: row[headerMap.annualActivityData['含水量(%)']] || null,
      carbonContent: row[headerMap.annualActivityData['含碳量(%)']] || null,
    },
    note: row[headerMap['備註']] || null,
  }))
}

function organizeTable5(data: any) {
  if (!Array.isArray(data)) return
  const headers = data[1]
  const subHeaders = data[2]
  const dataRows = data.slice(3)

  const headerMap: any = {
    process: {},
    equipment: {},
    rawFuelMaterialsOrProducts: {},
    emissionSourceInformation: {},
    activityData: {},
    emissionCoefficientData: {},
    emissionEquivalentsFromSingleSource3: 0,
    biomassFuelFromSingleSource4: 0,
    singleEmissionSourceToTotalEmissions5: 0,
    singleEmissionSourceToTotalEmissions6: 0,
    note: ''
  }

  const headerSections = [
    { key: 'process', start: 0, end: 2 },
    { key: 'equipment', start: 2, end: 4 },
    { key: 'rawFuelMaterialsOrProducts', start: 4, end: 7 },
    { key: 'emissionSourceInformation', start: 7, end: 9 },
    { key: 'activityData', start: 9, end: 11 },
    { key: 'emissionCoefficientData', start: 11, end: 44 }
  ]
  headerSections.forEach(section => {
    subHeaders.slice(section.start, section.end).forEach((subHeader: string, subIndex: number) => {
      if (subHeader) headerMap[section.key][subHeader] = section.start + subIndex
    })
  })
  headers.forEach((header: string, index: number) => {
    if (header && !headerMap[header]) headerMap[header] = index
  })

  return dataRows.map((row: any) => ({
    process: {
      serialNumber: row[headerMap.process['編號']],
      code: row[headerMap.process['代碼']],
    },
    equipment: {
      serialNumber: row[headerMap.equipment['編號']],
      code: row[headerMap.equipment['代碼']],
    },
    rawFuelMaterialsOrProducts: {
      code: row[headerMap.rawFuelMaterialsOrProducts['代碼']],
      name: row[headerMap.rawFuelMaterialsOrProducts['名稱']],
      isBiomassEnergySource: row[headerMap.rawFuelMaterialsOrProducts['是否屬生質能源']] === "是",
    },
    emissionSourceInformation: {
      category: row[headerMap.emissionSourceInformation['類別']],
      emissionType: row[headerMap.emissionSourceInformation['排放型式']],
    },
    activityData: {
      activityData: row[headerMap.activityData['活動數據']],
      unit: row[headerMap.activityData['單位']],
    },
    emissionCoefficientData: {
      ghg1: {
        gas: row[headerMap.emissionCoefficientData['溫室氣體#1']],
        type: row[headerMap.emissionCoefficientData['係數類型']],
        defaultFactor: row[headerMap.emissionCoefficientData['預設排放係數']],
        defaultSource: row[headerMap.emissionCoefficientData['預設係數來源']],
        customFactor: row[headerMap.emissionCoefficientData['自訂排放係數']],
        customSource: row[headerMap.emissionCoefficientData['自訂係數來源']],
        unit: row[headerMap.emissionCoefficientData['係數單位']],
        category: row[headerMap.emissionCoefficientData['係數種類1']],
        emission: row[headerMap.emissionCoefficientData['排放量(公噸/年)']],
        gwp: row[headerMap.emissionCoefficientData['GWP']],
        co2e: row[headerMap.emissionCoefficientData['排放當量(公噸CO2e/年)2']],
      },
      ghg2: {
        gas: row[headerMap.emissionCoefficientData['溫室氣體#2']],
        type: row[headerMap.emissionCoefficientData['係數類型']],
        defaultFactor: row[headerMap.emissionCoefficientData['預設排放係數']],
        defaultSource: row[headerMap.emissionCoefficientData['預設係數來源']],
        customFactor: row[headerMap.emissionCoefficientData['自訂排放係數']],
        customSource: row[headerMap.emissionCoefficientData['自訂係數來源']],
        unit: row[headerMap.emissionCoefficientData['係數單位']],
        category: row[headerMap.emissionCoefficientData['係數種類1']],
        emission: row[headerMap.emissionCoefficientData['排放量(公噸/年)']],
        gwp: row[headerMap.emissionCoefficientData['GWP']],
        co2e: row[headerMap.emissionCoefficientData['排放當量(公噸CO2e/年)2']],
      },
      ghg3: {
        gas: row[headerMap.emissionCoefficientData['溫室氣體#3']],
        type: row[headerMap.emissionCoefficientData['係數類型']],
        defaultFactor: row[headerMap.emissionCoefficientData['預設排放係數']],
        defaultSource: row[headerMap.emissionCoefficientData['預設係數來源']],
        customFactor: row[headerMap.emissionCoefficientData['自訂排放係數']],
        customSource: row[headerMap.emissionCoefficientData['自訂係數來源']],
        unit: row[headerMap.emissionCoefficientData['係數單位']],
        category: row[headerMap.emissionCoefficientData['係數種類1']],
        emission: row[headerMap.emissionCoefficientData['排放量(公噸/年)']],
        gwp: row[headerMap.emissionCoefficientData['GWP']],
        co2e: row[headerMap.emissionCoefficientData['排放當量(公噸CO2e/年)2']],
      },
    },
    emissionEquivalentsFromSingleSource3: row[headerMap['單一排放源排放當量小計(CO2e公噸/年)3']],
    biomassFuelFromSingleSource4: row[headerMap['單一排放源生質燃料之CO2排放當量小計(CO2e公噸/年)4']],
    singleEmissionSourceToTotalEmissions5: row[headerMap['單一排放源占排放總量比(%)5']],
    singleEmissionSourceToTotalEmissions6: row[headerMap['單一排放源占排放總量比6']],
    note: row[headerMap['備註']] || null,
  }))
}

function organizeTable6(data: any) {
  if (!Array.isArray(data)) return
  const headers = data[1]
  const subHeaders = data[2]
  const dataRows = data.slice(3)

  const headerMap: any = {
    process: {},
    equipment: {},
    rawFuelMaterialsOrProducts: {},
    emissionSourceInformation: {},
    emissionFactor: {},
    dataQualityManagement: {},
    note: ''
  }

  const headerSections = [
    { key: 'process', start: 0, end: 2 },
    { key: 'equipment', start: 2, end: 4 },
    { key: 'rawFuelMaterialsOrProducts', start: 4, end: 11 },
    { key: 'emissionSourceInformation', start: 11, end: 13 },
    { key: 'emissionFactor', start: 13, end: 15 },
    { key: 'dataQualityManagement', start: 15, end: 19 }
  ]
  headerSections.forEach(section => {
    subHeaders.slice(section.start, section.end).forEach((subHeader: string, subIndex: number) => {
      if (subHeader) headerMap[section.key][subHeader] = section.start + subIndex
    })
  })
  headers.forEach((header: string, index: number) => {
    if (header && !headerMap[header]) headerMap[header] = index
  })

  return dataRows.map((row: any) => ({
    process: {
      serialNumber: row[headerMap.process['編號']],
      code: row[headerMap.process['代碼']],
    },
    equipment: {
      serialNumber: row[headerMap.equipment['編號']],
      code: row[headerMap.equipment['代碼']],
    },
    rawFuelMaterialsOrProducts: {
      code: row[headerMap.rawFuelMaterialsOrProducts['代碼']],
      name: row[headerMap.rawFuelMaterialsOrProducts['名稱']],
      activityDataLevel: parseInt(row[headerMap.rawFuelMaterialsOrProducts['活動數據種類等級1']]),
      activityDataTrustCategory: row[headerMap.rawFuelMaterialsOrProducts['活動數據可信種類2(儀器校正誤差等級)']],
      activityDataTrustLevel: row[headerMap.rawFuelMaterialsOrProducts['活動數據可信等級3']],
      dataCredibilityDescription: row[headerMap.rawFuelMaterialsOrProducts['數據可信度資訊說明*4']],
      responsibleOrPreservationUnit: row[headerMap.rawFuelMaterialsOrProducts['負責單位或保存單位5']],
    },
    emissionSourceInformation: {
      category: row[headerMap.emissionSourceInformation['類別']],
      emissionType: row[headerMap.emissionSourceInformation['排放型式']],
    },
    emissionFactor: {
      type: row[headerMap.emissionFactor['係數種類']],
      level: row[headerMap.emissionFactor['係數種類等級6']],
    },
    dataQualityManagement: {
      singleEmissionSourceErrorLevel: row[headerMap.dataQualityManagement['單一排放源數據誤差等級7']],
      proportionOfSingleEmissionSource: row[headerMap.dataQualityManagement['單一排放源占排放總量比(%)']] + '%',
      scoringRange: row[headerMap.dataQualityManagement['評分區間範圍8']],
      weightedAverageOfEmissionRatio: row[headerMap.dataQualityManagement['排放量占比加權平均']],
    },
    note: row[headerMap['備註']] || null,
  }))
}

function organizeTable7(data: any) {
  if (!Array.isArray(data)) return
  const headers = data[1]
  const subHeaders = data[2]
  const subTwoHeaders = data[3]
  const dataRows = data.slice(4)

  const headerMap: any = {
    processSerialNumber: '',
    equipmentSerialNumber: '',
    rawFuelMaterialsOrProducts: {},
    activityDataUncertainty: {},
    ghg1EmissionFactorUncertainty: {},
    ghg2EmissionFactorUncertainty: {},
    ghg3EmissionFactorUncertainty: {},
    singleEmissionSourceUncertainty: {},
    note: ''
  }

  const headerSections = [
    { key: 'rawFuelMaterialsOrProducts', start: 2, end: 4 },
    { key: 'activityDataUncertainty', start: 4, end: 8 },
    { key: 'ghg1EmissionFactorUncertainty', start: 8, end: 16 },
    { key: 'ghg2EmissionFactorUncertainty', start: 16, end: 24 },
    { key: 'ghg3EmissionFactorUncertainty', start: 24, end: 32 },
    { key: 'singleEmissionSourceUncertainty', start: 32, end: 34 }
  ]
  headerSections.forEach(section => {
    subHeaders.slice(section.start, section.end).forEach((subHeader: string, subIndex: number) => {
      if (subHeader) headerMap[section.key][subHeader] = section.start + subIndex
    })
  })
  subTwoHeaders.forEach((subTwoHeader: string, index: number) => {
    if (subTwoHeader) headerMap[subTwoHeader] = index
  })
  headers.forEach((header: string, index: number) => {
    if (header && !headerMap[header]) headerMap[header] = index
  })

  return dataRows.map((row: any) => ({
    processSerialNumber: row[headerMap['製程編號']],
    equipmentSerialNumber: row[headerMap['設備編號']],
    rawFuelMaterialsOrProducts: {
      code: row[headerMap.rawFuelMaterialsOrProducts['代碼']],
      name: row[headerMap.rawFuelMaterialsOrProducts['名稱']],
    },
    activityDataUncertainty: {
      lower95CI: row[headerMap.activityDataUncertainty['95%信賴區間之下限1']] || null,
      upper95CI: row[headerMap.activityDataUncertainty['95%信賴區間之上限2']] || null,
      dataSource: row[headerMap.activityDataUncertainty['數據來源3']] || null,
      preservationUnit: row[headerMap.activityDataUncertainty['活動數據保存單位4']] || null,
    },
    ghg1EmissionFactorUncertainty: {
      gas: row[headerMap.ghg1EmissionFactorUncertainty['溫室氣體']] || null,
      emissionAmount: row[headerMap.ghg1EmissionFactorUncertainty['溫室氣體排放當量(噸CO2e/年)']] || null,
      lower95CI: row[headerMap.ghg1EmissionFactorUncertainty['95%信賴區間之下限5']] || null,
      upper95CI: row[headerMap.ghg1EmissionFactorUncertainty['95%信賴區間之上限6']] || null,
      dataSource: row[headerMap.ghg1EmissionFactorUncertainty['係數不確定性資料來源7']] || null,
      preservationUnit: row[headerMap.ghg1EmissionFactorUncertainty['排放係數保存單位8']] || null,
      singleGHGUncertainty: {
        lower95CI: row[headerMap['95%信賴區間之下限']] || null,
        upper95CI: row[headerMap['95%信賴區間之上限']] || null,
      },
    },
    ghg2EmissionFactorUncertainty: {
      gas: row[headerMap.ghg2EmissionFactorUncertainty['溫室氣體']] || null,
      emissionAmount: row[headerMap.ghg2EmissionFactorUncertainty['溫室氣體排放當量(噸CO2e/年)']] || null,
      lower95CI: row[headerMap.ghg2EmissionFactorUncertainty['95%信賴區間之下限5']] || null,
      upper95CI: row[headerMap.ghg2EmissionFactorUncertainty['95%信賴區間之上限6']] || null,
      dataSource: row[headerMap.ghg2EmissionFactorUncertainty['係數不確定性資料來源7']] || null,
      preservationUnit: row[headerMap.ghg2EmissionFactorUncertainty['排放係數保存單位8']] || null,
      singleGHGUncertainty: {
        lower95CI: row[headerMap['95%信賴區間之下限']] || null,
        upper95CI: row[headerMap['95%信賴區間之上限']] || null,
      },
    },
    ghg3EmissionFactorUncertainty: {
      gas: row[headerMap.ghg3EmissionFactorUncertainty['溫室氣體']] || null,
      emissionAmount: row[headerMap.ghg3EmissionFactorUncertainty['溫室氣體排放當量(噸CO2e/年)']] || null,
      lower95CI: row[headerMap.ghg3EmissionFactorUncertainty['95%信賴區間之下限5']] || null,
      upper95CI: row[headerMap.ghg3EmissionFactorUncertainty['95%信賴區間之上限6']] || null,
      dataSource: row[headerMap.ghg3EmissionFactorUncertainty['係數不確定性資料來源7']] || null,
      preservationUnit: row[headerMap.ghg3EmissionFactorUncertainty['排放係數保存單位8']] || null,
      singleGHGUncertainty: {
        lower95CI: row[headerMap['95%信賴區間之下限']] || null,
        upper95CI: row[headerMap['95%信賴區間之上限']] || null,
      },
    },
    singleEmissionSourceUncertainty: {
      lower95CI: row[headerMap.singleEmissionSourceUncertainty['95%信賴區間之下限']] || null,
      upper95CI: row[headerMap.singleEmissionSourceUncertainty['95%信賴區間之上限']] || null,
    },
    note: row[headerMap['備註']] || null,
  }))
}

function organizeTable8_1(data: any) {
  if (!Array.isArray(data)) return

  let headers: string[] = []
  let subHeaders: string[] = []
  let steamPlantProduction: number | null = null
  let dataRows: any[] = []

  data.forEach((row, rowIndex) => {
    if (row.length === 0) return

    if (row[0] && row[0].startsWith("彙整表一、全廠電力")) {
      headers = row
      steamPlantProduction = row[row.length - 1] || null
    } else if (headers.length > 0 && subHeaders.length === 0) {
      subHeaders = row
    } else {
      dataRows.push(row)
      console.log('Adding data row:', row)
    }
  })

  console.log('Final headers:', headers)

  if (headers.length === 0) {
    console.log('No headers found, returning early.')
    return
  }

  const headerMap: any = {}
  headers.forEach((header: string, index: number) => {
    if (header) headerMap[header] = index
  })
  subHeaders.forEach((subHeader: string, index: number) => {
    if (subHeader) headerMap[subHeader] = index
  })

  return dataRows.map(row => {
    const rowData: any = {}
    headers.forEach((header: string, index: number) => {
      rowData[header] = row[index]
    })
    subHeaders.forEach((subHeader: string, index: number) => {
      if (subHeader) rowData[subHeader] = row[index]
    })

    return {
      totalPower: rowData["全廠電力(仟度)"] || 0,
      totalThermalPower: rowData["全廠火力電力(仟度)"] || 0,
      wind: rowData["風力(仟度)"] || 0,
      hydraulic: rowData["水力(仟度)"] || 0,
      geothermal: rowData["地熱(仟度)"] || 0,
      tide: rowData["潮汐(仟度)"] || 0,
      otherRenewableEnergy: rowData["其他再生能源(仟度)"] || 0,
      otherRenewableEnergyNote: rowData["其他再生能源備註"] || 0,
      nuclearPowerGeneration: rowData["核能發電量(仟度)"] || 0,
      otherPowerGeneration: rowData["其他發電量(仟度)"] || 0,
      otherPowerGenerationNote: rowData["其他發電量備註"] || 0,
      steamPlantProduction: steamPlantProduction
    }
  })
}
</script>
