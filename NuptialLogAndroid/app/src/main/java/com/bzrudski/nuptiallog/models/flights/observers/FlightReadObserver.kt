package com.bzrudski.nuptiallog.models.flights.observers

import com.bzrudski.nuptiallog.models.flights.FlightList

interface FlightReadObserver {

    //region Reading callbacks
    fun flightListRead()
    fun flightListReadWithError(error: FlightList.ListReadErrors)
    //endregion

    //region Reading more flights callbacks
    fun flightListReadMore(n: Int)
    fun flightListReadMoreWithError(error: FlightList.ListReadErrors)
    //endregion

    //region Get new flights callbacks
    fun flightListGotNewFlights(n: Int)
    fun flightListGotNewFlightsWithError(error: FlightList.GetNewFlightsErrors)
    //endregion

    //region Other callbacks
    fun flightListChanged()
    fun flightListCleared()
    //endregion
}