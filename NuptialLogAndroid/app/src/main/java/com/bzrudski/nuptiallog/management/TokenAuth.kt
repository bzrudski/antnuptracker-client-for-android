package com.bzrudski.nuptiallog.management

class TokenAuth(private val token: String): AuthenticationCredential {
    override val authorizationString: String get() = "Token $token"
}