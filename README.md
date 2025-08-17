# Top Food Now 餐廳推薦平台

## 專案簡介

「Top Food Now」是一個餐廳推薦平台，旨在提供用戶個人化的用餐建議，並整合了人工智慧與社群分享功能。後端採用 Spring Boot 3.x 框架，提供 RESTful API 服務給前端。前端則由獨立的 Vue.js 3 專案開發，透過 API 獲取資料並呈現動態使用者介面。

**主要功能特色：**

- **用戶認證與管理：** 包含用戶註冊、登入、電子郵件驗證、密碼重設等功能，基於 Spring Security 搭配 JWT（JSON Web Token）提供安全、無狀態的認證機制。
- **個人化推薦：** 登入用戶可以管理自己的餐廳推薦列表，所有互動都通過 API 實現。
- **社群推薦瀏覽：** 前端可以瀏覽其他用戶公開分享的餐廳推薦，所有數據皆從後端 API 取得。
- **餐廳資訊管理：** 後端負責儲存和管理餐廳的基本資訊、照片等數據。
- **電子郵件服務：** 用於發送用戶驗證碼和密碼重設連結。

## 技術棧

- **後端框架：** Spring Boot 3.5.0
- **程式語言：** Java 17
- **資料庫：** MySQL (AWS RDS)
- **ORM 框架：** Spring Data JPA / Hibernate
- **安全框架：** Spring Security (JWT)
- **雲端服務：** Google Cloud Storage (GCS)、AWS RDS
- **API 文件：** SpringDoc OpenAPI (Swagger UI)
- **日誌：** SLF4J / Logback
- **依賴管理：** Maven

## 環境設置與運行專案

請按照以下步驟，在您的本地環境中啟動並運行「Top Food Now」專案。

### 前置條件

在運行本專案之前，請確保您已安裝以下軟體：

1.  **Java Development Kit (JDK) 17 或更高版本**
2.  **Apache Maven 3.5.x 或更高版本**
3.  **MySQL 資料庫伺服器**
    - 後端預設連接到 AWS RDS 上的 MySQL 資料庫。若要使用本地資料庫，請修改 back-end/src/main/resources/application.properties 中的連線資訊。
4.  **Node.js 和 pnpm**
5.  **Git**
    - 用於從 GitHub Clone 專案。

### 運行專案

1.  **切換到後端目錄，安裝依賴並建置專案，運行後端應用程式**
    - mvn clean install
    - mvn spring-boot:run
    - 後端服務將在 http://localhost:8080 啟動。
2.  **切換到前端目錄，安裝依賴，運行前端應用程式**
    - pnpm install
    - pnpm dev
    - 前端應用程式通常在 http://localhost:5173 啟動。
