# 台股分析 Android App 開發筆記

更新日期：2026-03-16

## 0. 目前實作進度

目前 `android-app/` 已完成第一版本地 MVP：

- 假資料 Repository
- 規則評分引擎
- 四象限分類
- 1 到 5 星推薦
- 推薦 / 不推薦原因
- 排行榜、精選區塊、篩選區塊、個股詳情、收藏切換

目前尚未完成：

- 真實台股資料串接
- FastAPI 後端
- Supabase / Room 持久化
- XGBoost Ranker

## 1. 專案目標

這是一個以學習用途為主的台股分析 Android App，核心目標是：

- 提供台股分析與推薦，不串券商下單
- 依風險與報酬分成四象限
- 提供 1 到 5 星推薦指數
- 清楚列出推薦原因與不推薦原因
- 後續可逐步升級成即時版與模型版

目前定位是：

- 先做分析推薦
- 先做人看得懂的規則
- 先用免費資料來源
- 自動化買賣留到未來再考慮

## 2. 最推薦的免費技術組合

### 前端

- `Kotlin`
- `Jetpack Compose`
- `Material 3`

原因：

- Android 官方主推
- 適合快速做排行榜、篩選、個股詳情、收藏頁
- 後續維護成本低

### App 架構

- `MVVM`
- `Repository`
- `ViewModel`
- `StateFlow`

原因：

- 資料流清楚
- 適合 API + 本地快取 + UI 狀態管理

### 本地資料庫

- `Room`

原因：

- 官方方案
- 適合快取股票列表、收藏、最近分析結果

### 後端

- `Python + FastAPI`
- `APScheduler` 或 `cron`

原因：

- Python 適合資料清洗、特徵工程、模型訓練
- FastAPI 很適合做分析 API

### 雲端資料庫

- `Supabase Free`

原因：

- 免費方案夠做學習專案
- 使用 PostgreSQL，查詢分析資料比文件型資料庫直覺
- 適合存快取、分析結果、使用者偏好、收藏清單

### 最推薦的整體組合

```text
Android: Kotlin + Compose
App 架構: MVVM + Repository + ViewModel + StateFlow
Local DB: Room
Backend: Python + FastAPI
Cloud DB: Supabase Free
Data: TWSE + TPEx + MOPS + FinMind
Model: 規則評分模型 -> XGBoost Ranker
```

## 3. 免費資料來源怎麼選

### TWSE

用途：

- 上市公司基本資料
- 每日成交資訊
- 法人、融資融券、統計資料

推薦原因：

- 官方來源
- 是台股分析的核心資料源

### TPEx

用途：

- 上櫃股票資料
- OTC 交易與統計資料

推薦原因：

- 補足上櫃市場
- 跟 TWSE 搭配才算完整台股池

### MOPS

用途：

- 財報
- 月營收
- 重大訊息
- 公司公告

推薦原因：

- 做優質股與成長股分析時非常重要
- 很適合產出推薦原因與不推薦原因

### FinMind

用途：

- 快速驗證資料流程
- MVP 階段 API 資料來源
- 回測與模型實驗

推薦原因：

- 對學習與原型階段很友善
- 可加速第一版落地

### 關於免費即時資料

最務實的結論是：

- `日更分析` 完全可行
- `分鐘級更新` 有機會做到
- `完整逐筆即時行情` 不適合一開始假設成全免費

所以第一版建議先做：

```text
收盤後分析 + 分鐘級更新 + 快取
```

## 4. 模型推薦

## 第一階段主模型：規則評分模型

推薦程度：`5 星`

推薦原因：

- 最容易實作
- 最好解釋
- 很適合先做四象限分類
- 可以直接輸出推薦原因與不推薦原因

適合的指標：

- 營收年增率
- EPS 成長率
- ROE
- 毛利率 / 營益率
- 負債比
- 本益比 / 股價淨值比
- 近 20 日 / 60 日趨勢
- 成交量變化
- 波動率

## 第二階段升級：XGBoost Ranker

推薦程度：`4.5 星`

推薦原因：

- 適合股票排序推薦
- 對表格型金融資料通常表現不錯
- 能補足規則模型的限制

## 第三階段備選：LightGBM Ranker

推薦程度：`4 星`

推薦原因：

- 速度快
- 也適合 ranking 任務

## Baseline：Random Forest

推薦程度：`3 星`

推薦原因：

- 可做 baseline
- 好上手

## 暫不建議作主模型

- `Prophet`
- `單純時間序列價格預測`

原因：

- 你現在要的是選股推薦，不只是預測價格
- 台股分析要結合基本面、價量、風險與報酬

## 5. 四象限與星等邏輯

### 風險分數 Risk Score

分數範圍：`0 ~ 100`

