package com.snipertech.leftoversaver.view.ui.volunteer

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.ActivityVolunteerSettingsBinding
import com.snipertech.leftoversaver.util.ApplicationLanguageHelper
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.UsefulMethodsUtil

class VolunteerSettingsActivity : AppCompatActivity() {
    private var activityBinding: ActivityVolunteerSettingsBinding? = null
    private val binding get() = activityBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityVolunteerSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ArrayAdapter.createFromResource(
            applicationContext,
            R.array.language,
            R.layout.support_simple_spinner_dropdown_item
        )
        binding.changeLanguage.adapter = adapter
        spinnerItemClicked()

        //get the toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        //Change the color of the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.settings)

    }


    private fun spinnerItemClicked() {
        binding.changeLanguage.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p0 != null) {
                        changeLanguage(p0.getItemAtPosition(p2) as String)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

    }

    private fun changeLanguage(lang: String) {
        when (lang) {
            resources.getString(R.string.english) -> {
                setLocale(Constants.ENGLISH)
            }
            resources.getString(R.string.arabic) -> {
                setLocale(Constants.ARABIC)
            }
        }
    }


    private fun setLocale(languageToLoad: String) {
        val pref = UsefulMethodsUtil

        pref.saveStringToSharedPreferences(
            applicationContext,
            Constants.PREF_NAME,
            Constants.SELECTED_LANGUAGE,
            languageToLoad
        )
        finish()
        startActivity(Intent(applicationContext, VolunteerMainActivity::class.java))
    }

    override fun attachBaseContext(newBase: Context?) {
        val pref = UsefulMethodsUtil
        val lang = newBase?.let {
            pref.getSavedStringFromPreference(
                it,
                Constants.PREF_NAME,
                Constants.SELECTED_LANGUAGE
            )
        }
        super.attachBaseContext(ApplicationLanguageHelper.applyLanguage(newBase!!, lang!!))
    }

    override fun onDestroy() {
        super.onDestroy()
        activityBinding = null
    }
}