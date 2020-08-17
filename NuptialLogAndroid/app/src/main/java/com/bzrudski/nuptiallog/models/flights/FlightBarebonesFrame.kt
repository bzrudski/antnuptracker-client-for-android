package com.bzrudski.nuptiallog.models.flights

import java.net.URL

data class FlightBarebonesFrame (
    val count: Int,
    val previous: URL?,
    val next: URL?,
    val results: ArrayList<FlightBarebones>
)