# 台股 AI 分析 App 筆記

更新日期：2026-03-17

## 1. 專案目標

這是一個以台股分析為主題的 Android App。

目前已完成的方向：

- Android 手機端 App
- 繁體中文介面
- 透明科技感 UI
- 依產業分類瀏覽股票
- 收藏清單
- AI 分析助理入口
- 串接 TWSE 官方開放資料
- 可產出 APK 並在 BlueStacks 成功執行

目前定位：

- 這是一個可展示、可安裝、可操作的 Android MVP
- 前端已完成主要互動流程
- 資料來源已從本地假資料切到官方真資料讀取
- 目前沒有獨立後端伺服器，資料由 App 直接向官方資料源抓取

## 2. 專案結構

專案根目錄：

- `tw-stock-android/`

主要目錄：

- `android-app/`
  Android App 原始碼
- `dist/`
  已產出的 APK
- `README.md`
  專案簡介
- `STOCK_APP_NOTEBOOK.md`
  專案筆記與執行紀錄

APK 位置：

- `C:\Users\Administrator\Desktop\project\test\tw-stock-android\dist\app-debug.apk`
- `C:\Users\Administrator\Desktop\project\test\tw-stock-android\dist\app-release-unsigned.apk`

## 3. 技術架構

### 前端

- Kotlin
- Jetpack Compose
- Material 3
- ViewModel
- StateFlow

前端負責：

- 首頁儀表板
- 產業分類切換
- 股票清單與個股詳情
- 收藏功能
- AI 分析頁
- 刷新官方資料

### 資料層

- Repository Pattern
- `StockRepository`
- `TwseHybridStockRepository`

資料層負責：

- 讀取官方資料
- 整理報價與估值欄位
- 產出 App 顯示用的 `StockAnalysis`
- 依規則計算風險分數、報酬分數、綜合分數

### 伺服器 / 後端

目前狀態：

- 沒有獨立後端伺服器
- 沒有 FastAPI
- 沒有 Supabase
- 沒有 Room 本地資料庫

目前架構屬於：

`Android App -> TWSE 官方開放資料`

也就是說，現在的版本是：

- 前端 App 直接打官方 API
- 在手機端本地完成分析與 UI 呈現

### 未來後端規劃

後續若要升級成更完整架構，可加入：

- FastAPI
- Supabase / PostgreSQL
- Room 快取
- 排程刷新
- AI 服務 API

未來完整型架構可長成：

`TWSE / 其他資料源 -> FastAPI -> DB / Cache -> Android App`

## 4. Android App 架構

目前採用：

- 單 Activity 架構
- Compose UI
- ViewModel 管理狀態
- Repository 提供資料

主要流程：

1. `MainActivity` 啟動
2. 建立 `StockAnalyzerViewModel`
3. `ViewModel` 初始化時自動刷新資料
4. `TwseHybridStockRepository` 讀取 TWSE 官方資料
5. 轉成 `StockAnalysis`
6. UI 顯示股票卡片、收藏、AI 分析

## 5. 主要程式碼位置

### App 入口

- `android-app/app/src/main/java/com/example/twstockanalyzer/MainActivity.kt`

用途：

- 啟動 App
- 建立 `StockAnalyzerViewModel`
- 將畫面交給 `StockAnalyzerApp`

### ViewModel

- `android-app/app/src/main/java/com/example/twstockanalyzer/ui/StockAnalyzerViewModel.kt`

用途：

- 管理 UI 狀態
- 切換分頁
- 切換產業分類
- 處理收藏
- 呼叫刷新
- 產生 AI 分析文字

### UI 主畫面

- `android-app/app/src/main/java/com/example/twstockanalyzer/ui/StockAnalyzerApp.kt`

用途：

- 顯示首頁儀表板
- 顯示產業分類
- 顯示焦點清單
- 顯示股票卡片
- 顯示收藏頁
- 顯示 AI 分析頁

### Repository

- `android-app/app/src/main/java/com/example/twstockanalyzer/data/repository/StockRepository.kt`
- `android-app/app/src/main/java/com/example/twstockanalyzer/data/repository/TwseHybridStockRepository.kt`

用途：

- 定義資料來源介面
- 從 TWSE 官方資料源抓資料
- 轉換成 App 所需模型

### Domain Model

- `android-app/app/src/main/java/com/example/twstockanalyzer/domain/model/StockAnalysis.kt`
- `android-app/app/src/main/java/com/example/twstockanalyzer/domain/model/StockSector.kt`
- `android-app/app/src/main/java/com/example/twstockanalyzer/domain/model/RiskRewardQuadrant.kt`

