package com.bzrudski.nuptiallog.models.flights

import java.io.File

object TaxonomyManager
{

    const val filename = "/Users/benjamin/Developer/KotlinTest/src/taxonomyRaw"
    private val taxonomyFile: File = File(filename)
    // var genera: ArrayList<String> = ArrayList()
    var species: HashMap<String, ArrayList<String>> = HashMap()

    init {
        species["Unknown"] = arrayListOf("Unknown (sp.)")
        println("Reading from the file")
        taxonomyFile.forEachLine(charset = Charsets.UTF_8, action =
        { line: String ->
            println("Reading from file... $line")
            if (line.isEmpty()) return@forEachLine
            if (line[0] == '#') return@forEachLine
            val lineSplit = line.split(Regex("\\s"), limit = 2)

            println("Split to: $lineSplit")

            val genus = lineSplit[0]

            if (!species.keys.contains(genus)) {
//                sp.add(genus)
                species.put(genus, arrayListOf("Unknown (sp.)"))
            }

            val speciesName = lineSplit[1]
            species.getValue(genus).add(speciesName)
        }
        )
        println("Found ${species.keys.count()} genera and ${species.values.stream().flatMap { list -> list.stream() }.count()} species")
    }
}