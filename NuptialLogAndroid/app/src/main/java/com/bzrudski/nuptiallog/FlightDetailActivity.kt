package com.bzrudski.nuptiallog

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bzrudski.nuptiallog.userinterface.FlightDetailAdapter

class FlightDetailActivity : AppCompatActivity() {

    companion object {
        private val LOG_TAG = FlightDetailActivity::class.java.simpleName
        const val FLIGHT_ID_EXTRA = "FLIGHT_ID"
    }

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: FlightDetailAdapter
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private var mFlightId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_detail)

        mFlightId = intent.getIntExtra(MainActivity.FLIGHT_ID_EXTRA, -1)

        if (mFlightId == -1){
            // Will display an alert
            finish()
        }

        mRecyclerView = findViewById(R.id.recycler_view)
        mAdapter = FlightDetailAdapter(this, mFlightId)
        mSwipeRefreshLayout = findViewById(R.id.swipe_container)

        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(this)

        mSwipeRefreshLayout.setOnRefreshListener(mAdapter::triggerFlightLoad)

        Log.d(LOG_TAG, "Finished setting up detail view")
        mSwipeRefreshLayout.isRefreshing = true
        mAdapter.triggerFlightLoad()
    }

    fun endRefreshing(){
        mSwipeRefreshLayout.isRefreshing = false
    }

    fun loadChangelog(){
        val intent = Intent(this, ChangelogActivity::class.java)
        intent.putExtra(FLIGHT_ID_EXTRA, mFlightId)
        startActivity(intent)
    }

    fun loadWeather(){
        val intent = Intent(this, WeatherActivity::class.java)
        intent.putExtra(FLIGHT_ID_EXTRA, mFlightId)
        startActivity(intent)
    }
}