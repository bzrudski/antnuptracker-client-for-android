package com.bzrudski.nuptiallog.userinterface

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bzrudski.nuptiallog.R
import com.bzrudski.nuptiallog.UserActivity
import com.bzrudski.nuptiallog.models.table.Row
import com.bzrudski.nuptiallog.models.table.Table
import com.bzrudski.nuptiallog.models.users.Role
import com.bzrudski.nuptiallog.models.users.User
import com.bzrudski.nuptiallog.models.users.UserManager

class UserAdapter(activity: UserActivity, username:String): RecyclerView.Adapter<UserAdapter.UserViewHolder>(), UserManager.UserObserver {

    companion object {
        private val LOG_TAG = UserAdapter::class.java.simpleName
    }

    private val mActivity = activity
    private val mContext = activity as Context
    private val mUsername = username
    private val mInflater = LayoutInflater.from(mActivity)
    private lateinit var mUser: User
    private var mUserTable = Table<User.UserRowModifiers>()

    init {
        UserManager.observer = this
    }

    inner class UserViewHolder(itemView:View): RecyclerView.ViewHolder(itemView) {
        private val mHeaderTextView: TextView = itemView.findViewById(R.id.cell_label)
        private val mContentTextView: TextView = itemView.findViewById(R.id.cell_content)

        fun prepareForRow(row: Row<Any, User.UserRowModifiers>){
            mHeaderTextView.text = mActivity.getText(row.headerId)

            when (row.content) {
                is Role -> {
                    mContentTextView.text = mActivity.getText(row.content.getStringResource())
//                    mContentTextView.gravity = Gravity.CENTER_VERTICAL
//                    mContentTextView.compoundDrawablePadding = 8
//                    mHeaderTextView.gravity = Gravity.CENTER_VERTICAL

                    when (row.content){
                        Role.MYRMECOLOGIST -> mContentTextView.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_antbluesmall, 0)
                        Role.FLAGGED -> mContentTextView.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_antredsmall, 0)
                        Role.CITIZEN -> mContentTextView.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
                    }
                }

                is String -> {
                    mContentTextView.text = row.content
                }
                else -> {
                    mContentTextView.text = row.content.toString()
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = when (User.UserRowModifiers.values()[viewType]){
            User.UserRowModifiers.TEXT, User.UserRowModifiers.ROLE -> mInflater.inflate(R.layout.label_detail_cell, parent, false)
            User.UserRowModifiers.STACKED -> mInflater.inflate(R.layout.detail_below_label_cell, parent, false)
        }
        
        return UserViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mUserTable.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.prepareForRow(mUserTable[position])
    }

    override fun getItemViewType(position: Int): Int {
        return mUserTable[position].modifier.ordinal
    }

    override fun fetchedUser(user: User) {
        mUser = user
        mUserTable = mUser.toTable()

        Log.d(LOG_TAG, "Successfully got user with username $mUsername")

        mActivity.runOnUiThread {
            notifyDataSetChanged()
            mActivity.endRefreshing()
        }
    }

    override fun fetchedUserWithError(username: String, error: UserManager.UserFetchError) {

        Log.d(LOG_TAG, "Failed to get user with username with:")

        when (error) {
            UserManager.UserFetchError.JsonError -> Log.d(LOG_TAG, "Error parsing user data")
            UserManager.UserFetchError.AuthError -> Log.d(LOG_TAG, "Authentication error")
            UserManager.UserFetchError.NoResponse -> Log.d(LOG_TAG, "No response")
            UserManager.UserFetchError.NotFound -> Log.d(LOG_TAG, "No user found")
            is UserManager.UserFetchError.UserError -> Log.d(LOG_TAG, "User error (${error.status})")
        }

        mActivity.runOnUiThread {
            mActivity.endRefreshing()
        }
    }

    fun triggerUserFetch(){
        UserManager.fetchUserForUsername(mUsername)
    }
}