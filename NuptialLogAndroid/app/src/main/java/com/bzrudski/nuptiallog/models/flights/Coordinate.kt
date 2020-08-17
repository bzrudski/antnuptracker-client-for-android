package com.bzrudski.nuptiallog.models.flights

class Coordinate(val latitude:Double, val longitude:Double) {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Coordinate) return false
        return latitude == other.latitude && longitude == other.longitude
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        return result
    }
}