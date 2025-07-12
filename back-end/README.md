# Top Food Now 餐廳推薦平台

## 專案簡介

「Top Food Now」是一個餐廳推薦平台，旨在提供用戶個人化的用餐建議，並整合了AI和社群分享功能。用戶可以探索由AI生成的餐廳推薦，也可以瀏覽其他網紅或用戶分享的餐廳心得。本專案採用 Spring Boot 框架開發後端 API 和 MVC 頁面，搭配 MySQL 資料庫儲存數據，並透過 Thymeleaf 模板引擎渲染前端頁面。

**主要功能特色：**

* **用戶認證與管理：** 包含用戶註冊、登入、電子郵件驗證、密碼重設等功能，基於 Spring Security 提供安全認證。
* **個人化推薦：** 登入用戶可以管理自己的餐廳推薦列表。
* **AI 推薦整合：** 結合外部 AI 服務（ChatGPT）為用戶提供動態的餐廳建議。
* **社群推薦瀏覽：** 瀏覽其他用戶（尤其是網紅）公開分享的餐廳推薦。
* **餐廳資訊管理：** 儲存和展示餐廳的基本資訊、照片等。
* **截圖功能：** 可能用於生成餐廳或推薦的預覽圖。
* **電子郵件服務：** 用於發送用戶驗證碼和密碼重設連結。

## 技術棧

* **後端框架：** Spring Boot 3.x
* **程式語言：** Java 17+
* **資料庫：** MySQL (使用 AWS RDS)
* **ORM 框架：** Spring Data JPA / Hibernate
* **安全框架：** Spring Security
* **模板引擎：** Thymeleaf
* **API 文件：** SpringDoc OpenAPI (Swagger UI)
* **日誌：** SLF4J / Logback
* **依賴管理：** Maven

## 環境設置與運行專案

請按照以下步驟，在您的本地環境中啟動並運行「Top Food Now」專案。

### 前置條件

在運行本專案之前，請確保您已安裝以下軟體：

1.  **Java Development Kit (JDK) 17 或更高版本**
    * 您可以從 [Oracle 官網](https://www.oracle.com/java/technologies/downloads/) 或 [AdoptOpenJDK](https://adoptopenjdk.net/) 下載並安裝。
2.  **Apache Maven 3.6.x 或更高版本**
    * Maven 用於管理專案依賴和建構。
3.  **MySQL 資料庫伺服器**
    * 本專案預設連接到 AWS RDS 上的 MySQL 資料庫。若要使用本地資料庫，您需要修改 `application.properties` 中的 `spring.datasource.url`。
4.  **網頁瀏覽器**
    * Chrome, Firefox, Edge 等現代瀏覽器。