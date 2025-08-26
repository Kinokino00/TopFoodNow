# --- 構建前端專案 (Build Stage) ---
FROM node:20-alpine AS builder

# 設置工作目錄
WORKDIR /app

# 將 package.json 和 pnpm-lock.yaml 複製到工作目錄
COPY package.json pnpm-lock.yaml ./

# 安裝 pnpm
RUN npm install -g pnpm

# 安裝項目依賴
RUN pnpm install --frozen-lockfile

# 將所有其他前端檔案複製到工作目錄
COPY . .

# 運行構建命令，生成靜態檔案
RUN pnpm run build

# --- 最後一步，準備靜態檔案給資料卷使用 ---
# 使用一個輕量級的映像檔作為最終映像檔
FROM alpine

# 設置工作目錄
WORKDIR /app

# 從構建階段複製構建好的靜態檔案
COPY --from=builder /app/dist ./dist

# 無需啟動任何服務，因為靜態檔案將由 Nginx 服務
CMD ["echo", "Frontend build completed. Ready for use by Nginx service."]