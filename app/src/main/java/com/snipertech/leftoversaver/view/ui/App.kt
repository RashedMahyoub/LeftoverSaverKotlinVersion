package com.snipertech.leftoversaver.view.ui

import android.app.Application
import android.content.Context
import com.snipertech.leftoversaver.util.ApplicationLanguageHelper
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.UsefulMethodsUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun attachBaseContext(newBase: Context) {
        val pref = UsefulMethodsUtil
        val lang = newBase.let {
            pref.getSavedStringFromPreference(
                it,
                Constants.PREF_NAME,
                Constants.SELECTED_LANGUAGE
            )
        }
        super.attachBaseContext(lang?.let { ApplicationLanguageHelper.applyLanguage(newBase, it) })
    }
}