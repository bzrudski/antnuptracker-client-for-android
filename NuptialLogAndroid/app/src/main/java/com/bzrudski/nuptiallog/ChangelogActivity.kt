package com.bzrudski.nuptiallog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bzrudski.nuptiallog.userinterface.FlightChangelogAdapter

class ChangelogActivity : AppCompatActivity() {
    private var mFlightId = -1
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: FlightChangelogAdapter
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    companion object {
        private val LOG_TAG = ChangelogActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changelog)

        mFlightId = intent.getIntExtra(FlightDetailActivity.FLIGHT_ID_EXTRA, -1)

        if (mFlightId == -1) {
            // Display alert
            finish()
        }

        mRecyclerView = findViewById(R.id.recycler_view)
        mSwipeRefreshLayout = findViewById(R.id.swipe_container)

        mAdapter = FlightChangelogAdapter(this, mFlightId)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(this)

        mSwipeRefreshLayout.setOnRefreshListener(mAdapter::triggerChangelogFetch)

        Log.d(LOG_TAG, "Finished setting up changelog")
        mSwipeRefreshLayout.isRefreshing = true
        mAdapter.triggerChangelogFetch()
    }

    fun endRefreshing(){
        mSwipeRefreshLayout.isRefreshing = false
    }
}