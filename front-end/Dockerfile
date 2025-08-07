# -------------------- 構建階段 --------------------
# 使用 Node.js 作為構建環境
FROM node:20-alpine AS builder

# 設置工作目錄
WORKDIR /app

# 將 package.json 和 pnpm-lock.yaml 複製到工作目錄
# 這有助於利用 Docker 的緩存機制
# pnpm 使用的是 pnpm-lock.yaml 而非 package-lock.json
COPY package.json pnpm-lock.yaml ./

# 安裝項目依賴
# 將 npm install 修改為 pnpm install
RUN pnpm install --frozen-lockfile

# 將所有其他前端檔案複製到工作目錄
COPY . .

# 運行構建命令，生成靜態檔案
# 如果您使用的是 Vite，命令通常是 `pnpm run build`
# 請根據您的 package.json 中的實際命令來修改
RUN pnpm run build


# -------------------- 運行階段 --------------------
# 使用一個輕量級的 web 伺服器來提供靜態檔案，例如 Nginx
FROM nginx:alpine

# 從構建階段複製構建好的靜態檔案
# 預設的構建輸出目錄通常是 `dist`，請根據您的實際情況檢查
COPY --from=builder /app/dist /usr/share/nginx/html

# 複製 Nginx 配置檔
# 您需要在前端專案中建立一個 nginx.conf 檔案
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

# 暴露 80 端口，這是 Nginx 預設的 HTTP 端口
EXPOSE 80