package com.bzrudski.nuptiallog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bzrudski.nuptiallog.userinterface.FlightWeatherAdapter

class WeatherActivity : AppCompatActivity() {

    companion object{
        private val LOG_TAG = WeatherActivity::class.java.simpleName
    }

    private var mFlightID = -1
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: FlightWeatherAdapter
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        mFlightID = intent.getIntExtra(FlightDetailActivity.FLIGHT_ID_EXTRA, -1)

        if (mFlightID == -1) {
            // SHOW ALERT
            finish()
        }

        mRecyclerView = findViewById(R.id.recycler_view)
        mAdapter = FlightWeatherAdapter(this, mFlightID)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(this)

        mSwipeRefreshLayout = findViewById(R.id.swipe_container)
        mSwipeRefreshLayout.setOnRefreshListener(mAdapter::triggerWeatherFetch)

        Log.d(LOG_TAG, "Finished setting up the weather activity")
        mSwipeRefreshLayout.isRefreshing = true
        mAdapter.triggerWeatherFetch()
    }

    fun endRefreshing(){
        mSwipeRefreshLayout.isRefreshing = false
    }
}