可參考項目：

- 波動率
- 回撤
- 負債比
- 估值偏高程度
- 短線過熱程度

### 報酬分數 Reward Score

分數範圍：`0 ~ 100`

可參考項目：

- 營收年增率
- EPS 成長率
- ROE
- 毛利率 / 營益率
- 趨勢強度

### 四象限分類

- 高風險高報酬：`Risk >= 60` 且 `Reward >= 60`
- 低風險高報酬：`Risk < 60` 且 `Reward >= 60`
- 高風險低報酬：`Risk >= 60` 且 `Reward < 60`
- 低風險低報酬：`Risk < 60` 且 `Reward < 60`

### 星等建議

- `5 星`：綜合條件優秀，風險與報酬比佳
- `4 星`：值得關注，條件大致不錯
- `3 星`：中性觀察
- `2 星`：偏弱，不優先推薦
- `1 星`：明顯不推薦

## 6. 推薦與不推薦原因要怎麼寫

### 推薦原因模板

- 營收持續年增
- EPS 明顯成長
- ROE 穩定且高於平均
- 量價結構轉強
- 估值仍在合理區間

### 不推薦原因模板

- 波動過高
- 近期回撤過大
- 負債比偏高
- 獲利不穩
- 估值過高

### 輸出格式建議

每檔股票至少輸出：

- 股票代號
- 股票名稱
- 風險分數
- 報酬分數
- 四象限
- 星等
- 推薦原因
- 不推薦原因
- 總結

## 7. App 功能範圍

### MVP 必做

- 股票列表頁
- 排行榜頁
- 四象限篩選頁
- 股票詳情頁
- 1 到 5 星顯示
- 推薦原因 / 不推薦原因
- 收藏功能

### 第二階段

- 自選股清單
- 歷史分析結果
- 每日更新分析
- 推薦變化追蹤

### 後續再做

- 更高頻更新
- 模型排序版本
- 通知功能
- 自動化交易研究

## 8. 資料流架構

```text
TWSE / TPEx / MOPS / FinMind
        ->
Python 抓資料與清洗
        ->
規則評分 / 排序模型
        ->
Supabase 儲存分析結果
        ->
FastAPI 提供 App API
        ->
Android App 顯示排行、星等、推薦與不推薦原因
```

## 9. 開發 SOP

### Phase 1：定義規則

- [ ] 定義四象限欄位
- [ ] 定義風險分數公式
- [ ] 定義報酬分數公式
- [ ] 定義星等公式
- [ ] 定義推薦原因模板
- [ ] 定義不推薦原因模板

### Phase 2：整合資料

- [ ] 串接 TWSE
- [ ] 串接 TPEx
- [ ] 串接 MOPS
- [ ] 串接 FinMind 做 MVP 驗證
- [ ] 統一欄位格式與股票代號

### Phase 3：建立分析引擎

- [ ] 實作規則評分模型
- [ ] 產出四象限分類
- [ ] 產出 1 到 5 星
- [ ] 產出推薦與不推薦原因
- [ ] 輸出分析 JSON

### Phase 4：建立後端

- [ ] 建資料抓取腳本
- [ ] 建特徵工程流程
- [ ] 建 FastAPI API
- [ ] 建每日更新排程
- [ ] 存進 Supabase

### Phase 5：建立 Android App

- [ ] 建 Compose 專案骨架
- [ ] 建首頁排行榜
- [ ] 建四象限篩選頁
- [ ] 建股票詳情頁
- [ ] 建收藏頁
- [ ] 用 Room 快取

### Phase 6：升級模型

- [ ] 定義未來報酬標籤
- [ ] 訓練 XGBoost Ranker
- [ ] 用 LightGBM 做對照
- [ ] 比較規則模型與排序模型

### Phase 7：驗證與調整

- [ ] 檢查推薦理由是否合理
- [ ] 檢查高星股票後續表現
- [ ] 調整指標權重
- [ ] 調整 UI 呈現方式

## 10. 開發順序建議

最穩的順序是：

1. 先做假資料 UI
2. 先做規則模型
3. 再串免費資料
4. 再做本地快取與雲端儲存
5. 最後再上 XGBoost Ranker

## 11. 目前外部參考

- TWSE：https://www.twse.com.tw/
- TPEx：https://www.tpex.org.tw/
- MOPS：https://mops.twse.com.tw/
- FinMind：https://finmindtrade.com/
- Android Architecture：https://developer.android.com/topic/architecture/recommendations
- Room：https://developer.android.com/training/data-storage/room
- Supabase：https://supabase.com/pricing
- Firebase：https://firebase.google.com/pricing
- XGBoost Ranking：https://xgboost.readthedocs.io/en/release_2.1.0/tutorials/learning_to_rank.html
