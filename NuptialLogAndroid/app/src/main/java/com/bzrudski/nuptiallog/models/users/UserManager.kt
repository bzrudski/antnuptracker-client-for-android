package com.bzrudski.nuptiallog.models.users

import com.bzrudski.nuptiallog.WebInt
import com.bzrudski.nuptiallog.management.FlightAppManager
import com.bzrudski.nuptiallog.management.UrlManager
import com.google.gson.JsonSyntaxException

object UserManager {

    var observer: UserObserver? = null

    /**
     * Cached user information.
     *
     * Cache a user before destroying a UserActivity. Therefore,
     * a full reload of the user info isn't needed. Make sure to
     * clear when fully ending the activity.
     */
    var cachedUser: User? = null

    sealed class UserFetchError{
        object JsonError: UserFetchError()
        object AuthError: UserFetchError()
        object NoResponse: UserFetchError()
        object NotFound: UserFetchError()
        data class UserError(val status:Int): UserFetchError()
    }

    interface UserObserver {
        fun fetchedUser(user: User)
        fun fetchedUserWithError(username: String, error: UserFetchError)
    }

    fun fetchUserForUsername(username: String){
        val url = UrlManager.getUserURL(username)

        Thread {
            WebInt.request(WebInt.HttpMethods.GET, url, callback = {
                status, responseData ->

                when (status) {
                    404 -> {
                        observer?.fetchedUserWithError(username, UserFetchError.NotFound)
                        return@request
                    }
                    401 -> {
                        observer?.fetchedUserWithError(username, UserFetchError.AuthError)
                        return@request
                    }
                    200 -> {}
                    else -> {
                        observer?.fetchedUserWithError(username, UserFetchError.UserError(status))
                        return@request
                    }
                }

                try {
                    val user = FlightAppManager.gson.fromJson(responseData, User::class.java)
                    observer?.fetchedUser(user)
                } catch (e:JsonSyntaxException) {
                    observer?.fetchedUserWithError(username, UserFetchError.JsonError)
                }

            }, errorHandler = {
                observer?.fetchedUserWithError(username, UserFetchError.NoResponse)
            })
        }.start()
    }
}