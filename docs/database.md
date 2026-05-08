# 2.2 База данных

## Технология

Локальное хранилище — **Room 2.7.1** с кодогенерацией через **KSP**. Единственная база данных: `AppDatabase` (`meteohealth_v2.db`, версия 1, схема экспортируется в `/schemas`).

## Почему v2

Предыдущая схема хранила профиль одной «fat»-строкой и держала кэш погоды/Kp в singleton-таблицах без внешних ключей. В v2:

- Профиль разбит на 5 таблиц (профиль, здоровье, локация, настройки уведомлений, отображение) — чище миграции, нет широких обновлений.
- Симптомы дневника вынесены в `journal_symptom` + связь `journal_entry_symptom` (FK, CASCADE DELETE) — нет сериализации списка в TEXT.
- Метрики погоды на момент записи хранятся в `journal_entry_metric` (FK, CASCADE DELETE) — нет nullable-столбцов в основной таблице.
- `weather_hour` использует `hourBucketEpoch` как PK — дедупликация по часу без autoincrement.
- Нет миграций: база новая (`meteohealth_v2.db`), схема версии 1.

## Таблицы

### profile

| Столбец | Тип | Описание |
|---|---|---|
| id | INTEGER PK | Всегда 1 |
| name | TEXT | Имя пользователя |
| age | INTEGER? | Возраст (опционально) |
| sensitivity | INTEGER | 1–5, множитель PersonalPenalty |

### profile_health_condition

| Столбец | Тип | Описание |
|---|---|---|
| profileId | INTEGER (FK → profile) | |
| condition | TEXT | HYPERTENSION / MIGRAINE / JOINT_PAIN / ASTHMA / DIABETES / HEART_DISEASE |

### profile_location

| Столбец | Тип | Описание |
|---|---|---|
| profileId | INTEGER (FK → profile) | |
| cityName | TEXT | Название города |
| latitude | REAL? | Широта |
| longitude | REAL? | Долгота |

### profile_notification_pref

| Столбец | Тип | Описание |
|---|---|---|
| profileId | INTEGER (FK → profile) | |
| enabled | INTEGER (Boolean) | Главный тумблер |
| thresholdScore | INTEGER | Порог индекса для уведомления |
| types | TEXT | JSON-список типов уведомлений |

### profile_display_pref

| Столбец | Тип | Описание |
|---|---|---|
| profileId | INTEGER (FK → profile) | |
| pressureUnit | TEXT | HPA / MMHG |

### journal_entry

| Столбец | Тип | Описание |
|---|---|---|
| id | INTEGER PK autoGenerate | |
| createdAt | INTEGER (Instant → Long) | Время записи |
| wellbeingScore | INTEGER | 0–100 |
| notes | TEXT | Произвольные заметки |

### journal_symptom

| Столбец | Тип | Описание |
|---|---|---|
| id | INTEGER PK autoGenerate | |
| name | TEXT (Unique) | HEADACHE / FATIGUE / PRESSURE / … |

### journal_entry_symptom

| Столбец | Тип | Описание |
|---|---|---|
| entryId | INTEGER (FK → journal_entry CASCADE) | |
| symptomId | INTEGER (FK → journal_symptom CASCADE) | |

### journal_entry_metric

| Столбец | Тип | Описание |
|---|---|---|
| entryId | INTEGER (FK → journal_entry CASCADE) | |
| key | TEXT | pressure_hpa / temp_c / kp / humidity |
| value | REAL | Значение показателя |

### weather_hour

Кэш почасовых данных OWM.

| Столбец | Тип | Описание |
|---|---|---|
| hourBucketEpoch | INTEGER PK | Unix-время, округлённое до часа |
| tempC | REAL | Температура, °C |
| pressureHpa | REAL | Давление, гПа |
| humidity | INTEGER | Влажность, % |
| windMs | REAL | Скорость ветра, м/с |
| description | TEXT | Описание OWM |
| icon | TEXT | Код иконки OWM |

Записи старше 7 дней удаляются при каждом обновлении.

### kp_minute

| Столбец | Тип | Описание |
|---|---|---|
| epochSecond | INTEGER PK | Unix-время замера |
| kp | REAL | Kp-индекс (0–9) |

Записи старше 24 часов удаляются при каждом обновлении.

### notification_log

| Столбец | Тип | Описание |
|---|---|---|
| id | INTEGER PK autoGenerate | |
| sentAt | INTEGER (Instant → Long) | Время отправки |
| riskLevel | TEXT | CALM / WATCH / ALERT / HIGH |
| score | INTEGER | Индекс при отправке |

## DAO

| DAO | Основные операции |
|---|---|
| ProfileDao | observe(), upsertProfile(), upsertLocation(), conditions, notifPrefs, displayPref |
| JournalDao | observeAll(), upsertEntry(): Long, getSymptomsForEntry(), getMetricsForEntry() |
| WeatherDao | upsertHour(), observeLatest(), getHistory(), deleteOlderThan() |
| KpDao | upsertMinute(), observeLatest(), deleteOlderThan() |
| NotificationLogDao | insert(), observeRecent() |

## Конвертеры

`Converters.kt` — `@TypeConverter` для `Instant ↔ Long` (kotlinx-datetime).

## Offline-first

Gateway-реализации всегда эмитят данные из Room немедленно. Сетевые запросы обновляют базу в фоне. При ошибке сети UI продолжает видеть кэш.
