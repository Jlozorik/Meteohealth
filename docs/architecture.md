# 2.1 Архитектура приложения

## Общая структура

Приложение построено по паттерну **MVI** (Model–View–Intent) с разделением на слои `data`, `domain`, `ui`. Единственный модуль — `app`. Зависимости внедряются через **Koin 4.1.0**, организованный по фичам, а не по слоям.

```
com.meteohealth/
├── data/
│   ├── network/        — Ktor: DTO, сервисы, HttpClientFactory, MockEngineFactory
│   ├── storage/        — Room v2: сущности, DAO, конвертеры, AppDatabase
│   └── repository/     — реализации gateway-интерфейсов
├── domain/
│   ├── model/          — Profile, WeatherHour, KpSample, JournalEntry, RiskLevel, …
│   ├── gateway/        — интерфейсы WeatherGateway, KpGateway, JournalGateway, …
│   ├── wellbeing/      — Penalty, WellbeingPipeline, RiskClassifier
│   ├── triggers/       — PearsonAnalyzer, TriggerResult
│   └── usecase/        — ObserveHome, RefreshNow, AppendJournalEntry, …
├── ui/
│   ├── theme/          — Color, Type, Theme (только светлая)
│   ├── components/     — MeteoDrawer, MeteoTopBar, DividedSection, EmptyState
│   └── navigation/     — NavRoutes
├── feature/
│   ├── home/           — HomeContract, HomeReducer, HomeViewModel
│   ├── forecast/       — ForecastContract, ForecastReducer, ForecastViewModel
│   ├── journal/        — JournalContract, JournalReducer, JournalViewModel
│   ├── settings/       — SettingsContract, SettingsReducer, SettingsViewModel
│   └── onboarding/     — OnboardingContract, OnboardingReducer, OnboardingViewModel
├── background/         — NotificationCenter, WeatherTickService, TickReceiver, BootReceiver
└── di/                 — CoreModule, WeatherModule, KpModule, JournalModule,
                          ProfileModule, HomeModule, ForecastModule,
                          JournalUiModule, SettingsUiModule, OnboardingModule, AppModule
```

## MVI

Каждая фича содержит три артефакта:

- **Contract** — `State` (data class), `Intent` (sealed), `Effect` (sealed, одноразовые события).
- **Reducer** — чистая функция `reduce(state, intent): Pair<State, List<Effect>>`, без Android-зависимостей, легко тестируется.
- **ViewModel** — принимает intent, передаёт в reducer, применяет эффекты через `Channel<Effect>`.

```
Screen ──Intent──▶ ViewModel.send(intent)
                       │
                   Reducer.reduce(state, intent) → (newState, effects)
                       │
                   _state.value = newState
                   effects → channel → LaunchedEffect в Screen
```

## Слой данных (data)

- **Ktor Client** (OkHttp engine) вместо Retrofit: `HttpClientFactory.owm()` и `HttpClientFactory.noaa()`.
- **MockEngine** при `BuildConfig.DEBUG && apiKey.isEmpty()` — позволяет запускать приложение без ключа.
- **Room v2** (`meteohealth_v2.db`, версия 1) — нормализованная схема (профиль разбит на 5 таблиц, симптомы/метрики через FK с CASCADE).
- Репозитории реализуют gateway-интерфейсы из `domain/gateway/` и соблюдают **offline-first**: Flow из Room эмитит кэш сразу, сеть обновляет фон.

## Доменный слой (domain)

Только чистый Kotlin, без Android-зависимостей:

- **Gateway-интерфейсы** — контракт между доменом и данными.
- **WellbeingPipeline** — fold по списку `Penalty`-стратегий; `companion object { fun default() }` возвращает стандартный набор из 5 стратегий.
- **RiskClassifier** — `classify(score): RiskLevel` (CALM ≥ 80, WATCH ≥ 60, ALERT ≥ 40, HIGH < 40).
- **PearsonAnalyzer** — корреляция Пирсона между уровнями самочувствия и погодными факторами.
- **Use-cases** — оркестрация: `ObserveHomeUseCase` комбинирует 4 потока в `HomeFeed`.

## Формула индекса (WellbeingPipeline)

```
score = 100
  - PressurePenalty:  clamp(|ΔP_6h| × 4,       0, 30)
  - KpPenalty:        clamp((Kp − 3) × 8,        0, 30)
  - TempPenalty:      clamp((|ΔT_24h| − 5) × 2,  0, 20)
  - HumidityPenalty:  clamp((humidity − 70) × 0.5, 0, 10)
  - PersonalPenalty:  (sensitivity − 3) × 2 + conditions × 2,  coerceIn(0, 10)
```

## Фоновая работа

`WeatherTickService` (Foreground Service, `dataSync`) запускается `AlarmManager.setInexactRepeating` раз в час. `TickReceiver` планирует будильник при старте и пересоздаёт его `BootReceiver`-ом после перезагрузки.

WorkManager полностью удалён; `InitializationProvider` убран из манифеста.

## Внедрение зависимостей

Koin-модули сгруппированы по фичам:

| Модуль | Содержимое |
|---|---|
| CoreModule | AppDatabase, DAO, owmClient, noaaClient (named qualifier), IO Dispatcher |
| WeatherModule, KpModule | gateway-реализации + сервисы |
| JournalModule, ProfileModule | gateway-реализации |
| HomeModule | WellbeingPipeline, ObserveHomeUseCase, HomeViewModel |
| ForecastModule, JournalUiModule, SettingsUiModule, OnboardingModule | ViewModel соответствующих фич |
| AppModule | `allModules` — список всех выше |

## Поток данных

```
Compose Screen ──collect──▶ ViewModel.state: StateFlow<State>
                                 │
                           send(intent) → Reducer → (newState, effects)
                                 │
                       Use-case (Flow / suspend)
                                 │
                    GatewayImpl (data/repository)
                        │               │
                 Room (Flow)        Ktor (suspend)
```
