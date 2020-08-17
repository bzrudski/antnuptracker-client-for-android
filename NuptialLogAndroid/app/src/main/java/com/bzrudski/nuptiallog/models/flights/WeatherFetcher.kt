package com.bzrudski.nuptiallog.models.flights

import com.bzrudski.nuptiallog.WebInt
import com.bzrudski.nuptiallog.management.FlightAppManager
import com.bzrudski.nuptiallog.management.UrlManager
import com.google.gson.JsonSyntaxException

object WeatherFetcher {

    var observer: WeatherFetchObserver? = null

    sealed class WeatherFetchError{
        object NoResponse: WeatherFetchError()
        object NoWeather: WeatherFetchError()
        object AuthError: WeatherFetchError()
        object JsonError: WeatherFetchError()
        data class WeatherError(val status:Int): WeatherFetchError()
    }

    interface WeatherFetchObserver {
        fun gotWeather(weather: Weather)
        fun gotWeatherWithError(id: Int, error: WeatherFetchError)
    }

    fun getWeatherForId(id: Int){
        val url = UrlManager.urlForWeather(id)

        Thread {
            WebInt.request(WebInt.HttpMethods.GET, url, callback = {
                status, responseData ->

                when (status) {
                    404 -> {
                        observer?.gotWeatherWithError(id, WeatherFetchError.NoWeather)
                        return@request
                    }
                    401 -> {
                        observer?.gotWeatherWithError(id, WeatherFetchError.AuthError)
                        return@request
                    }
                    200 -> {}
                    else -> {
                        observer?.gotWeatherWithError(id, WeatherFetchError.WeatherError(status))
                        return@request
                    }
                }

                try {
                    val weather = FlightAppManager.gson.fromJson(responseData, Weather::class.java)
                    observer?.gotWeather(weather)
                } catch (e: JsonSyntaxException) {
                    observer?.gotWeatherWithError(id, WeatherFetchError.JsonError)
                }

            }, errorHandler = {
                observer?.gotWeatherWithError(id, WeatherFetchError.NoResponse)
            })
        }.start()
    }
}