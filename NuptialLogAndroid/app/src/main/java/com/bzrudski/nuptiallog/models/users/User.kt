package com.bzrudski.nuptiallog.models.users

import com.bzrudski.nuptiallog.R
import com.bzrudski.nuptiallog.models.table.Row
import com.bzrudski.nuptiallog.models.table.RowModifier
import com.bzrudski.nuptiallog.models.table.Table
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class User(val username: String,
           @SerializedName("professional") val isProfessional:Boolean = false,
           @SerializedName("flagged") val isFlagged:Boolean = false,
           val description: String = "",
           val institution: String = "") {

    val role: Role
    get() {
        return when {
            isFlagged -> Role.FLAGGED
            isProfessional -> Role.MYRMECOLOGIST
            else -> Role.CITIZEN
        }
    }

    enum class UserRowModifiers: RowModifier {
        TEXT,
        STACKED,
        ROLE
    }
    
    fun toTable(): Table<UserRowModifiers> {
        val table = Table<UserRowModifiers>()

        table.addRow(Row(R.string.username_label, username, UserRowModifiers.TEXT))
        table.addRow(Row(R.string.role_label, role, UserRowModifiers.ROLE))

        if (!institution.isBlank()) {
            table.addRow(Row(R.string.institution_label, institution, UserRowModifiers.STACKED))
        }

        if (!description.isBlank()) {
            table.addRow(Row(R.string.user_description_label, description, UserRowModifiers.STACKED))
        }

        return table
    }

}