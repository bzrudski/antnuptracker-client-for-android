package com.bzrudski.nuptiallog.models.flights

import android.util.Log
import com.bzrudski.nuptiallog.WebInt
import com.bzrudski.nuptiallog.management.FlightAppManager
import com.bzrudski.nuptiallog.management.UrlManager
import com.bzrudski.nuptiallog.models.flights.observers.FlightReadObserver
import com.google.gson.JsonSyntaxException
import java.net.URL

object FlightList : Iterable<FlightBarebones> {

    // Define underlying list of Flights
    var flights: ArrayList<FlightBarebones> = ArrayList()
    val length: Int get() = this.flights.size

    val totalCount: Int get() = currentFrame?.count ?: 0

    // Observers
    var readObserver: FlightReadObserver? = null

    private val LOG_TAG = FlightList::class.java.simpleName

    private var currentFrame: FlightBarebonesFrame? = null

    // Return iterator to make the FlightList iterable
    override fun iterator(): Iterator<FlightBarebones> {
        return this.flights.iterator()
    }

    // Indexing
    operator fun get(i: Int): FlightBarebones {
        return flights[i]
    }

    private operator fun set(i: Int, f:FlightBarebones){
        flights[i] = f
    }

    //region List Updating
    private enum class ListEnd{
        START, END
    }

    private enum class ListAddOrdering{
        FORWARD, REVERSED
    }

    private fun updateFlights(newFlights: ArrayList<FlightBarebones>, end: ListEnd, ordering: ListAddOrdering):Int{

        val flightsToAdd = when (ordering){
            ListAddOrdering.FORWARD -> newFlights
            ListAddOrdering.REVERSED -> newFlights.reversed()
        }

        var numberAdded = 0


        for (flight in flightsToAdd) {
            val flightIndex = flights.indexOfFirst { it.flightID == flight.flightID }

            if (flightIndex == -1 ){
                val indexToAddAt = when (end){
                    ListEnd.START -> 0
                    ListEnd.END -> length
                }

                flights.add(indexToAddAt, flight)
                numberAdded += 1

            } else {
                flights[flightIndex] = flight
            }
        }

        return numberAdded
    }

    //endregion List Updating

    //region List methods

    //region List Reading
    sealed class ListReadErrors {
        object NoResponse : ListReadErrors()
        object JsonError : ListReadErrors()
        object AuthError : ListReadErrors()
        data class ReadError(val status: Int) : ListReadErrors()
    }

    fun read() {
        Thread {
            WebInt.request(
                WebInt.HttpMethods.GET,
                UrlManager.listURL(),
                callback = { status, responseData ->

                    if (status == 401) {
                        readObserver?.flightListReadMoreWithError(ListReadErrors.AuthError)
                        return@request
                    }

                    if (status != 200) {
                        Log.d(LOG_TAG, "Error reading flight list ($status)")
                        readObserver?.flightListReadWithError(ListReadErrors.ReadError(status))
                        return@request
                    }

                    val newFrame: FlightBarebonesFrame

                    try {
                        newFrame = FlightAppManager.gson.fromJson(
                            responseData,
                            FlightBarebonesFrame::class.java
                        )
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                        readObserver?.flightListReadWithError(ListReadErrors.JsonError)
                        return@request
                    }

                    currentFrame = newFrame
                    flights.addAll(0, newFrame.results)

                    Log.d(LOG_TAG, "Read flights. There are now $length flights.")

                    readObserver?.flightListRead()

                },
                errorHandler = { _ ->
                    readObserver?.flightListReadWithError(ListReadErrors.NoResponse)
                })
        }.start()

    }

    fun readMore(offsetFrame: FlightBarebonesFrame? = null) {

        val frameToRead = offsetFrame ?: currentFrame ?: return

        val next = frameToRead.next ?: return

        Thread {
            WebInt.request(WebInt.HttpMethods.GET, next, callback = { status, responseData ->

                if (status == 401) {
                    readObserver?.flightListReadMoreWithError(ListReadErrors.AuthError)
                    return@request
                }

                if (status != 200) {
                    readObserver?.flightListReadMoreWithError(ListReadErrors.ReadError(status))
                    return@request
                }

                try {
                    val newFrame =
                        FlightAppManager.gson.fromJson(
                            responseData,
                            FlightBarebonesFrame::class.java
                        )
                    val numberRead =
                        updateFlights(newFrame.results, ListEnd.END, ListAddOrdering.FORWARD)
                    Log.d(LOG_TAG, "New flights read: $numberRead")
                    currentFrame = newFrame
                    readObserver?.flightListReadMore(numberRead)
                } catch (e: JsonSyntaxException) {
                    readObserver?.flightListReadMoreWithError(ListReadErrors.JsonError)
                }

            }, errorHandler = { _ ->
                readObserver?.flightListReadWithError(ListReadErrors.NoResponse)
            })
        }.start()

    }

    //endregion List Reading

    //region Getting New Flights

    sealed class GetNewFlightsErrors {
        object NoResponse: GetNewFlightsErrors()
        object JsonError: GetNewFlightsErrors()
        object AuthError: GetNewFlightsErrors()
        data class GetError(val status:Int): GetNewFlightsErrors()
    }

    fun getNewFlights(){
        if (currentFrame == null){
            read()
            return
        }

        getNewFlightsFrom(currentFrame!!, UrlManager.listURL())
    }

    private fun getNewFlightsFrom(startingFrame: FlightBarebonesFrame, startingUrl: URL) {
        val oldCount = startingFrame.count

        Thread {
            WebInt.request(WebInt.HttpMethods.GET, startingUrl, callback = {
                status, responseData ->

                if (status == 401) {
                    readObserver?.flightListGotNewFlightsWithError(GetNewFlightsErrors.AuthError)
                    return@request
                }

                if (status != 200) {
                    readObserver?.flightListGotNewFlightsWithError(GetNewFlightsErrors.GetError(status))
                    return@request
                }

                try {
                    val newFrame = FlightAppManager.gson.fromJson(responseData, FlightBarebonesFrame::class.java)
                    val newFlights = newFrame.results
                    val newCount = newFrame.count

                    if (newCount - oldCount > 15){
                        getNewFlightsFrom(newFrame, newFrame.next!!)
                    }

                    val n = updateFlights(newFlights, ListEnd.START, ListAddOrdering.REVERSED)
                    readObserver?.flightListGotNewFlights(n)
                } catch (e: JsonSyntaxException) {
                    readObserver?.flightListGotNewFlightsWithError(GetNewFlightsErrors.JsonError)
                    return@request
                }

            }, errorHandler = {
                readObserver?.flightListGotNewFlightsWithError(GetNewFlightsErrors.NoResponse)
            })
        }.start()

    }

    //endregion Getting New Flights

    /**
     * Add a new flight to the list. Interacts with the API to produce new flight on the server
     */
    fun add(flight: FlightBarebones) {

    }

    fun update(flight: FlightBarebones) {

    }

    //endregion
}