# 2.2 База данных

## Технология

Локальное хранилище реализовано на **Room 2.7.1** с генерацией кода через **KSP** (Kotlin Symbol Processing). База данных — одна: `AppDatabase` (версия 5, схема экспортируется в `/schemas`).

## Миграции

| Версия | Изменение |
|---|---|
| 1 → 2 | `user_profile.pressureUnit TEXT NOT NULL DEFAULT 'MMHG'` |
| 2 → 3 | `user_profile.isDarkTheme INTEGER NOT NULL DEFAULT 1` |
| 3 → 4 | новая таблица `weather_history` (timestamp PK, давление, температура, влажность) |
| 4 → 5 | расширение `user_profile` (возраст, чувствительность, типы уведомлений, координаты), типизация `notification_log` (eventType, severity) |

## Таблицы

### user_profile

Хранит единственную строку с профилем пользователя (PK = 1).

| Столбец | Тип | Описание |
|---|---|---|
| id | INTEGER (PK) | Всегда 1 |
| name | TEXT | Имя пользователя |
| age | INTEGER (nullable) | Возраст 18–90 (опционально) |
| sensitivity | TEXT | `LIGHT` / `MODERATE` (default) / `STRONG` — множитель чувствительности 1.0/1.3/1.6 |
| hasHypertension | INTEGER (Boolean) | Гипертония |
| hasMigraines | INTEGER (Boolean) | Мигрени |
| hasJointPain | INTEGER (Boolean) | Боли в суставах |
| hasRespiratoryIssues | INTEGER (Boolean) | Проблемы с дыханием |
| notificationsEnabled | INTEGER (Boolean) | Главный тумблер уведомлений |
| notificationThreshold | INTEGER | Порог индекса (0–100) для общего уведомления |
| notifyPressureJump | INTEGER (Boolean, default 1) | Уведомлять о скачках давления |
| notifyGeomagneticStorm | INTEGER (Boolean, default 1) | Уведомлять о магнитных бурях |
| notifyFrost | INTEGER (Boolean, default 1) | Уведомлять о морозе |
| notifyHeat | INTEGER (Boolean, default 1) | Уведомлять о жаре |
| onboardingCompleted | INTEGER (Boolean) | Онбординг пройден |
| pressureUnit | TEXT | `HPA` или `MMHG` (default: MMHG) |
| isDarkTheme | INTEGER (Boolean, default 1) | Тёмная тема включена |
| cityName | TEXT (nullable) | Город пользователя (fallback, если нет GPS) |
| latitude | REAL (nullable) | Широта (заполняется при «Определить автоматически») |
| longitude | REAL (nullable) | Долгота |

### weather_cache

Кэш текущей погоды от OpenWeatherMap.

| Столбец | Тип | Описание |
|---|---|---|
| id | INTEGER (PK, autoGenerate) | |
| cityName | TEXT | Название города |
| timestamp | INTEGER | Unix-время снимка |
| temperatureCelsius | REAL | Температура, °C |
| feelsLikeCelsius | REAL | Ощущаемая температура, °C |
| pressureHpa | REAL | Давление, гПа |
| humidity | INTEGER | Влажность, % |
| windSpeedMs | REAL | Скорость ветра, м/с |
| weatherDescription | TEXT | Текстовое описание |
| weatherIcon | TEXT | Код иконки OWM |

### weather_history

История замеров погоды для расчёта реальных дельт ΔP_6h и ΔT_24h.

| Столбец | Тип | Описание |
|---|---|---|
| timestamp | INTEGER (PK) | Unix-время замера |
| pressureHpa | REAL | Давление, гПа |
| temperatureCelsius | REAL | Температура, °C |
| humidity | INTEGER | Влажность, % |

Записи старше 7 дней удаляются при каждом обновлении.

### diary_entry

Записи дневника самочувствия.

| Столбец | Тип | Описание |
|---|---|---|
| id | INTEGER (PK, autoGenerate) | |
| timestamp | INTEGER | Unix-время записи |
| wellbeingLevel | TEXT | Enum: GREAT/GOOD/FAIR/POOR/TERRIBLE |
| symptoms | TEXT | Симптомы через запятую |
| notes | TEXT | Произвольные заметки |
| temperatureCelsius | REAL (nullable) | Температура на момент записи |
| pressureHpa | REAL (nullable) | Давление на момент записи |
| kpIndex | REAL (nullable) | Kp-индекс на момент записи |

### kp_cache

Кэш Kp-индекса геомагнитной активности (NOAA SWPC).

| Столбец | Тип | Описание |
|---|---|---|
| id | INTEGER (PK, autoGenerate) | |
| timestamp | INTEGER | Unix-время измерения |
| kpIndex | REAL | Значение Kp (0–9) |

### notification_log

Лог отправленных уведомлений (для дубликатов и аналитики).

| Столбец | Тип | Описание |
|---|---|---|
| id | INTEGER (PK, autoGenerate) | |
| timestamp | INTEGER | Время отправки |
| title | TEXT | Заголовок уведомления |
| body | TEXT | Текст уведомления |
| wellbeingIndex | INTEGER | Индекс при отправке |
| eventType | TEXT (default 'GENERAL') | `PRESSURE_DROP` / `PRESSURE_RISE` / `GEOMAGNETIC_STORM` / `FROST` / `HEAT` / `GENERAL` |
| severity | TEXT (default 'INFO') | `INFO` (канал `wellbeing_info`) или `URGENT` (`wellbeing_urgent`, IMPORTANCE_HIGH) |

## Доступ к данным (DAO)

| DAO | Основные операции |
|---|---|
| UserProfileDao | upsert, observe(), deleteAll() |
| WeatherCacheDao | вставка, getLatest(), deleteOlderThan() |
| WeatherHistoryDao | insert, getSince(from), deleteOlderThan |
| DiaryEntryDao | insert, delete, observeAll(), observeRange() |
| KpCacheDao | вставка, getLatest() |
| NotificationLogDao | insert, observeRecent(), deleteOlderThan |

## Стратегия offline-first

Репозитории всегда читают из Room. Сетевые запросы запускаются в фоне и обновляют кэш. При ошибке сети UI получает устаревшие данные из БД, а не сообщение об ошибке.
