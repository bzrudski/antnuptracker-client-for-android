package com.bzrudski.nuptiallog.models.flights

import com.bzrudski.nuptiallog.R
import com.bzrudski.nuptiallog.models.table.Row
import com.bzrudski.nuptiallog.models.table.RowModifier
import com.bzrudski.nuptiallog.models.table.Table
import java.util.*

data class Weather(
    val flightID: Int,
    val weather: WeatherBasic,
    val description: WeatherDescription,
    val day: WeatherDay,
    val rain: WeatherRain? = null,
    val wind: WeatherWind? = null,
    val timeFetched: Date
){

    enum class WeatherRowModifiers: RowModifier {
        TEXT,
        HEADER,
        FOOTER
    }

    fun toTable(): Table<WeatherRowModifiers> {
        val table = Table<WeatherRowModifiers>()

        table.addRow(Row(R.string.weather_flight_info_header, "",
            WeatherRowModifiers.HEADER
        ))
        table.addRow(Row(R.string.flight_id, flightID,
            WeatherRowModifiers.TEXT
        ))
        table.extend(description.toTable())
        table.extend(weather.toTable())
        rain?.let {
            table.extend(it.toTable())
        }
        wind?.let{
            table.extend(it.toTable())
        }

        table.extend(day.toTable())
        table.addRow(Row(R.string.weather_time_retrieved, timeFetched,
            WeatherRowModifiers.TEXT
        ))

        table.addRow(Row(R.string.weather_footer, "",
            WeatherRowModifiers.FOOTER
        ))

        return table
    }

    data class WeatherDescription(
        val desc: String,
        val longDesc: String
    ) {

        fun toTable(): Table<WeatherRowModifiers>{
            val table = Table<WeatherRowModifiers>()

            table.addRow(Row(R.string.weather_description_header, "",
                WeatherRowModifiers.HEADER
            ))
            table.addRow(Row(R.string.weather_description, desc,
                WeatherRowModifiers.TEXT
            ))
            table.addRow(Row(R.string.weather_long_desc, longDesc,
                WeatherRowModifiers.TEXT
            ))

            return table
        }

    }

    data class WeatherBasic(
        val temperature: Double,
        val pressure: Double,
        val pressureSea: Double?,
        val pressureGround: Double?,
        val humidity: Int,
        val tempMin: Double?,
        val tempMax: Double?,
        val clouds: Int
    ) {

        fun toTable(): Table<WeatherRowModifiers> {
            val table = Table<WeatherRowModifiers>()

            table.addRow(Row(R.string.weather_details_header, "",
                WeatherRowModifiers.HEADER
            ))
            table.addRow(Row(R.string.weather_temperature, temperature,
                WeatherRowModifiers.TEXT
            ))
            table.addRow(Row(R.string.weather_pressure, pressure,
                WeatherRowModifiers.TEXT
            ))
            table.addRow(Row(R.string.weather_humidity, humidity,
                WeatherRowModifiers.TEXT
            ))

            if (tempMin != null) table.addRow(Row(R.string.weather_temp_min, tempMin,
                WeatherRowModifiers.TEXT
            ))
            if (tempMax != null) table.addRow(Row(R.string.weather_temp_max, tempMax,
                WeatherRowModifiers.TEXT
            ))

            table.addRow(Row(R.string.weather_clouds, clouds,
                WeatherRowModifiers.TEXT
            ))

            return table
        }

    }

    data class WeatherDay(
        val sunrise: Date,
        val sunset: Date
    ) {
        fun toTable(): Table<WeatherRowModifiers>{
            val table = Table<WeatherRowModifiers>()

            table.addRow(Row(R.string.weather_day_header, "",
                WeatherRowModifiers.HEADER
            ))
            table.addRow(Row(R.string.weather_sunrise, sunrise,
                WeatherRowModifiers.TEXT
            ))
            table.addRow(Row(R.string.weather_sunset, sunset,
                WeatherRowModifiers.TEXT
            ))

            return table
        }
    }

    data class WeatherRain(
        val rain1: Double?,
        val rain3: Double?
    ) {
        fun toTable(): Table<WeatherRowModifiers>{
            val table = Table<WeatherRowModifiers>()

            table.addRow(Row(R.string.weather_rain_header, "",
                WeatherRowModifiers.HEADER
            ))
            rain1?.let {
                table.addRow(Row(R.string.weather_rain1_header, it,
                    WeatherRowModifiers.TEXT
                ))
            }
            rain3?.let {
                table.addRow(Row(R.string.weather_rain3_header, it,
                    WeatherRowModifiers.TEXT
                ))
            }

            return table
        }
    }

    data class WeatherWind(
        val windSpeed: Double?,
        val windDegree: Int?
    ) {
        fun toTable(): Table<WeatherRowModifiers>{
            val table = Table<WeatherRowModifiers>()

            table.addRow(Row(R.string.weather_wind_header, "",
                WeatherRowModifiers.HEADER
            ))

            windSpeed?.let {
                table.addRow(Row(R.string.weather_wind_speed, it,
                    WeatherRowModifiers.TEXT
                ))
            }

            windDegree?.let {
                table.addRow(Row(R.string.weather_wind_degree, it,
                    WeatherRowModifiers.TEXT
                ))
            }

            return table
        }
    }
}