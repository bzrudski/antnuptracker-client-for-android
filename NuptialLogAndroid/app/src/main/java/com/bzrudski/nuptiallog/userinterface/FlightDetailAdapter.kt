package com.bzrudski.nuptiallog.userinterface

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bzrudski.nuptiallog.FlightDetailActivity
import com.bzrudski.nuptiallog.R
import com.bzrudski.nuptiallog.UserActivity
import com.bzrudski.nuptiallog.management.FlightAppManager
import com.bzrudski.nuptiallog.models.flights.Comment
import com.bzrudski.nuptiallog.models.flights.Flight
import com.bzrudski.nuptiallog.models.flights.Flight.FlightRowModifiers
import com.bzrudski.nuptiallog.models.flights.FlightDetailFetcher
import com.bzrudski.nuptiallog.models.table.Row
import com.bzrudski.nuptiallog.models.table.RowModifier
import com.bzrudski.nuptiallog.models.table.Table
import com.bzrudski.nuptiallog.models.users.Role
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class FlightDetailAdapter(activity: FlightDetailActivity, flightID: Int): RecyclerView.Adapter<FlightDetailAdapter.RowViewHolder>(), FlightDetailFetcher.DetailFetchObserver {

    companion object {
        val LOG_TAG = FlightDetailAdapter::class.java.simpleName
    }

    private val mActivity = activity
    private val mContext = activity as Context
    private val mInflater = LayoutInflater.from(mContext)
    private val mFlightId = flightID
    private var mDataSource: Table<FlightRowModifiers> = Table()

    init {
        FlightDetailFetcher.observer = this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {


        val modifier =  FlightRowModifiers.values()[viewType]
        val itemView: View

        return when (modifier){
            FlightRowModifiers.TEXT, FlightRowModifiers.TAXONOMY -> {
                itemView = mInflater.inflate(R.layout.label_detail_cell, parent, false)
                LabelTextViewHolder(itemView)
            }
            FlightRowModifiers.LOCATION -> {
                itemView = mInflater.inflate(R.layout.location_cell, parent, false)
                LocationViewHolder(itemView)
            }
            FlightRowModifiers.WEATHER, FlightRowModifiers.CHANGELOG, FlightRowModifiers.LABEL_ONLY, FlightRowModifiers.LABEL_ONLY_NO_DETAILS -> {
                itemView = mInflater.inflate(R.layout.label_only_cell, parent, false)

                val onClickListener = when (modifier) {
                    FlightRowModifiers.LABEL_ONLY_NO_DETAILS -> {view: View -> }
                    FlightRowModifiers.WEATHER -> {view: View -> mActivity.loadWeather()}
                    FlightRowModifiers.CHANGELOG -> {view: View -> mActivity.loadChangelog()}
                    FlightRowModifiers.LABEL_ONLY -> {view: View -> }
                    else -> {view: View -> }
                }

                LabelOnlyViewHolder(itemView, onClickListener)
            }
            FlightRowModifiers.USER -> {
                itemView = mInflater.inflate(R.layout.user_cell, parent, false)
                UserCellViewHolder(itemView)
            }
            FlightRowModifiers.COMMENT -> {
                itemView = mInflater.inflate(R.layout.comment_cell, parent, false)
                CommentViewHolder(itemView)
            }
            FlightRowModifiers.IMAGE -> {
                itemView = mInflater.inflate(R.layout.image_cell, parent, false)
                ImageViewHolder(itemView)
            }
            FlightRowModifiers.HEADER -> {
                Log.d(LOG_TAG, "Preparing a header cell")
                itemView = mInflater.inflate(R.layout.flight_detail_header_cell, parent, false)
                HeaderViewHolder(itemView)
            }
        }
    }

    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        holder.prepareForRow(mDataSource[position])
    }

    override fun getItemViewType(position: Int): Int {

        val modifier = mDataSource[position].modifier

        return modifier.ordinal
    }

    abstract class RowViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun prepareForRow(row: Row<Any, FlightRowModifiers>)
    }

    inner class LabelTextViewHolder(itemView: View) : RowViewHolder(itemView){
        private val mHeaderLabel:TextView = itemView.findViewById(R.id.cell_label)
        private val mContentLabel: TextView = itemView.findViewById(R.id.cell_content)

        override fun prepareForRow(row: Row<Any, FlightRowModifiers>) {
            val header = mContext.getString(row.headerId)
            val content = when {
                row.isStringResource -> {
                    mContext.getString(row.content as Int)
                }
                row.content is String -> {
                    row.content
                }
                row.content is Date -> {
                    FlightAppManager.dateFormatter.format(row.content)
                }
                else -> {
                    row.content.toString()
                }
            }

            mHeaderLabel.text = header
            mContentLabel.text = content

            if (row.modifier == FlightRowModifiers.TAXONOMY){
                mContentLabel.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            }
        }
    }

    inner class LocationViewHolder(itemView: View): RowViewHolder(itemView),
        OnMapReadyCallback {

        // Add in mapview
        private val mapFragment = mActivity.supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        val fragmentTransaction = mActivity.supportFragmentManager.beginTransaction()

        var mMap: GoogleMap? = null

        var mLatitude: Double? = null
        var mLongitude: Double? = null
        var mRadius: Double? = null

        init {
            mapFragment.getMapAsync(this)
        }

        override fun prepareForRow(row: Row<Any, FlightRowModifiers>) {
            val coordinates = row.content as Triple<*, *, *>
            mLongitude = coordinates.first as Double
            mLatitude = coordinates.second as Double
            mRadius = coordinates.third as Double

            if (mMap != null) {
                mMap!!.addMarker(MarkerOptions().position(LatLng(mLatitude!!, mLongitude!!)))
                mMap!!.addCircle(CircleOptions().center(LatLng(mLatitude!!, mLongitude!!)).radius(mRadius!!*1000))
            }
        }

        override fun onMapReady(map: GoogleMap) {
            mMap = map

            if (mLatitude != null && mLongitude != null && mRadius != null){
                val location = LatLng(mLatitude!!, mLongitude!!)
                mMap!!.addMarker(MarkerOptions().position(location))
                mMap!!.addCircle(CircleOptions().center(location).radius(mRadius!!*1000))
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 9.0f))
            }
        }
    }

    inner class LabelOnlyViewHolder(itemView: View, onClickAction: ((View)-> Unit)?=null): RowViewHolder(itemView), View.OnClickListener{
        private val mHeaderLabel: TextView = itemView.findViewById(R.id.header_label)
        private var mOnClickAction: (view: View) -> Unit = if (onClickAction != null){
            itemView.setOnClickListener(this)
            onClickAction
        } else {
            {}
        }

        override fun prepareForRow(row: Row<Any, FlightRowModifiers>) {
            mHeaderLabel.text = mContext.getString(row.headerId)

            val triangleView: ImageView = itemView.findViewById(R.id.detail_image)

            if (row.modifier == FlightRowModifiers.LABEL_ONLY_NO_DETAILS){
                triangleView.visibility = View.INVISIBLE
            } else {
                triangleView.visibility = View.VISIBLE
            }
        }

        override fun onClick(view: View) {
            mOnClickAction(view)
        }
    }

    inner class UserCellViewHolder(itemView: View): RowViewHolder(itemView){
        private val mHeaderLabel: TextView = itemView.findViewById(R.id.header_label)
        private val mUsernameLabel: TextView = itemView.findViewById(R.id.username_label)

        override fun prepareForRow(row: Row<Any, FlightRowModifiers>) {
            mHeaderLabel.text = mContext.getString(row.headerId)
            val userInfo = row.content as Pair<*, *>
            val username = userInfo.first as String
            val userRole = userInfo.second as Role

            mUsernameLabel.text = username
            mUsernameLabel.setOnClickListener {
                val usernameOfInterest = (it as TextView).text.toString()
                val intent = Intent(mContext, UserActivity::class.java)
                intent.putExtra(UserActivity.USERNAME_EXTRA, usernameOfInterest)
                mContext.startActivity(intent)
            }

            when (userRole){
                Role.FLAGGED -> mUsernameLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_antredsmall,
                    0
                )
                Role.CITIZEN -> mUsernameLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    0,
                    0,
                    0
                )
                Role.MYRMECOLOGIST -> mUsernameLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_antbluesmall,
                    0
                )
            }

        }
    }

    inner class CommentViewHolder(itemView: View): RowViewHolder(itemView){
        private val mCommentBody: TextView = itemView.findViewById(R.id.comment_body)
        private val mCommentAuthor: TextView = itemView.findViewById(R.id.comment_author)
        private val mCommentDate: TextView = itemView.findViewById(R.id.comment_date)

        override fun prepareForRow(row: Row<Any, FlightRowModifiers>) {
            val comment = row.content as Comment

            mCommentBody.text = comment.text
            mCommentAuthor.text = comment.author
            mCommentDate.text = SimpleDateFormat.getDateTimeInstance().format(comment.time)

            when (comment.role) {
                Role.FLAGGED -> mCommentAuthor.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_antredsmall,
                    0
                )
                Role.CITIZEN -> mCommentAuthor.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    0,
                    0,
                    0
                )
                Role.MYRMECOLOGIST -> mCommentAuthor.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_antbluesmall,
                    0
                )
            }
        }
    }

    inner class HeaderViewHolder(itemView: View): RowViewHolder(itemView){
        private val mHeaderLabel: TextView = itemView.findViewById(R.id.header_title_label)

        override fun prepareForRow(row: Row<Any, FlightRowModifiers>) {
            mHeaderLabel.text = mContext.getText(row.headerId)
        }
    }

    inner class ImageViewHolder(itemView: View): RowViewHolder(itemView){
        private val mImageView: ImageView = itemView.findViewById(R.id.image_view)

        override fun prepareForRow(row: Row<Any, FlightRowModifiers>) {
            val imageUrl = row.content as URL

            Glide.with(mContext).load(Uri.parse(imageUrl.toString())).into(mImageView)
        }
    }

    fun triggerFlightLoad(){
        FlightDetailFetcher.getFlightById(mFlightId)
    }

    override fun fetchedFlightDetail(flight: Flight) {
        Log.d(LOG_TAG, "Successfully got flight detail")
        mDataSource = flight.toTable()

        mActivity.runOnUiThread {
            notifyDataSetChanged()
            (mActivity as? FlightDetailActivity)?.endRefreshing()
        }
    }

    override fun fetchedFlightDetailWithError(
        id: Int,
        error: FlightDetailFetcher.FetchDetailError
    ) {
        Log.d(LOG_TAG, "An error occurred fetching the flight details.")
        when (error){
            FlightDetailFetcher.FetchDetailError.NoFlight -> Log.d(LOG_TAG, "No flight found with that id")
            FlightDetailFetcher.FetchDetailError.AuthError -> Log.d(LOG_TAG, "Authentication Error")
            is FlightDetailFetcher.FetchDetailError.FetchError -> Log.d(LOG_TAG, "Error fetching the flight list (${error.status}).")
            FlightDetailFetcher.FetchDetailError.JsonError -> Log.d(LOG_TAG, "Error parsing flight details")
            FlightDetailFetcher.FetchDetailError.NoResponse -> Log.d(LOG_TAG, "No response from server fetching flight detail")
        }

        mActivity.runOnUiThread {
            (mActivity as? FlightDetailActivity)?.endRefreshing()
        }
    }
}