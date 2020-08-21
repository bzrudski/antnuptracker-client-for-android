package com.bzrudski.nuptiallog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.bzrudski.nuptiallog.databinding.ActivityAddFlightBinding
import com.bzrudski.nuptiallog.models.flights.Species
import com.bzrudski.nuptiallog.models.flights.TaxonomyManager

class AddFlightActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityAddFlightBinding
    private lateinit var mGenusSelector: AutoCompleteTextView
    private lateinit var mSpeciesSelector: AutoCompleteTextView
    private val mGenera = TaxonomyManager.genera
    private var mSpecies: Array<String> = arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAddFlightBinding.inflate(layoutInflater)
        val view = mBinding.root
        setContentView(view)

        mGenusSelector = mBinding.genusSelector

        mSpeciesSelector = mBinding.speciesSelector

        val genusAdapter = ArrayAdapter(this, R.layout.taxonomy_dropdown_item, mGenera)
        mGenusSelector.setAdapter(genusAdapter)

        mGenusSelector.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                val genusContainer = mBinding.genusContainer

                if (s.isBlank()){
                    genusContainer.isErrorEnabled = true
                    genusContainer.error = getString(R.string.please_enter_genus)
                    mSpecies = arrayOf()
                }
                else if (!mGenera.contains(s.toString())){
                    genusContainer.isErrorEnabled = true
                    genusContainer.error = getString(R.string.invalid_genus)
                    mSpecies = arrayOf()
                } else {
                    genusContainer.isErrorEnabled = false
                    genusContainer.error = null
                    mSpecies = TaxonomyManager[s.toString()]
                }
                mSpeciesSelector.text.clear()
                mSpeciesSelector.setAdapter(ArrayAdapter(this@AddFlightActivity, R.layout.taxonomy_dropdown_item, mSpecies))
            }

        })

        mSpeciesSelector.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                val speciesContainer = mBinding.speciesContainer

                if (s.isBlank()){
                    speciesContainer.isErrorEnabled = true
                    speciesContainer.error = getString(R.string.please_enter_species)
                }
                else if (!mSpecies.contains(s.toString())){
                    speciesContainer.isErrorEnabled = true
                    speciesContainer.error = getString(R.string.invalid_species)
                } else {
                    speciesContainer.isErrorEnabled = false
                    speciesContainer.error = null
                }
            }

        })

    }
}