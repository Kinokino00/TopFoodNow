import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

// 獲取命令行參數
const args = process.argv.slice(2)
if (args.length === 0) {
  console.error('請提供 JSON 文件名稱，例如：light')
  process.exit(1)
}

const themeName = args[0]

export function generateTailwindConfig(themeName) {
  const __filename = fileURLToPath(import.meta.url)
  const __dirname = path.dirname(__filename)

  // 讀取 JSON 文件
  const jsonFilePath = path.join(__dirname, `./${themeName}.json`)
  let jsonData
  try {
    jsonData = JSON.parse(fs.readFileSync(jsonFilePath, 'utf-8'))
  } catch (error) {
    console.error('JSON 文件解析錯誤:', error.message)
    process.exit(1)
  }

  // 生成 Tailwind CSS 配置
  const themes = {
    [themeName]: {}
  }

  for (const [colorGroup, shades] of Object.entries(jsonData)) {
    const filteredShades = Object.keys(shades)
      .filter(shade => !(/-\d+%/.test(shade)))
      .reduce((acc, shade) => {
        acc[shade] = shades[shade]
        return acc
      }, {})

    for (const [shade, value] of Object.entries(filteredShades)) {
      themes[themeName][`${colorGroup}-${shade}`] = value.$value
    }
  }

  // 讀取現有的 tailwind.config.js 文件
  const tailwindConfigPath = path.join(__dirname, '..', '..', '..', '..', 'tailwind.config.js')
  let tailwindConfigContent = fs.readFileSync(tailwindConfigPath, 'utf-8')

  // 提取現有的 createThemes 配置
  const existingThemesMatch = tailwindConfigContent.match(/createThemes\(([^)]*)\)/)
  let existingThemes = {}

  if (existingThemesMatch) {
    existingThemes = JSON.parse(existingThemesMatch[1])
  }

  // 合併新的主題配置到現有的配置中
  const mergedThemes = {
    ...existingThemes,
    ...themes
  }

  // 更新 createThemes 部分
  const createThemesConfig = `createThemes(${JSON.stringify(mergedThemes, null, 2)})`
  const updatedConfigContent = tailwindConfigContent.replace(
    /createThemes\([^)]*\)/,
    createThemesConfig
  )

  // 將更新後的配置寫入 tailwind.config.js
  fs.writeFileSync(tailwindConfigPath, updatedConfigContent)
  console.log('Tailwind CSS 配置已更新')
}

generateTailwindConfig(themeName)