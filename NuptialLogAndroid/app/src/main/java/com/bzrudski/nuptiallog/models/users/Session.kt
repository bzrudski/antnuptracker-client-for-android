package com.bzrudski.nuptiallog.models.users

class Session(
    username: String,
    isProfessional:Boolean,
    description:String="",
    institution:String="",
    val token: String,
    val device: Device
) {
    private val user = User(username = username, isProfessional = isProfessional, description = description, institution = institution)

    val username get() = user.username
    val role get() = user.role
    val description get() = user.description
    val institution get() = user.institution
}