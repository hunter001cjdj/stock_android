# 台股 AI 分析 App

這是一個以台股分析為主題的 Android App。

目前版本已完成：

- 繁體中文介面
- 透明科技感 UI
- 依產業分類瀏覽股票
- 收藏清單
- AI 分析助理入口
- 串接 TWSE 官方開放資料
- 產出 APK 並可於 BlueStacks 安裝測試

## 專案目錄

- `android-app/`
  Android App 原始碼
- `dist/`
  已產出的 APK
- `STOCK_APP_NOTEBOOK.md`
  完整專案筆記、架構與執行紀錄

## 目前技術架構

前端：

- Kotlin
- Jetpack Compose
- Material 3
- ViewModel
- StateFlow

資料層：

- Repository Pattern
- `StockRepository`
- `TwseHybridStockRepository`

目前資料流：

`Android App -> TWSE 官方開放資料`

目前尚未加入：

- FastAPI
- Supabase
- Room

## 目前功能

- 首頁儀表板
- 市場快照
- 產業分類切換
- 焦點股票清單
- 個股詳情
- 收藏 / 取消收藏
- AI 分析頁
- 官方資料刷新

目前支援的產業分類：

- 全部
- AI 伺服器
- 半導體
- 科技電子
- 金融
- 航運
- 傳產
- ETF
- 其他

## 真實資料來源

目前使用：

- TWSE 收盤 / 成交資料
  - `https://openapi.twse.com.tw/v1/exchangeReport/STOCK_DAY_ALL`
- TWSE 殖利率 / 本益比 / 股價淨值比
  - `https://openapi.twse.com.tw/v1/exchangeReport/BWIBBU_ALL`

## 建置方式

需求：

- JDK 17
- Android SDK
- Gradle Wrapper

目前 Android 設定：

- `compileSdk = 34`
- `targetSdk = 34`
- `minSdk = 26`

在 `android-app/` 目錄執行：

```powershell
.\gradlew.bat assembleDebug
```

輸出位置：

- `android-app/app/build/outputs/apk/debug/app-debug.apk`

整理後 APK：

- `dist/app-debug.apk`
- `dist/app-release-unsigned.apk`

## 執行方式

目前已確認可行方式：

- 使用 BlueStacks 安裝 APK

步驟：

1. 開啟 BlueStacks
2. 把 `dist/app-debug.apk` 拖入視窗
3. 安裝後開啟 App

## GitHub APK 下載

APK 已發佈，可直接下載：

- GitHub 頁面：
  - `https://github.com/hunter001cjdj/android_stock/blob/main/downloads/app-debug.apk`
- 直接下載：
  - `https://raw.githubusercontent.com/hunter001cjdj/android_stock/main/downloads/app-debug.apk`

## 補充文件

更完整的內容請看：

- `STOCK_APP_NOTEBOOK.md`

裡面包含：

- 架構說明
- 資料來源
- App 流程
- Android 建置與執行方式
- 當前版本總結
- 下一步規劃
