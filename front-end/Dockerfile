# 使用 Node.js 20-alpine 作為構建環境，它是一個輕量級的 Linux 發行版
FROM node:20-alpine AS builder

# 設置工作目錄
WORKDIR /app

# 將 package.json 和 pnpm-lock.yaml 複製到工作目錄，有助於利用 Docker 的緩存機制
COPY package.json pnpm-lock.yaml ./

# 安裝 pnpm
RUN npm install -g pnpm

# 安裝項目依賴
RUN pnpm install --frozen-lockfile

# 將所有其他前端檔案複製到工作目錄
COPY . .

# 運行構建命令，生成靜態檔案
RUN pnpm run build

# 階段二：使用一個輕量級的 Nginx 伺服器來運行靜態檔案
FROM nginx:stable-alpine

# 從構建階段複製構建好的靜態檔案
COPY --from=builder /app/dist /usr/share/nginx/html

# 複製 Nginx 配置檔
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

# 暴露 80 端口，這是 Nginx 預設的 HTTP 端口
EXPOSE 80

# 啟動 Nginx 服務
CMD ["nginx", "-g", "daemon off;"]