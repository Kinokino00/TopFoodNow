# --- 階段一：構建前端專案 (Build Stage) ---
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

# --- 階段二：運行 Nginx 伺服器 (Production Stage) ---
FROM nginx:stable-alpine

# 從構建階段複製構建好的靜態檔案到 Nginx 的網站根目錄
COPY --from=builder /app/dist /usr/share/nginx/html

# 複製 Nginx 配置檔
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

# 複製我們用來注入環境變數的啟動腳本
COPY entrypoint.sh /entrypoint.sh

# 給予該腳本執行權限
RUN chmod +x /entrypoint.sh

# 暴露 80 端口
EXPOSE 80

# 將容器的啟動指令改為執行我們的腳本
ENTRYPOINT ["/entrypoint.sh"]