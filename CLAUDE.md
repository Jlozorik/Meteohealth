# Метео-Здоровье — Project Context

## Обзор

Android-приложение для метеозависимых пользователей. Предупреждает о неблагоприятных погодных условиях, считает индекс благополучия, ведёт метео-дневник. Параллельно ведётся пояснительная записка (отчёт) — материалы разработки должны ложиться в `/docs/*.md`.

## Стек

| Слой | Технология |
|---|---|
| Язык | Kotlin 2.2.10 |
| minSdk / targetSdk | 26 / 36 |
| UI | Jetpack Compose + Material 3 |
| Архитектура | MVVM, одномодульный, пакеты `data/domain/ui` |
| DI | Koin 4.1.0 |
| Сеть | Retrofit 2.11.0 + OkHttp 4.12.0 + kotlinx.serialization 1.8.1 |
| БД | Room 2.7.1 (KSP, не KAPT) |
| Background | WorkManager 2.10.1 |
| Графики | Vico 2.1.2 (compose-m3) |
| Тесты | JUnit4 + MockK 1.14.2 + Turbine 1.2.0 |

## Внешние API

- **OpenWeatherMap** — `/weather` и `/forecast`, ключ в `local.properties` → `BuildConfig.OPEN_WEATHER_API_KEY`
- **NOAA SWPC** — `planetary_k_index_1m.json`, Kp-индекс, без ключа

Demo-режим: при `DEBUG && apiKey.isEmpty()` используется `FakeWeatherRepository`.

## Структура пакетов

```
com.meteohealth/
  App.kt, MainActivity.kt
  data/local/     — Room: entities, DAO, AppDatabase
  data/remote/    — Retrofit: API, DTO, ApiKeyInterceptor
  data/repository/— реализации Repository
  domain/model/   — UserProfile, WeatherSnapshot, DiaryEntry, WellbeingLevel
  domain/repository/ — интерфейсы Repository
  domain/WellbeingCalculator.kt
  ui/theme/, ui/components/, ui/navigation/
  ui/onboarding/, ui/dashboard/, ui/forecast/
  ui/diary/, ui/recommendations/, ui/settings/
  worker/WeatherSyncWorker.kt
  notification/NotificationHelper.kt
  di/             — DatabaseModule, NetworkModule, RepositoryModule, ViewModelModule
```

## Ключевые архитектурные решения

- **Koin + WorkManager**: отключить auto-init WorkManager (`tools:node="remove"` для `InitializationProvider`), инициализировать вручную с `KoinWorkerFactory` в `App.onCreate()`
- **Тёмная тема принудительно**: `dynamicColor = false`, только `darkColorScheme`, палитра: primary `#4DA6FF`, background `#0A0E1A`, surface `#12182B`
- **Offline-first**: при ошибке сети репозитории возвращают кэш из Room, не `Result.failure`
- **Docs без UML**: в `/docs/` только текст и ASCII-таблицы; KDoc-комментарии к репозиториям, ViewModel, WellbeingCalculator, DI-модулям — на русском

## Формула WellbeingIndex (0–100)

```
100
- clamp(|ΔP_6h| × 4, 0, 30)        // давление
- clamp((Kp - 3) × 8, 0, 30)       // геомагнитная активность
- clamp((|ΔT_24h| - 5) × 2, 0, 20) // перепад температуры
- clamp((humidity - 70) × 0.5, 0, 10)
- personalPenalty(profile, conditions)
```

Реализация: `domain/WellbeingCalculator.kt` — чистый object без Android-зависимостей.

## Документация /docs

| Файл | Раздел отчёта |
|---|---|
| `docs/architecture.md` | 2.1 Архитектура |
| `docs/database.md` | 2.2 База данных |
| `docs/ui.md` | 2.3 UI |
| `docs/dependencies.md` | 3.1 Инструментальные средства |
| `docs/testing.md` | 3.5 Тестирование |
