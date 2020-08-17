package com.bzrudski.nuptiallog.userinterface

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bzrudski.nuptiallog.R
import com.bzrudski.nuptiallog.WeatherActivity
import com.bzrudski.nuptiallog.models.flights.Weather
import com.bzrudski.nuptiallog.models.flights.WeatherFetcher
import com.bzrudski.nuptiallog.models.table.Row
import com.bzrudski.nuptiallog.models.table.Table
import java.text.SimpleDateFormat
import java.util.*

class FlightWeatherAdapter(activity: WeatherActivity, flightID: Int): RecyclerView.Adapter<FlightWeatherAdapter.ViewHolder>(), WeatherFetcher.WeatherFetchObserver {

    companion object {
        private val LOG_TAG = FlightWeatherAdapter::class.java.simpleName
    }

    private val mActivity = activity
    private val mFlightID = flightID
    private val mInflater = LayoutInflater.from(mActivity)

    private lateinit var mWeather: Weather
    private var mWeatherTable: Table<Weather.WeatherRowModifiers> = Table()

    init {
        WeatherFetcher.observer = this
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun prepareForRow(row: Row<Any, Weather.WeatherRowModifiers>)
    }

    inner class HeaderViewHolder(itemView: View): ViewHolder(itemView){
        private val headerTextView: TextView = itemView.findViewById(R.id.header_title_label)

        override fun prepareForRow(row: Row<Any, Weather.WeatherRowModifiers>) {
            headerTextView.text = mActivity.getText(row.headerId)
        }
    }

    inner class TextViewHolder(itemView: View): ViewHolder(itemView){
        private val headerTextView: TextView = itemView.findViewById(R.id.cell_label)
        private val contentTextView: TextView = itemView.findViewById(R.id.cell_content)

        override fun prepareForRow(row: Row<Any, Weather.WeatherRowModifiers>) {
            headerTextView.text = mActivity.getText(row.headerId)

            contentTextView.text = when {
                row.content is String -> row.content
                row.isStringResource -> mActivity.getText(row.content as Int)
                row.content is Date -> SimpleDateFormat.getDateTimeInstance().format(row.content)
                else -> row.content.toString()
            }
        }
    }

    inner class FooterViewHolder(itemView: View): ViewHolder(itemView){
        private val footerTextView: TextView = itemView.findViewById(R.id.footer_text_label)

        override fun prepareForRow(row: Row<Any, Weather.WeatherRowModifiers>) {
            footerTextView.text = mActivity.getString(row.headerId)
        }

    }

    override fun getItemViewType(position: Int): Int {

        return mWeatherTable[position].modifier.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view: View

        return when (Weather.WeatherRowModifiers.values()[viewType]){
            Weather.WeatherRowModifiers.TEXT -> {
                view = mInflater.inflate(R.layout.label_detail_cell, parent, false)
                TextViewHolder(view)
            }
            Weather.WeatherRowModifiers.HEADER -> {
                view = mInflater.inflate(R.layout.flight_detail_header_cell, parent, false)
                HeaderViewHolder(view)
            }
            Weather.WeatherRowModifiers.FOOTER -> {
                view = mInflater.inflate(R.layout.footer_cell, parent, false)
                FooterViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return mWeatherTable.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.prepareForRow(mWeatherTable[position])
    }

    fun triggerWeatherFetch(){
        WeatherFetcher.getWeatherForId(mFlightID)
    }

    override fun gotWeather(weather: Weather) {
        Log.d(LOG_TAG, "Successfully got weather for $mFlightID")
        mWeather = weather
        mWeatherTable = weather.toTable()

        mActivity.runOnUiThread{
            notifyDataSetChanged()
            mActivity.endRefreshing()
        }
    }

    override fun gotWeatherWithError(id: Int, error: WeatherFetcher.WeatherFetchError) {
        Log.d(LOG_TAG, "Error fetching weather for flight $id:")

        when (error){
            WeatherFetcher.WeatherFetchError.NoResponse -> Log.d(LOG_TAG, "No response")
            WeatherFetcher.WeatherFetchError.NoWeather -> Log.d(LOG_TAG, "No weather for flight")
            WeatherFetcher.WeatherFetchError.AuthError -> Log.d(LOG_TAG, "Authentication Error")
            WeatherFetcher.WeatherFetchError.JsonError -> Log.d(LOG_TAG, "JSON parse error")
            is WeatherFetcher.WeatherFetchError.WeatherError -> Log.d(LOG_TAG, "Weather fetch error (${error.status})")
        }

        mActivity.runOnUiThread {
            mActivity.endRefreshing()
        }
    }
}