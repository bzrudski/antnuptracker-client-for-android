package com.bzrudski.nuptiallog.models.flights

import com.bzrudski.nuptiallog.WebInt
import com.bzrudski.nuptiallog.management.FlightAppManager
import com.bzrudski.nuptiallog.management.UrlManager
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

object ChangelogFetcher {

    var observer: ChangelogFetchObserver? = null

    sealed class ChangelogFetchError{
        object NoResponse: ChangelogFetchError()
        object AuthError: ChangelogFetchError()
        object JsonError: ChangelogFetchError()
        data class ChangelogError(val status:Int): ChangelogFetchError()
    }

    interface ChangelogFetchObserver {
        fun gotChangelog(changelog: ArrayList<Changelog>)
        fun gotChangelogWithError(id: Int, error: ChangelogFetchError)
    }

    fun getChangelogForId(id: Int){
        val url = UrlManager.urlForChangelog(id)

        Thread {
            WebInt.request(WebInt.HttpMethods.GET, url, callback = {
                    status, responseData ->

                when (status) {
                    401 -> {
                        observer?.gotChangelogWithError(id, ChangelogFetchError.AuthError)
                        return@request
                    }
                    200 -> {}
                    else -> {
                        observer?.gotChangelogWithError(id, ChangelogFetchError.ChangelogError(status))
                        return@request
                    }
                }

                try {
                    val changelogArrayListType = object : TypeToken<ArrayList<Changelog>>(){}.type
                    val changelog = FlightAppManager.gson.fromJson<ArrayList<Changelog>>(responseData, changelogArrayListType)
                    observer?.gotChangelog(changelog)
                } catch (e: JsonSyntaxException) {
                    observer?.gotChangelogWithError(id, ChangelogFetchError.JsonError)
                }

            }, errorHandler = {
                observer?.gotChangelogWithError(id, ChangelogFetchError.NoResponse)
            })
        }.start()
    }
}