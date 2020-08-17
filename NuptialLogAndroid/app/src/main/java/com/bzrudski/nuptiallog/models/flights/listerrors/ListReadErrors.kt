package com.bzrudski.nuptiallog.models.flights.listerrors

sealed class ListReadErrors {
    object NoResponse: ListReadErrors()

}