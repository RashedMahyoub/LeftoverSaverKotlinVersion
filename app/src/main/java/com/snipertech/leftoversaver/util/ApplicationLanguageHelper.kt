package com.snipertech.leftoversaver.util

import android.content.Context
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AppCompatActivity
import com.snipertech.leftoversaver.R
import java.util.*


class ApplicationLanguageHelper(base: Context) : ContextThemeWrapper(base, R.style.AppTheme) {
    companion object {

        fun applyLanguage(context: Context, language: String): Context {
            val locale = Locale(language)
            val configuration = context.resources.configuration

            Locale.setDefault(locale)
            configuration.setLocale(locale)
            return context.createConfigurationContext(configuration)
        }
    }
}