package com.bzrudski.nuptiallog.management

import android.net.Uri
import com.bzrudski.nuptiallog.models.flights.Genus
import com.bzrudski.nuptiallog.models.flights.Species
import java.net.URL

object UrlManager {

    private val baseUri = Uri.parse("https://www.antnuptialflights.com/")
    private const val loginURL = "login"
    private const val verifyURL = "verify"
    private const val logoutDeviceURL = "logout"
    private const val flightsURL = "flights"
    private const val createURL = "create"
    private const val createAccountURL = "create-account"
    private const val commentsURL = "comments"
    private const val mySpeciesURL = "my-species"
    private const val historyURL = "history"
    private const val weatherURL = "weather"
    private const val usersURL = "users"
    private const val resetPassURL = "reset-password"
    private const val validateURL = "validate"
    private const val aboutURL = "about"
    private const val privacyURL = "privacy-policy"
    private const val termsURL = "terms-and-conditions"
    private const val emailAddress = "mailto:nuptialtracker@gmail.com"

    fun getHomeURL() : URL {
        return URL(baseUri.toString())
    }

    fun listURL() : URL
    {
        return URL("${Uri.withAppendedPath(baseUri, flightsURL)}")
    }

    fun urlForFlight(id: Int) : URL
    {
        val flightsUri = Uri.parse(listURL().toString())
        val newUri = Uri.withAppendedPath(flightsUri, id.toString())
        return URL(newUri.toString())
//        return URL(Uri.parse(listURL().toString()).buildUpon().appendPath("$id").toString())
    }

    fun urlForWeather(id: Int) : URL
    {
        return URL("${Uri.parse(urlForFlight(id).toString()).buildUpon().appendPath(weatherURL)}")
    }

    fun urlForChangelog(id: Int) : URL
    {
        return URL("${Uri.parse(urlForFlight(id).toString()).buildUpon().appendPath(historyURL)}")
    }

    fun urlForValidate(id: Int) : URL
    {
        return URL("${Uri.parse(urlForFlight(id).toString()).buildUpon().appendPath(validateURL)}/")
    }

    fun urlForCreate() : URL
    {
        return URL("${baseUri.buildUpon().appendPath(createURL)}")
    }

    fun urlForComments() : URL
    {
        return URL("${baseUri.buildUpon().appendPath(commentsURL)}")
    }

    fun getLoginURL() : URL
    {
        return URL("${baseUri.buildUpon().appendPath(loginURL)}/")
    }

    fun getVerifyURL() : URL
    {
        return URL("${Uri.parse("${getLoginURL()}").buildUpon().appendPath(verifyURL)}/")
    }

    fun getLogoutURL() : URL
    {
        return URL("${baseUri.buildUpon().appendPath(logoutDeviceURL)}/")
    }

    fun getMySpeciesURL() : URL
    {
        return URL("${baseUri.buildUpon().appendPath(mySpeciesURL)}")
    }

    fun getCreateAccountURL() : URL
    {
        return URL("${baseUri.buildUpon().appendPath(createAccountURL)}")
    }

    fun getUserURL(username:String) : URL
    {
        return URL("${baseUri.buildUpon().appendPath(usersURL).appendPath(username)}")
    }

    fun getPasswordResetURL() : URL
    {
        return URL("${baseUri.buildUpon().appendPath(resetPassURL)}")
    }

    fun getAboutURL() : URL
    {
        return URL("${baseUri.buildUpon().appendPath(aboutURL)}")
    }

    fun getPrivacyURL() : URL
    {
        return URL("${baseUri.buildUpon().appendPath(privacyURL)}")
    }

    fun getTermsURL() : URL
    {
        return URL("${baseUri.buildUpon().appendPath(termsURL)}")
    }

    fun filteredListURL(genus: Genus) : URL {
        return URL(Uri.parse(listURL().toString()).buildUpon().appendQueryParameter("genus", genus.name).toString())
    }

    fun filteredListURL(species: Species) : URL {
        return URL(Uri.parse(listURL().toString()).buildUpon()
            .appendQueryParameter("genus", species.genus.name)
            .appendQueryParameter("species", species.name).toString())
    }

    fun getContactURL() : URL {
        return URL(emailAddress)
    }
}