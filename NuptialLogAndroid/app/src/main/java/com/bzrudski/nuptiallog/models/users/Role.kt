package com.bzrudski.nuptiallog.models.users

import com.bzrudski.nuptiallog.R
import com.google.gson.annotations.SerializedName

enum class Role(val rawValue: Int)
{
    @SerializedName("-1")
    FLAGGED(-1),

    @SerializedName("0")
    CITIZEN(0),

    @SerializedName("1")
    MYRMECOLOGIST(1);

    companion object {
        fun withRawValue(n: Int): Role{
            return values().first { it.rawValue == n }
        }
    }

    fun getStringResource(): Int {
        return when (this){
            FLAGGED -> R.string.flagged
            CITIZEN -> R.string.citizen
            MYRMECOLOGIST -> R.string.myrmecologist
        }
    }

}