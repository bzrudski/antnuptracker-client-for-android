package com.bzrudski.nuptiallog.models.flights
import com.bzrudski.nuptiallog.models.users.Role
import com.google.gson.annotations.SerializedName
import java.util.Date
import kotlin.math.abs

class FlightBarebones(val flightID: Int,
                      var taxonomy: Species,
                      @SerializedName("dateOfFlight") var date: Date,
                      var latitude: Double,
                      var longitude: Double,
                      var owner: String,
                      var ownerRole: Role,
                      var validated: Boolean
)
{

    val locationString: String
    get() {
        val latDir = if (latitude > 0) "N" else "S"
        val lonDir = if (longitude > 0) "E" else "W"

        val latString = "%.2f".format(abs(latitude))
        val lonString = "%.2f".format(abs(longitude))

        return "($latString\u00b0 $latDir, $lonString\u00b0 $lonDir)"
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FlightBarebones) return false

        if (flightID != other.flightID) return false
        if (taxonomy != other.taxonomy) return false
        if (date != other.date) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (owner != other.owner) return false
        if (ownerRole != other.ownerRole) return false
        if (validated != other.validated) return false


        return true
    }

    override fun hashCode(): Int {
        var result = flightID
        result = 31 * result + taxonomy.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + owner.hashCode()
        return result
    }
}