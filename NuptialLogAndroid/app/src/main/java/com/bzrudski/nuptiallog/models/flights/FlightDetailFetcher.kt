package com.bzrudski.nuptiallog.models.flights

import com.bzrudski.nuptiallog.WebInt
import com.bzrudski.nuptiallog.management.FlightAppManager
import com.bzrudski.nuptiallog.management.UrlManager
import com.google.gson.JsonSyntaxException

object FlightDetailFetcher {

    sealed class FetchDetailError {
        object NoFlight: FetchDetailError()
        object AuthError: FetchDetailError()
        data class FetchError(val status:Int): FetchDetailError()
        object JsonError: FetchDetailError()
        object NoResponse: FetchDetailError()
    }

    interface DetailFetchObserver {
        fun fetchedFlightDetail(flight: Flight)
        fun fetchedFlightDetailWithError(id: Int, error: FetchDetailError)
    }

    var observer: DetailFetchObserver? = null

    fun getFlightById(id: Int) {
        val url = UrlManager.urlForFlight(id)

        Thread {
            WebInt.request(WebInt.HttpMethods.GET, url, callback = { status, responseData ->

                when (status) {
                    404 -> {
                        observer?.fetchedFlightDetailWithError(id, FetchDetailError.NoFlight)
                        return@request
                    }
                    401 -> {
                        observer?.fetchedFlightDetailWithError(id, FetchDetailError.AuthError)
                        return@request
                    }
                    200 -> {
                    }
                    else -> {
                        observer?.fetchedFlightDetailWithError(
                            id,
                            FetchDetailError.FetchError(status)
                        )
                        return@request
                    }
                }

                try {
                    val flight = FlightAppManager.gson.fromJson(responseData, Flight::class.java)
                    observer?.fetchedFlightDetail(flight)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                    observer?.fetchedFlightDetailWithError(id, FetchDetailError.JsonError)
                    return@request
                }

            }, errorHandler = {
                observer?.fetchedFlightDetailWithError(id, FetchDetailError.NoResponse)
            })
        }.start()
    }
}