package com.bzrudski.nuptiallog.userinterface

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bzrudski.nuptiallog.ChangelogActivity
import com.bzrudski.nuptiallog.R
import com.bzrudski.nuptiallog.UserActivity
import com.bzrudski.nuptiallog.models.flights.Changelog
import com.bzrudski.nuptiallog.models.flights.ChangelogFetcher
import java.text.SimpleDateFormat

class FlightChangelogAdapter(activity: ChangelogActivity, flightID:Int): RecyclerView.Adapter<FlightChangelogAdapter.ChangelogViewHolder>(), ChangelogFetcher.ChangelogFetchObserver{

    companion object {
        val LOG_TAG = FlightChangelogAdapter::class.java.simpleName
    }

    private val mFlightID = flightID
    private val mContext = activity as Context
    private val mActivity = activity
    private val inflater = LayoutInflater.from(mContext)
    private var mChangelogList = ArrayList<Changelog>()

    init {
        ChangelogFetcher.observer = this
    }

    inner class ChangelogViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private var mEventLabel: TextView = itemView.findViewById(R.id.changelog_event_label)
        private var mAuthorLabel: TextView = itemView.findViewById(R.id.changelog_author_label)
        private var mDateLabel: TextView = itemView.findViewById(R.id.changelog_date_label)

        fun prepareForChangelog(changelog: Changelog){
            mEventLabel.text = changelog.event
            mAuthorLabel.text = changelog.user
            mDateLabel.text = SimpleDateFormat.getDateTimeInstance().format(changelog.date)

            mAuthorLabel.setOnClickListener {
                val username = (it as TextView).text.toString()
                val intent = Intent(mContext, UserActivity::class.java)
                intent.putExtra(UserActivity.USERNAME_EXTRA, username)
                mContext.startActivity(intent)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChangelogViewHolder {
        val view = inflater.inflate(R.layout.changelog_cell, parent, false)

        return ChangelogViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mChangelogList.size
    }

    override fun onBindViewHolder(holder: ChangelogViewHolder, position: Int) {
        holder.prepareForChangelog(mChangelogList[position])
    }

    fun triggerChangelogFetch(){
        ChangelogFetcher.getChangelogForId(mFlightID)
    }

    override fun gotChangelog(changelog: ArrayList<Changelog>) {
        Log.d(LOG_TAG, "Successfully got changelog for ID $mFlightID")
        mChangelogList.clear()
        mChangelogList.addAll(changelog)

        mActivity.runOnUiThread{
            notifyDataSetChanged()
            mActivity.endRefreshing()
        }
    }

    override fun gotChangelogWithError(id: Int, error: ChangelogFetcher.ChangelogFetchError) {
        Log.d(LOG_TAG, "Error occurred fetching changelog for ID $id")
        when (error){
            ChangelogFetcher.ChangelogFetchError.NoResponse -> Log.d(LOG_TAG, "No Response")
            ChangelogFetcher.ChangelogFetchError.AuthError -> Log.d(LOG_TAG, "Authentication Error")
            ChangelogFetcher.ChangelogFetchError.JsonError -> Log.d(LOG_TAG, "Json error")
            is ChangelogFetcher.ChangelogFetchError.ChangelogError -> Log.d(LOG_TAG, "Changelog Error (${error.status})")
        }
        mActivity.runOnUiThread(mActivity::endRefreshing)
    }
}