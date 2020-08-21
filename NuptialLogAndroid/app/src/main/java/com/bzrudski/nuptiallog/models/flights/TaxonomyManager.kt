package com.bzrudski.nuptiallog.models.flights

import android.content.Context
import com.bzrudski.nuptiallog.R
import java.io.BufferedInputStream
import java.io.InputStreamReader

object TaxonomyManager
{
    val genera: Array<String> get() = species.keys.sorted().toTypedArray()

    private var species: HashMap<String, ArrayList<String>> = HashMap()

    fun initialize(context: Context) {
        val taxonomyInputStream = context.resources.openRawResource(R.raw.taxonomy)
        val taxonomyReader = InputStreamReader(BufferedInputStream(taxonomyInputStream))

        species["Unknown"] = arrayListOf("Unknown (sp.)")
        println("Reading from the file")
        taxonomyReader.forEachLine { line: String ->
            println("Reading from file... $line")
            if (line.isEmpty()) return@forEachLine
            if (line[0] == '#') return@forEachLine
            val lineSplit = line.split(Regex("\\s"), limit = 2)

            println("Split to: $lineSplit")

            val genus = lineSplit[0]

            if (!species.keys.contains(genus)) {
//                sp.add(genus)
                species[genus] = arrayListOf("Unknown (sp.)")
            }

            val speciesName = lineSplit[1]
            species.getValue(genus).add(speciesName)
        }
        println("Found ${species.keys.count()} genera and ${species.values.stream().flatMap { list -> list.stream() }.count()} species")
    }

    operator fun get(name: String): Array<String>{

        return species[name]!!.toTypedArray()

    }
}