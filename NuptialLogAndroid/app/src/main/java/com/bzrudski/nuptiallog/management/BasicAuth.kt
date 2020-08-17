package com.bzrudski.nuptiallog.management

import android.util.Base64

class BasicAuth(private val username:String, private val password:String): AuthenticationCredential {

    override val authorizationString: String
    get() {
        val rawString = "${username}:${password}"
        val encodedCreds = Base64.encodeToString(rawString.toByteArray(), Base64.DEFAULT)
        return "Basic $encodedCreds"
    }
}