用途：

- 定義股票分析資料格式
- 定義產業分類
- 定義風險 / 報酬象限

## 6. 真實資料來源

目前使用的官方資料來源：

- TWSE 收盤與成交資訊
  - `https://openapi.twse.com.tw/v1/exchangeReport/STOCK_DAY_ALL`
- TWSE 殖利率 / 本益比 / 股價淨值比
  - `https://openapi.twse.com.tw/v1/exchangeReport/BWIBBU_ALL`

目前資料特性：

- 使用官方開放資料
- 可直接在 App 中刷新
- 非逐筆成交等級
- 屬於真資料分析版，不是示範假資料版

## 7. 分析邏輯

目前分析引擎會根據以下欄位做綜合判斷：

- 收盤價
- 漲跌幅
- 成交量
- 本益比
- 股價淨值比
- 殖利率

目前輸出：

- `rewardScore`
- `riskScore`
- `finalScore`
- 星等
- 推薦理由
- 風險提醒
- 摘要說明

## 8. 產業分類邏輯

目前 App 以產業分類作為主要瀏覽方式。

已支援分類：

- 全部
- AI 伺服器
- 半導體
- 科技電子
- 金融
- 航運
- 傳產
- ETF
- 其他

用途：

- 讓使用者不是只看單一排行
- 可以依產業快速縮小觀察範圍
- 更符合台股實際看盤習慣

## 9. UI 功能

目前已完成的頁面與功能：

### 總覽頁

- 儀表板 Header
- 更新時間
- 資料來源顯示
- 刷新按鈕
- 市場快照
- 產業分類切換
- 今日焦點清單
- 股票卡片列表
- 個股詳情

### 收藏頁

- 加入收藏
- 取消收藏
- 集中查看所有收藏股
- 從收藏頁回到個股詳情

### AI 分析頁

- AI 提示詞按鈕
- 顯示最新分析
- 顯示歷史訊息
- 根據目前選中的股票產生摘要

## 10. 安裝與建置成功流程

### 開發環境

最小成功建置條件：

- Windows
- JDK 17
- Android SDK
- Gradle Wrapper

目前專案設定：

- `compileSdk = 34`
- `targetSdk = 34`
- `minSdk = 26`
- Java 17
- Kotlin JVM target 17

### App 權限

目前已加入：

- `INTERNET`

位置：

- `android-app/app/src/main/AndroidManifest.xml`

### 建置方式

在 `android-app/` 目錄下執行：

```powershell
.\gradlew.bat assembleDebug
```

成功後產物位置：

- `android-app/app/build/outputs/apk/debug/app-debug.apk`

之後再複製到：

- `dist/app-debug.apk`

## 11. 執行成功方式

目前確認可用的執行方式：

- 使用 BlueStacks 安裝 APK

步驟：

1. 開啟 BlueStacks
2. 把 `dist/app-debug.apk` 拖入 BlueStacks
3. 安裝完成後打開 App
4. 進入首頁
5. 使用產業分類切換、收藏、AI 分析等功能

目前確認成功：

- App 可在 BlueStacks 開啟
- 中文介面可顯示
- 新版透明科技風 UI 可顯示
- 收藏頁可使用
- AI 分析頁可使用

## 12. APK 發佈方式

目前已完成 GitHub 發佈。

倉庫：

- `https://github.com/hunter001cjdj/android_stock`

下載位置：

- GitHub 頁面：
  - `https://github.com/hunter001cjdj/android_stock/blob/main/downloads/app-debug.apk`
- 直接下載：
  - `https://raw.githubusercontent.com/hunter001cjdj/android_stock/main/downloads/app-debug.apk`

用途：

- 可直接用手機點擊下載 APK
- 可分享給其他裝置安裝測試

## 13. 當前版本總結

目前這個版本已經達成：

- 有 Android App
- 有中文 UI
- 有真資料來源
- 有產業分類
- 有收藏功能
- 有 AI 分析入口
- 有 APK
- 可在模擬器類環境成功安裝與操作
- 可透過 GitHub 連結下載

目前尚未導入，但屬於下一階段可擴充項目：

- FastAPI 後端
- Supabase
- Room 快取
- 真正的 AI API
- 更高頻或更完整的即時資料源

## 14. 下一步建議

接下來最合理的擴充順序：

1. 把 TWSE / 其他資料源整理成更完整的欄位結構
2. 補上本地快取
3. 導入後端 API
4. 導入真實 AI 分析服務
5. 補上登入、個人化收藏與推播
