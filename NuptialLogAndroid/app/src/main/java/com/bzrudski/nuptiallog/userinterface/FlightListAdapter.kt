package com.bzrudski.nuptiallog.userinterface

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bzrudski.nuptiallog.FlightDetailActivity
import com.bzrudski.nuptiallog.MainActivity
import com.bzrudski.nuptiallog.R
import com.bzrudski.nuptiallog.UserActivity
import com.bzrudski.nuptiallog.management.FlightAppManager
import com.bzrudski.nuptiallog.models.flights.FlightBarebones
import com.bzrudski.nuptiallog.models.flights.FlightList
import com.bzrudski.nuptiallog.models.users.Role
import java.text.SimpleDateFormat

class FlightListAdapter(context: Context): RecyclerView.Adapter<FlightListAdapter.FlightViewHolder>() {

    companion object {
        val LOG_TAG = FlightListAdapter::class.java.simpleName
    }

    private val mInflater = LayoutInflater.from(context)
    private val mContext = context

    inner class FlightViewHolder(itemView: View, adapter: FlightListAdapter):RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val taxonomyLabel: TextView = itemView.findViewById(R.id.taxonomy_label)
        private val locationLabel: TextView = itemView.findViewById(R.id.location_label)
        private val dateLabel: TextView = itemView.findViewById(R.id.date_label)
        private val authorLabel: TextView = itemView.findViewById(R.id.author_label)
        private val verifiedImage: ImageView = itemView.findViewById(R.id.verified_image)

        init {
            itemView.setOnClickListener(this)
        }

        fun prepareForFlight(flight: FlightBarebones) {
            taxonomyLabel.text = flight.taxonomy.toString()
            locationLabel.text = flight.locationString
            dateLabel.text = SimpleDateFormat.getDateTimeInstance().format(flight.date)
            authorLabel.text = flight.owner

            if (flight.validated) {
                verifiedImage.visibility = View.VISIBLE
                Glide.with(itemView.context).load(R.drawable.ic_antgreensmall).into(verifiedImage)
            } else {
                verifiedImage.visibility = View.INVISIBLE
            }

            when (flight.ownerRole) {
                Role.FLAGGED -> {
                    verifiedImage.visibility = View.VISIBLE
                    Glide.with(itemView.context).load(R.drawable.ic_antredsmall).into(verifiedImage)
                    authorLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_antredsmall, 0, 0, 0)
                }

                Role.MYRMECOLOGIST -> {
                    authorLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_antbluesmall, 0, 0, 0)
                }

                Role.CITIZEN -> {
                    authorLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)
                }
            }

            authorLabel.setOnClickListener {
                val username = (it as TextView).text.toString()
                val intent = Intent(mContext, UserActivity::class.java)
                intent.putExtra(UserActivity.USERNAME_EXTRA, username)
                mContext.startActivity(intent)
            }
        }

        override fun onClick(view: View) {
            Log.d(LOG_TAG, "Clicked on flight at $adapterPosition")
            val selectedFlight = FlightList[adapterPosition]
            val flightID = selectedFlight.flightID
            val detailIntent = Intent(mContext, FlightDetailActivity::class.java)
            detailIntent.putExtra(MainActivity.FLIGHT_ID_EXTRA, flightID)
            mContext.startActivity(detailIntent)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val itemView = mInflater.inflate(R.layout.flight_cell, parent, false)

        return FlightViewHolder(itemView, this)
    }

    override fun getItemCount(): Int = FlightList.length

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val flight = FlightList.flights[position]

        holder.prepareForFlight(flight)
    }
}