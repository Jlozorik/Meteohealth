# 2.1 Архитектура приложения

## Общая структура

Приложение построено по архитектурному паттерну **MVVM** (Model-View-ViewModel) с разделением на три слоя: `data`, `domain`, `ui`. Модуль единственный — весь код находится в модуле `app`.

```
com.meteohealth/
├── data/
│   ├── local/       — Room: сущности, DAO, AppDatabase
│   ├── remote/      — Retrofit: API, DTO, интерсепторы
│   └── repository/  — реализации интерфейсов репозиториев
├── domain/
│   ├── model/       — доменные модели (UserProfile, DiaryEntry, …)
│   ├── repository/  — интерфейсы репозиториев
│   ├── WellbeingCalculator.kt
│   └── TriggerAnalyzer.kt
├── ui/
│   ├── theme/       — цвета, типографика, тема Material 3
│   ├── navigation/  — NavGraph, BottomNavBar, NavRoutes
│   ├── components/  — переиспользуемые компоненты
│   ├── onboarding/
│   ├── dashboard/
│   ├── forecast/
│   ├── diary/
│   ├── recommendations/
│   └── settings/
├── di/              — Koin-модули
├── worker/          — WeatherSyncWorker
└── notification/    — NotificationHelper
```

## Слои и их ответственности

### Слой данных (data)

Реализует два источника данных:

- **Room** — локальная БД (кэш погоды, дневник, профиль пользователя, лог уведомлений, кэш Kp-индекса).
- **Retrofit** — сетевые API: OpenWeatherMap (текущая погода, прогноз) и NOAA SWPC (Kp-индекс).

Репозитории реализуют принцип **offline-first**: при недоступности сети возвращают данные из Room, а не `Result.failure`.

В режиме разработки (BuildConfig.DEBUG + пустой API-ключ) подключается `FakeWeatherRepository` с синтетическими данными.

### Доменный слой (domain)

Содержит только Kotlin-классы без Android-зависимостей:

- **Доменные модели** — `DiaryEntry`, `UserProfile`, `WeatherSnapshot`, `ForecastDay`, `WellbeingLevel`, `Recommendation`, `PressureUnit`.
- **Интерфейсы репозиториев** — `DiaryRepository`, `WeatherRepository`, `KpRepository`, `UserProfileRepository`.
- **WellbeingCalculator** — вычисляет индекс самочувствия (0–100) по формуле с фиксированными весами.
- **TriggerAnalyzer** — корреляция Пирсона между записями дневника и погодными факторами.
- **WellbeingPredictor** — линейная регрессия (МНК), обучается на записях дневника, предсказывает персональный индекс по данным прогноза погоды.

### Слой представления (ui)

Весь UI построен на **Jetpack Compose + Material 3**. Каждый экран имеет собственный ViewModel. Навигация реализована через `NavHost` с нижней панелью `NavigationBar`.

Состояние экранов передаётся через `StateFlow` → `collectAsState()` / `collectAsStateWithLifecycle()`.

## Внедрение зависимостей

Используется **Koin 4.1.0**. DI-модули:

| Модуль | Содержимое |
|---|---|
| DatabaseModule | AppDatabase, все DAO |
| NetworkModule | Retrofit, OkHttp, WeatherApi, KpApi |
| RepositoryModule | реализации репозиториев |
| ViewModelModule | все ViewModel |

WorkManager инициализируется вручную с `KoinWorkerFactory` в `App.onCreate()`, поэтому стандартный `InitializationProvider` отключён через `tools:node="remove"` в манифесте.

## Фоновая работа

`WeatherSyncWorker` (WorkManager) запускается периодически раз в 3 часа. При неблагоприятных условиях (индекс < порогового значения из профиля) отправляет уведомление через `NotificationHelper`.

## Персонализация и ML

Приложение реализует два уровня персонализации:

1. **Статический** — `personalPenalty()` в `WellbeingCalculator` добавляет штраф к индексу в зависимости от хронических состояний из профиля (гипертония, мигрень, суставы, дыхание).

2. **Адаптивный (ML)** — `WellbeingPredictor` обучается на записях дневника методом наименьших квадратов (МНК / OLS):
   - Признаки: атмосферное давление (гПа), температура (°C), Kp-индекс.
   - Целевая переменная: уровень самочувствия из дневника (TERRIBLE=10 … GREAT=90).
   - Обучение: normal equations `w = (XᵀX)⁻¹ Xᵀy`, матричная арифметика на чистом Kotlin без внешних зависимостей.
   - Требует ≥10 записей с полными метеоданными; при нехватке данных предиктор не активируется.
   - Результат: персональный прогноз самочувствия на каждый из 5 дней на экране «Прогноз».

3. **Аналитический** — `TriggerAnalyzer` вычисляет корреляцию Пирсона между записями дневника и тремя факторами (давление, температура, Kp), отображая личные триггеры на экране «Анализ».

## Поток данных

```
UI (Compose Screen)
    ↕ StateFlow / collectAsState
ViewModel
    ↕ suspend / Flow
Repository (interface — domain)
    ↕
RepositoryImpl (data)
    ↕                   ↕
Room (local cache)    Retrofit (remote API)
```
