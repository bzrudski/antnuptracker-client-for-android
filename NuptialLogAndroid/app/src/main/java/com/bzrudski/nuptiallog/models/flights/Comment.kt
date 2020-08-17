package com.bzrudski.nuptiallog.models.flights

import com.bzrudski.nuptiallog.models.users.Role
import java.util.*

class Comment(
    val flight: Int,
    val author: String,
    val time: Date,
    val role: Role,
    val text: String
) {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true

        if (other !is Comment) return false
        if (other.flight != flight) return false
        if (other.author != author) return false
        if (other.time != time) return false
        if (other.role != role) return false
        if (other.text != text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = flight
        result = 31 * result + author.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + role.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }
}