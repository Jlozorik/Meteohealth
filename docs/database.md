# 2.2 База данных

## Технология

Локальное хранилище реализовано на **Room 2.7.1** с генерацией кода через **KSP** (Kotlin Symbol Processing). База данных — одна: `AppDatabase` (версия 1, схема экспортируется в `/schemas`).

## Таблицы

### user_profile

Хранит единственную строку с профилем пользователя (PK = 1).

| Столбец | Тип | Описание |
|---|---|---|
| id | INTEGER (PK) | Всегда 1 |
| name | TEXT | Имя пользователя |
| hasHypertension | INTEGER (Boolean) | Гипертония |
| hasMigraines | INTEGER (Boolean) | Мигрени |
| hasJointPain | INTEGER (Boolean) | Боли в суставах |
| hasRespiratoryIssues | INTEGER (Boolean) | Проблемы с дыханием |
| notificationsEnabled | INTEGER (Boolean) | Уведомления включены |
| notificationThreshold | INTEGER | Порог индекса (0–100) для уведомлений |
| onboardingCompleted | INTEGER (Boolean) | Онбординг пройден |

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

Лог отправленных уведомлений (для предотвращения дубликатов).

| Столбец | Тип | Описание |
|---|---|---|
| id | INTEGER (PK, autoGenerate) | |
| timestamp | INTEGER | Время отправки |
| wellbeingIndex | INTEGER | Индекс при отправке |

## Доступ к данным (DAO)

| DAO | Основные операции |
|---|---|
| UserProfileDao | upsert профиля, observe() как Flow |
| WeatherCacheDao | вставка, getLatest(), deleteOlderThan() |
| DiaryEntryDao | insert, delete, observeAll(), observeRange() |
| KpCacheDao | вставка, getLatest() |
| NotificationLogDao | insert, getLastSent() |

## Стратегия offline-first

Репозитории всегда читают из Room. Сетевые запросы запускаются в фоне и обновляют кэш. При ошибке сети UI получает устаревшие данные из БД, а не сообщение об ошибке.
