package com.bzrudski.nuptiallog.models.flights

import com.bumptech.glide.Glide
import com.bzrudski.nuptiallog.R
import com.bzrudski.nuptiallog.models.table.Row
import com.bzrudski.nuptiallog.models.table.RowModifier
import com.bzrudski.nuptiallog.models.table.Table
import com.bzrudski.nuptiallog.models.users.Role
import com.google.gson.annotations.SerializedName
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class Flight(
    val flightID:Int,
    val taxonomy: Species,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
    val dateOfFlight: Date,
    val dateRecorded: Date,
    val owner: String,
    val ownerRole: Role,
    @SerializedName("image") val imageUrl: URL?=null,
    val weather: Boolean=false,
    @SerializedName("confidence") val confidenceLevel: ConfidenceLevel,
    @SerializedName("size") val flightSize: FlightSize,
    val comments: ArrayList<Comment>,
    val validated: Boolean = false,
    val validatedBy: String? = null,
    val validatedAt: Date? = null
) {

    val locationString: String
        get() {
            val latDir = if (latitude > 0) "N" else "S"
            val lonDir = if (longitude > 0) "E" else "W"

            val latString = "%.2f".format(abs(latitude))
            val lonString = "%.2f".format(abs(longitude))

            return "($latString\u00b0 $latDir, $lonString\u00b0 $lonDir)"
        }

    val withinString: String get() = "$radius km"

    override fun equals(other: Any?): Boolean {
        if (other !is Flight) return false

        if (other.flightID != flightID) return false
        if (other.taxonomy != taxonomy) return false
        if (other.latitude != latitude) return false
        if (other.longitude != longitude) return false
        if (other.radius != radius) return false
        if (other.dateOfFlight != dateOfFlight) return false
        if (other.dateRecorded != dateRecorded) return false
        if (other.owner != owner) return false
        if (other.ownerRole != ownerRole) return false
        if (other.weather != weather) return false
        if (other.confidenceLevel != confidenceLevel) return false
        if (other.flightSize != flightSize) return false
        if (other.comments != comments) return false
        if (other.validated != validated) return false
        if (other.validatedBy != validatedBy) return false
        if (other.validatedAt?.equals(validatedAt) != true) return false
        if (other.imageUrl != imageUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = flightID
        result = 31 * result + taxonomy.hashCode()
        result = 31 * result + latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + radius.hashCode()
        result = 31 * result + dateOfFlight.hashCode()
        result = 31 * result + dateRecorded.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + ownerRole.hashCode()
        result = 31 * result + weather.hashCode()
        result = 31 * result + confidenceLevel.hashCode()
        result = 31 * result + flightSize.hashCode()
        result = 31 * result + comments.hashCode()
        result = 31 * result + validated.hashCode()
        result = 31 * result + (validatedBy?.hashCode() ?: 0)
        result = 31 * result + (validatedAt?.hashCode() ?: 0)
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        return result
    }

    enum class ConfidenceLevel(val rawValue:Int){

        @SerializedName("0")
        LOW(0),

        @SerializedName("1")
        HIGH(1);

        companion object {
            fun withRawValue(n: Int): ConfidenceLevel {
                return values().first { it.rawValue == n }
            }
        }

        fun getStringResource(): Int{
            return when (this){
                LOW -> R.string.low
                HIGH -> R.string.high
            }
        }
    }

    enum class FlightSize(val rawValue: Int){
        @SerializedName("0")
        MANY(0),
        @SerializedName("1")
        SINGLE(1);

        companion object {
            fun withRawValue(n: Int): FlightSize {
                return values().first { it.rawValue == n }
            }
        }

        fun getStringResource(): Int{
            return when (this){
                MANY -> R.string.many
                SINGLE -> R.string.single
            }
        }
    }

    enum class FlightRowModifiers: RowModifier{
        TEXT,
        LOCATION,
        WEATHER,
        USER,
        COMMENT,
        TAXONOMY,
        IMAGE,
        CHANGELOG,
        HEADER,
        LABEL_ONLY,
        LABEL_ONLY_NO_DETAILS
    }

    fun toTable(): Table<FlightRowModifiers> {
        val table: Table<FlightRowModifiers> = Table()

        // IMAGE SECTION
        imageUrl?.let {
            table.addRow(Row(-1, it, FlightRowModifiers.IMAGE))
        }

        // BASIC INFORMATION SECTION
        table.addRow(Row(R.string.basic_information_header, "", FlightRowModifiers.HEADER))
        table.addRow(Row(R.string.flight_id, flightID, FlightRowModifiers.TEXT))
        table.addRow(Row(R.string.genus, taxonomy.genus.name, FlightRowModifiers.TAXONOMY))
        table.addRow(Row(R.string.species, taxonomy.name, FlightRowModifiers.TAXONOMY))
        table.addRow(Row(R.string.species_confidence, confidenceLevel.getStringResource(), FlightRowModifiers.TEXT, true))
        table.addRow(Row(R.string.size_of_flight, flightSize.getStringResource(), FlightRowModifiers.TEXT, true))

        // DATE AND TIME OF FLIGHT SECTION
        table.addRow(Row(R.string.date_time_header, "", FlightRowModifiers.HEADER))
        table.addRow(Row(R.string.date_of_flight, SimpleDateFormat.getDateInstance().format(dateOfFlight), FlightRowModifiers.TEXT))
        table.addRow(Row(R.string.time_of_flight, SimpleDateFormat.getTimeInstance().format(dateOfFlight), FlightRowModifiers.TEXT))

        // LOCATION SECTION
        table.addRow(Row(R.string.location_header, "", FlightRowModifiers.HEADER))
        table.addRow(Row(-1, Triple(longitude, latitude, radius), FlightRowModifiers.LOCATION))
        table.addRow(Row(R.string.gps_coordinates, locationString, FlightRowModifiers.TEXT))
        table.addRow(Row(R.string.within, withinString, FlightRowModifiers.TEXT))

        // RECORDING SECTION
        table.addRow(Row(R.string.recording_header, "", FlightRowModifiers.HEADER))
        table.addRow(Row(R.string.recorded_by, Pair(owner, ownerRole), FlightRowModifiers.USER))
        table.addRow(Row(R.string.date_recorded, SimpleDateFormat.getDateInstance().format(dateRecorded), FlightRowModifiers.TEXT))
        table.addRow(Row(R.string.time_recorded, SimpleDateFormat.getTimeInstance().format(dateRecorded), FlightRowModifiers.TEXT))

        // WEATHER SECTION
        if (weather) {
            table.addRow(Row(R.string.weather_header, "", FlightRowModifiers.HEADER))
            table.addRow(Row(R.string.weather_for_flight, "", FlightRowModifiers.WEATHER))
        }

        // VALIDATION SECTION
        if (validated && validatedBy != null) {
            table.addRow(Row(R.string.validation_header, "", FlightRowModifiers.HEADER))
            table.addRow(Row(R.string.validated_by, Pair(validatedBy, Role.MYRMECOLOGIST), FlightRowModifiers.USER))
            table.addRow(Row(R.string.validated_time, SimpleDateFormat.getDateTimeInstance().format(validatedAt!!), FlightRowModifiers.TEXT))
        }

        // COMMENTS SECTION
        table.addRow(Row(R.string.comments_header, "", FlightRowModifiers.HEADER))

        if (comments.size > 0){
            for (comment in comments){
                table.addRow(Row(-1, comment, FlightRowModifiers.COMMENT))
            }
        } else {
            table.addRow(Row(R.string.no_comments, R.string.no_comments, FlightRowModifiers.LABEL_ONLY_NO_DETAILS))
        }

        // RECORD HISTORY SECTION
        table.addRow(Row(R.string.record_history_header, "", FlightRowModifiers.HEADER))
        table.addRow(Row(R.string.record_history, R.string.record_history, FlightRowModifiers.CHANGELOG))

        return table
    }
}