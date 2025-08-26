#!/bin/sh
# entrypoint.sh

# 建立一個包含環境變數的 JavaScript 檔案
# 注意 VITE_API_BASE_URL 前面沒有 '$'，因為我們要把字串本身寫入檔案
echo "window.runtimeConfig = { VITE_API_BASE_URL: '${VITE_API_BASE_URL}' };" > /usr/share/nginx/html/config.js

# 執行 Nginx 伺服器
exec nginx -g 'daemon off;'