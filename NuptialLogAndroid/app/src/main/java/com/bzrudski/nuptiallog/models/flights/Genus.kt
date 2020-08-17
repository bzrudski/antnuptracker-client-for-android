package com.bzrudski.nuptiallog.models.flights

class Genus private constructor(val name:String)
{
    companion object
    {
        private var genusStore: HashMap<String, Genus> = HashMap()
        @JvmStatic fun get(name:String): Genus
        {
            return genusStore.getValue(name)
        }
    }

    override fun toString(): String {
        return name
    }
}