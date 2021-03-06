package com.bzrudski.nuptiallog

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bzrudski.nuptiallog.userinterface.UserAdapter

class UserActivity : AppCompatActivity() {

    companion object {
        const val USERNAME_EXTRA = "USERNAME_EXTRA"
        private val LOG_TAG = UserActivity::class.java.simpleName
    }

    private var mUsername = ""
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: UserAdapter
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val username = intent.getStringExtra(USERNAME_EXTRA)

        if (username == null){
            AlertDialog.Builder(this)
                .setTitle(R.string.no_user_selected)
                .setMessage(R.string.please_select_user)
                .setNegativeButton(R.string.ok){ _: DialogInterface, _: Int ->
                    finish()
                }.show()

            return
        } else {
            mUsername = username
        }

        title = getString(R.string.user_title, mUsername)

        mRecyclerView = findViewById(R.id.recycler_view)
        mAdapter = UserAdapter(this, mUsername)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(this)

        mSwipeRefreshLayout = findViewById(R.id.swipe_container)
        mSwipeRefreshLayout.setOnRefreshListener(mAdapter::triggerUserFetch)

        Log.d(LOG_TAG, "Finished setting up user activity.")

        mSwipeRefreshLayout.isRefreshing = true
        mAdapter.triggerUserFetch()
    }

    fun endRefreshing(){
        mSwipeRefreshLayout.isRefreshing = false
    }
}