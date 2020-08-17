package com.bzrudski.nuptiallog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bzrudski.nuptiallog.models.flights.FlightList
import com.bzrudski.nuptiallog.models.flights.observers.FlightReadObserver
import com.bzrudski.nuptiallog.userinterface.FlightListAdapter

class MainActivity : AppCompatActivity(), FlightReadObserver {

    companion object {
        private val LOG_TAG = MainActivity::class.java.simpleName
        const val FLIGHT_ID_EXTRA = "FLIGHT_ID"
    }

    // FIELDS
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: FlightListAdapter? = null
    private var mSwipeContainer: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSwipeContainer = findViewById(R.id.swipe_container)
        mRecyclerView = findViewById(R.id.recycler_view)
        mAdapter = FlightListAdapter(this)

        mRecyclerView!!.adapter = mAdapter

        mRecyclerView!!.layoutManager = LinearLayoutManager(this)

        mRecyclerView!!.addItemDecoration(DividerItemDecoration(mRecyclerView!!.context, DividerItemDecoration.VERTICAL))

        FlightList.readObserver = this

        mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val scrollPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                Log.d(LOG_TAG, "Scrolled to $scrollPosition")

                if (scrollPosition >= FlightList.length - 5){
                    FlightList.readMore()
                }
            }
        })

        mSwipeContainer!!.setOnRefreshListener {
            FlightList.getNewFlights()
        }

        Log.d(LOG_TAG, "About to start reading flights")
        FlightList.getNewFlights()
    }

    override fun flightListRead() {
        runOnUiThread {
            mRecyclerView!!.adapter!!.notifyDataSetChanged()
        }

        Log.d(LOG_TAG, "Flights read")
    }

    override fun flightListReadWithError(error: FlightList.ListReadErrors) {
        Log.d(LOG_TAG, "Error reading flight list")
        when (error){
            FlightList.ListReadErrors.NoResponse -> Log.d(LOG_TAG, "No Response")
            FlightList.ListReadErrors.JsonError -> Log.d(LOG_TAG, "JSON parse error")
            FlightList.ListReadErrors.AuthError -> Log.d(LOG_TAG, "Authentication error")
            is FlightList.ListReadErrors.ReadError -> Log.d(LOG_TAG, "Read error ${error.status}")
        }
    }

    override fun flightListReadMore(n: Int) {
        Log.d(LOG_TAG, "Read $n new flights from the end")
        runOnUiThread {
            mRecyclerView!!.adapter!!.notifyItemRangeInserted(FlightList.length - n, n)
        }
    }

    override fun flightListReadMoreWithError(error: FlightList.ListReadErrors) {
        Log.d(LOG_TAG, "Error reading more from flight list")
        when (error){
            FlightList.ListReadErrors.NoResponse -> Log.d(LOG_TAG, "No Response")
            FlightList.ListReadErrors.JsonError -> Log.d(LOG_TAG, "JSON parse error")
            FlightList.ListReadErrors.AuthError -> Log.d(LOG_TAG, "Authentication error")
            is FlightList.ListReadErrors.ReadError -> Log.d(LOG_TAG, "Read error ${error.status}")
        }
    }

    override fun flightListGotNewFlights(n: Int) {
        runOnUiThread {
            mSwipeContainer?.isRefreshing = false
            mRecyclerView!!.adapter!!.notifyItemRangeInserted(0, n)
            mRecyclerView!!.scrollToPosition(0)
        }
    }

    override fun flightListGotNewFlightsWithError(error: FlightList.GetNewFlightsErrors) {
        Log.d(LOG_TAG, "Error reading new flights from flight list")
        when (error){
            FlightList.GetNewFlightsErrors.NoResponse -> Log.d(LOG_TAG, "No Response")
            FlightList.GetNewFlightsErrors.JsonError -> Log.d(LOG_TAG, "JSON parse error")
            FlightList.GetNewFlightsErrors.AuthError -> Log.d(LOG_TAG, "Authentication error")
            is FlightList.GetNewFlightsErrors.GetError -> Log.d(LOG_TAG, "Get error ${error.status}")
        }
        runOnUiThread {
            mSwipeContainer?.isRefreshing = false
        }
    }

    override fun flightListChanged() {
        TODO("Not yet implemented")
    }

    override fun flightListCleared() {
        TODO("Not yet implemented")
    }
}
