package com.bzrudski.nuptiallog.models.users

import com.bzrudski.nuptiallog.management.TokenAuth

class Session(
    username: String,
    isProfessional:Boolean,
    description:String="",
    institution:String="",
    token: String
) {
    private val user = User(username = username, isProfessional = isProfessional, description = description, institution = institution)

    val username get() = user.username
    val role get() = user.role
    val description get() = user.description
    val institution get() = user.institution
    val authentication = TokenAuth(token)

    companion object {
        const val USERNAME_KEY = "USER_USERNAME"
        const val TOKEN_KEY = "USER_TOKEN"
        const val PROFESSIONAL_KEY = "USER_PROFESSIONAL"
        const val DESCRIPTION_KEY = "USER_DESCRIPTION"
        const val INSTITUTION_KEY = "USER_INSTITUTION"
    }
}