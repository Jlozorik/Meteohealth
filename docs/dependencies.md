# 3.1 Инструментальные средства и зависимости

## Среда разработки

| Инструмент | Версия | Назначение |
|---|---|---|
| Android Studio | Meerkat (2025) | IDE |
| Kotlin | 2.2.10 | Язык разработки |
| Gradle | 9.x | Система сборки |
| KSP | 2.2.10-1.0.x | Кодогенерация (Room) |
| minSdk | 26 (Android 8.0) | Минимальная поддерживаемая версия |
| targetSdk / compileSdk | 36 | Целевая версия |

## Основные зависимости

### UI

| Библиотека | Версия | Назначение |
|---|---|---|
| Jetpack Compose BOM | актуальная | Базовая платформа Compose |
| Material 3 | (из BOM) | Компоненты UI |
| Navigation Compose | (из BOM) | Навигация между экранами |
| Material Icons Extended | (из BOM) | Расширенный набор иконок |
| Vico compose-m3 | 2.1.2 | Графики (линейные, столбчатые) |

### Архитектура

| Библиотека | Версия | Назначение |
|---|---|---|
| Lifecycle ViewModel Compose | (Jetpack) | ViewModel + Compose-интеграция |
| Lifecycle Runtime KTX | (Jetpack) | collectAsStateWithLifecycle |
| Activity Compose | (Jetpack) | setContent, enableEdgeToEdge |

### База данных

| Библиотека | Версия | Назначение |
|---|---|---|
| Room Runtime | 2.7.1 | ORM, локальная SQLite БД |
| Room KTX | 2.7.1 | Coroutines и Flow расширения |
| Room Compiler (KSP) | 2.7.1 | Генерация DAO-реализаций |

### Сеть

| Библиотека | Версия | Назначение |
|---|---|---|
| Retrofit | 2.11.0 | HTTP-клиент, описание API |
| OkHttp | 4.12.0 | HTTP-движок, логирование |
| kotlinx.serialization JSON | 1.8.1 | Десериализация JSON |
| Retrofit kotlinx.serialization | (конвертер) | Интеграция сериализации с Retrofit |

### Внедрение зависимостей

| Библиотека | Версия | Назначение |
|---|---|---|
| Koin Android | 4.1.0 | DI-контейнер |
| Koin AndroidX Compose | 4.1.0 | `koinViewModel()` в Compose |
| Koin AndroidX WorkManager | 4.1.0 | `KoinWorkerFactory` |

### Фоновая работа и уведомления

| Библиотека | Версия | Назначение |
|---|---|---|
| WorkManager KTX | 2.10.1 | Периодические фоновые задачи |
| Core KTX | (Jetpack) | NotificationCompat, системные вызовы |

### Асинхронность

| Библиотека | Версия | Назначение |
|---|---|---|
| kotlinx.coroutines Android | (Jetpack) | Корутины, Dispatchers |

## Тестовые зависимости

| Библиотека | Версия | Назначение |
|---|---|---|
| JUnit 4 | 4.x | Фреймворк юнит-тестов |
| MockK | 1.14.2 | Мок-объекты для Kotlin |
| Turbine | 1.2.0 | Тестирование Flow |
| kotlinx.coroutines test | (Jetpack) | TestCoroutineDispatcher |
| AndroidX Test / Espresso | (BOM) | Инструментальные тесты |

## Внешние API

| API | URL | Ключ |
|---|---|---|
| OpenWeatherMap | api.openweathermap.org/data/2.5 | `OPEN_WEATHER_API_KEY` в `local.properties` |
| NOAA SWPC (Kp-индекс) | services.swpc.noaa.gov/products/noaa/planetary_k_index_1m.json | Не требуется |

API-ключ OpenWeatherMap передаётся через `BuildConfig.OPEN_WEATHER_API_KEY`, генерируемый из `local.properties` при сборке.
