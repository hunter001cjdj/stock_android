# TW Stock Analyzer

Learning-first Android app for Taiwan stock analysis.

This repository currently includes:

- an Android MVP built with `Kotlin + Jetpack Compose`
- a simple rule-based scoring engine
- risk/reward quadrant classification
- 1 to 5 star stock ratings
- recommendation and caution reasons for each stock

## Project Structure

- `STOCK_APP_NOTEBOOK.md`: product note, architecture, model idea, and SOP
- `android-app/`: Android application source

## Current MVP

The current Android app already includes:

- featured watchlist cards
- risk/reward filter chips
- ranked stock cards
- stock detail panel
- favorite toggle
- local fake data flow for fast iteration

## How To View The First Version On Your Computer

1. Open `tw-stock-android/android-app` in Android Studio.
2. Let Gradle sync the project.
3. Run the app on an emulator or Android device.

Because this project currently has no backend dependency, you can preview the first version directly from local fake data.

## What Comes Next

- connect real Taiwan stock data sources
- add `Room` local cache
- add `FastAPI` backend
- add `Supabase` storage
- upgrade from rules to ranking models later
