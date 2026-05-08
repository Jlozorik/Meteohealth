# 3.5 Тестирование

## Подход

Четыре уровня тестов: штрафные стратегии, пайплайн/классификатор, редьюсеры MVI, сетевой слой с MockEngine. Инструментальные DAO-тесты — отдельно.

## Инструменты

| Инструмент | Назначение |
|---|---|
| JUnit 4 | Фреймворк запуска тестов |
| MockK 1.14.2 | Мок gateway-интерфейсов в use-case тестах |
| kotlinx.coroutines-test | runTest, UnconfinedTestDispatcher |
| Ktor Client Mock | MockEngine для тестов OpenWeatherService / NoaaSwpcService |

## Структура

```
app/src/test/java/com/meteohealth/
├── domain/
│   ├── wellbeing/
│   │   ├── PenaltyTest.kt           — 17 тестов (каждый Penalty по отдельности)
│   │   ├── WellbeingPipelineTest.kt — 5 тестов (fold, граничные значения)
│   │   └── RiskClassifierTest.kt    — 8 тестов (границы CALM/WATCH/ALERT/HIGH)
│   ├── triggers/
│   │   └── PearsonAnalyzerTest.kt   — 11 тестов (r≈1, r≈0, мало записей)
│   └── usecase/
│       ├── ObserveHomeUseCaseTest.kt
│       ├── RefreshNowUseCaseTest.kt
│       ├── AppendJournalEntryUseCaseTest.kt
│       └── AnalyseTriggersUseCaseTest.kt
├── network/
│   └── OpenWeatherServiceTest.kt    — 3 теста (MockEngine: текущая погода, прогноз, 401)
└── feature/
    ├── HomeReducerTest.kt           — 4 теста
    ├── ForecastReducerTest.kt       — 5 тестов
    ├── JournalReducerTest.kt        — 5 тестов
    ├── SettingsReducerTest.kt       — 6 тестов
    └── OnboardingReducerTest.kt     — 6 тестов
```

## Уровень 1: Penalty-стратегии

`PenaltyTest` проверяет каждую из 5 стратегий изолированно:

- Нулевые входы → штраф 0.
- Точки перегиба формулы (clamp start/end).
- Максимальный штраф не превышает объявленный `max`.

## Уровень 2: WellbeingPipeline + RiskClassifier

`WellbeingPipelineTest`:

- Идеальные условия → score = 100.
- Все штрафы максимальны → score ≥ 0 (не уходит ниже нуля).
- Пустой список стратегий → score = 100.

`RiskClassifierTest`:

- Граничные значения 80, 79, 60, 59, 40, 39, 0 → правильный `RiskLevel`.

## Уровень 3: Редьюсеры MVI

Каждый редьюсер — чистая функция, тестируется без ViewModel:

```kotlin
val (newState, effects) = HomeReducer.reduce(initialState, HomeIntent.Refresh)
assertEquals(true, newState.isLoading)
assertEquals(emptyList(), effects)
```

Покрытие: все ветки `when (intent)`, граничные переходы шагов онбординга (0↔1↔3), toggle-поведение.

## Уровень 4: Сетевой слой

`OpenWeatherServiceTest` использует `MockEngine`:

```kotlin
val engine = MockEngineFactory.buildMockEngine()
val client = HttpClientFactory.owm(engine, "test-key")
val service = OpenWeatherService(client)
val result = service.current(55.75, 37.62)
assertEquals("Moscow", result.name)
```

Тесты: успешный ответ `current`, успешный ответ `forecast`, HTTP 401 → выброс `ClientRequestException`.

## Use-case тесты (MockK)

```kotlin
val gateway = mockk<JournalGateway>()
coEvery { gateway.observeAll() } returns flowOf(emptyList())
val useCase = AppendJournalEntryUseCase(gateway)
// ...
coVerify { gateway.append(any()) }
```

Gateway-интерфейсы мокируются через MockK; тесты проверяют оркестрацию, а не реализацию хранилища.

## Запуск

```bash
./gradlew :app:testDebugUnitTest
```

Отчёт: `app/build/reports/tests/testDebugUnitTest/index.html`

## Покрытие

| Компонент | Подход |
|---|---|
| Penalty × 5 | Юнит, исчерпывающий |
| WellbeingPipeline, RiskClassifier | Юнит, граничные значения |
| PearsonAnalyzer | Юнит, математическая корректность |
| Use-cases | MockK fake gateways |
| Редьюсеры × 5 | Юнит, все ветки intent |
| OpenWeatherService, NoaaSwpcService | Ktor MockEngine |
| DAO | Инструментальные (Room in-memory) |
