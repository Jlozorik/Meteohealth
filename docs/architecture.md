# 2.1 Архитектура приложения

## Общая структура

Приложение построено по архитектурному паттерну **MVVM** (Model-View-ViewModel) с разделением на три слоя: `data`, `domain`, `ui`. Модуль единственный — весь код находится в модуле `app`.

```
com.meteohealth/
├── data/
│   ├── local/       — Room: сущности, DAO, AppDatabase (v5)
│   ├── remote/      — Retrofit: API, DTO, интерсепторы
│   └── repository/  — реализации интерфейсов репозиториев (включая LocationRepositoryImpl)
├── domain/
│   ├── model/       — доменные модели (UserProfile, DiaryEntry, Sensitivity, …)
│   ├── repository/  — интерфейсы репозиториев (включая LocationRepository)
│   ├── WellbeingCalculator.kt
│   └── TriggerAnalyzer.kt
├── ui/
│   ├── theme/       — цвета (включая SeverityGreen/Yellow/Red), типографика
│   ├── navigation/  — NavGraph, BottomNavBar, NavRoutes (+ SOURCES, PRIVACY)
│   ├── components/  — переиспользуемые компоненты (DisclaimerBanner, SeverityBadge, ConditionChip)
│   ├── onboarding/  — анкета (имя, возраст, чувствительность, заболевания)
│   ├── dashboard/   — главная (карточки погоды, Kp, самочувствия + светофор + иконки факторов)
│   ├── forecast/    — прогноз 5 дней + график wellbeing-таймлайна
│   ├── diary/       — записи + анализ триггеров
│   ├── recommendations/  — карточки советов с дисклеймером и источниками
│   └── settings/    — настройки + SourcesScreen + PrivacyScreen
├── di/              — Koin-модули
├── worker/          — WeatherSyncWorker (определяет тип события и канал уведомления)
└── notification/    — NotificationHelper (два канала + шаблоны WeatherEvent)
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

`WeatherSyncWorker` (WorkManager) запускается периодически раз в 3 часа. На каждом запуске:

1. Берёт координаты из профиля (`latitude`/`longitude`), при отсутствии — fallback Москва (55.75, 37.62).
2. Обновляет погоду и Kp.
3. Считает реальные ΔP_6h и ΔT_24h по таблице `weather_history`.
4. Вычисляет индекс через `WellbeingCalculator`.
5. Определяет тип события (`PRESSURE_DROP/RISE`, `GEOMAGNETIC_STORM`, `FROST`, `HEAT`, `GENERAL`) по пороговым значениям.
6. Уважает per-event тумблеры из профиля (`notifyPressureJump`, `notifyGeomagneticStorm`, `notifyFrost`, `notifyHeat`).
7. Отправляет уведомление через нужный канал (`wellbeing_info` IMPORTANCE_DEFAULT для общих сообщений, `wellbeing_urgent` IMPORTANCE_HIGH с вибрацией для бурь и экстремальной температуры).
8. Логирует отправку в `notification_log` с типом события и уровнем критичности.

## Геолокация

Реализация — `LocationRepositoryImpl` через `FusedLocationProviderClient` (`play-services-location`). Используется `PRIORITY_BALANCED_POWER_ACCURACY`. Запрашивается только `ACCESS_FINE_LOCATION` и `ACCESS_COARSE_LOCATION` (без `BACKGROUND_LOCATION` — фоновое отслеживание не нужно). Координаты сохраняются в профиль; при отказе или отсутствии разрешения остаётся fallback Москвы.

## Персонализация и ML

Приложение реализует два уровня персонализации:

1. **Статический** — `personalPenalty()` в `WellbeingCalculator` добавляет штраф к индексу в зависимости от хронических состояний из профиля (гипертония, мигрень, суставы, дыхание). Итоговый штраф умножается на коэффициент чувствительности `Sensitivity` из онбординга: `LIGHT`×1.0, `MODERATE`×1.3, `STRONG`×1.6.

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
