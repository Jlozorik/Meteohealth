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
| Jetpack Compose BOM | 2026.05.00 | Базовая платформа Compose |
| Material 3 | (из BOM) | Компоненты UI |
| Navigation Compose | (из BOM) | Навигация |
| Material Icons Extended | (из BOM) | Расширенный набор иконок |
| Vico compose-m3 | 2.1.2 | Графики (линейные) |

Глобально подключён `ExperimentalMaterial3ExpressiveApi` через `freeCompilerArgs` в `app/build.gradle.kts`.

### Архитектура

| Библиотека | Версия | Назначение |
|---|---|---|
| Lifecycle ViewModel Compose | (Jetpack) | ViewModel + Compose-интеграция |
| Lifecycle Runtime KTX | (Jetpack) | collectAsState |
| Activity Compose | (Jetpack) | setContent, enableEdgeToEdge |

### База данных

| Библиотека | Версия | Назначение |
|---|---|---|
| Room Runtime | 2.7.1 | ORM, локальная SQLite БД |
| Room KTX | 2.7.1 | Coroutines и Flow |
| Room Compiler (KSP) | 2.7.1 | Генерация DAO-реализаций |

### Сеть

| Библиотека | Версия | Назначение |
|---|---|---|
| Ktor Client Core | 3.1.3 | HTTP-клиент |
| Ktor Client OkHttp | 3.1.3 | OkHttp движок |
| Ktor Client Mock | 3.1.3 | MockEngine для demo-режима |
| Ktor Client Content Negotiation | 3.1.3 | JSON content negotiation |
| Ktor Serialization kotlinx-json | 3.1.3 | Десериализация JSON |
| kotlinx.serialization JSON | 1.8.1 | Сериализация |
| kotlinx-datetime | 0.6.1 | Работа с датами вместо java.time |

Retrofit и OkHttp как самостоятельные зависимости удалены.

### Внедрение зависимостей

| Библиотека | Версия | Назначение |
|---|---|---|
| Koin Android | 4.1.0 | DI-контейнер |
| Koin AndroidX Compose | 4.1.0 | `koinViewModel()` в Compose |

Koin AndroidX WorkManager удалён вместе с WorkManager.

### Фоновая работа

| Компонент | Тип | Назначение |
|---|---|---|
| WeatherTickService | Foreground Service | Обновление данных раз в час |
| TickReceiver | BroadcastReceiver | Планирование AlarmManager |
| BootReceiver | BroadcastReceiver | Восстановление будильника после перезагрузки |
| NotificationCenter | Object | Каналы SYNC (MIN) и ALERT (DEFAULT) |

WorkManager 2.10.1 удалён.

## Удалённые зависимости (относительно предыдущей версии)

| Библиотека | Причина удаления |
|---|---|
| Retrofit 2.11.0 | Заменён на Ktor |
| OkHttp (standalone) | OkHttp-движок идёт через Ktor |
| retrofit-kotlinx-serialization-converter | Не нужен при Ktor |
| WorkManager 2.10.1 | Заменён на AlarmManager + ForegroundService |
| DataStore Preferences | Профиль перенесён в Room v2 |
| Turbine 1.2.0 | Тесты Flow упрощены до прямых suspend-вызовов |

## Тестовые зависимости

| Библиотека | Версия | Назначение |
|---|---|---|
| JUnit 4 | 4.x | Фреймворк юнит-тестов |
| MockK | 1.14.2 | Мок-объекты для Kotlin |
| kotlinx.coroutines-test | (Jetpack) | runTest, TestCoroutineDispatcher |
| Ktor Client Mock | 3.1.3 | Тесты сетевого слоя |

## Внешние API

| API | URL | Ключ |
|---|---|---|
| OpenWeatherMap | api.openweathermap.org/data/2.5 | `OPEN_WEATHER_API_KEY` в `local.properties` |
| NOAA SWPC | services.swpc.noaa.gov/products/noaa/planetary_k_index_1m.json | Не требуется |

Demo-режим: при `BuildConfig.DEBUG && apiKey.isEmpty()` `HttpClientFactory` получает `MockEngineFactory.buildMockEngine()`.
