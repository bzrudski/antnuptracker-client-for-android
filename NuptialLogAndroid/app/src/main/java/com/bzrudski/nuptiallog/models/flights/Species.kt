package com.bzrudski.nuptiallog.models.flights

class Species private constructor(val genus: Genus, val name:String)
{
    override fun toString(): String {
        return "$genus $name"
    }
}