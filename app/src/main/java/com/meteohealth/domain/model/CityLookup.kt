package com.meteohealth.domain.model

data class CityCoords(val city: String, val lat: Double, val lon: Double)

object CityLookup {
    val MOSCOW = CityCoords("Москва", 55.7558, 37.6176)

    private val TABLE = listOf(
        MOSCOW,
        CityCoords("Санкт-Петербург",    59.9311, 30.3609),
        CityCoords("Екатеринбург",        56.8389, 60.6057),
        CityCoords("Новосибирск",         54.9885, 82.9207),
        CityCoords("Казань",              55.7879, 49.1221),
        CityCoords("Нижний Новгород",     56.2965, 43.9361),
        CityCoords("Челябинск",           55.1644, 61.4368),
        CityCoords("Самара",              53.2415, 50.1606),
        CityCoords("Уфа",                 54.7388, 55.9721),
        CityCoords("Ростов-на-Дону",      47.2357, 39.7015),
        CityCoords("Пермь",              58.0105, 56.2502),
        CityCoords("Красноярск",          56.0097, 92.8664),
        CityCoords("Воронеж",             51.6720, 39.1843),
        CityCoords("Краснодар",           45.0355, 38.9753),
        CityCoords("Саратов",             51.5330, 46.0341),
        CityCoords("Тюмень",              57.1522, 65.5272),
        CityCoords("Омск",                54.9885, 73.3242),
        CityCoords("Барнаул",             53.3606, 83.7636),
        CityCoords("Иркутск",             52.2978, 104.2964),
        CityCoords("Хабаровск",           48.4827, 135.0840),
        CityCoords("Владивосток",         43.1332, 131.9113),
        CityCoords("Новокузнецк",         53.7557, 87.1099),
        CityCoords("Кемерово",            55.3908, 86.0471),
        CityCoords("Рязань",              54.6269, 39.6916),
        CityCoords("Тула",                54.1961, 37.6182),
        CityCoords("Калининград",         54.7065, 20.5110),
    )

    /** Ищет по вхождению строки без учёта регистра. Дефолт — Москва. */
    fun resolve(input: String): CityCoords {
        val q = input.trim().lowercase()
        if (q.isEmpty()) return MOSCOW
        return TABLE.firstOrNull { it.city.lowercase().contains(q) || q.contains(it.city.lowercase()) }
            ?: MOSCOW
    }

    fun suggestions(input: String): List<CityCoords> {
        val q = input.trim().lowercase()
        if (q.length < 2) return emptyList()
        return TABLE.filter { it.city.lowercase().contains(q) }.take(5)
    }
}
