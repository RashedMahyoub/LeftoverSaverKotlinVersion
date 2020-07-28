package com.snipertech.leftoversaver.view.ui.volunteer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.ActivityVolunteerMainBinding
import com.snipertech.leftoversaver.model.Volunteer
import com.snipertech.leftoversaver.util.ApplicationLanguageHelper
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.UsefulMethodsUtil
import com.snipertech.leftoversaver.view.adapter.SectionsPagerAdapter
import com.snipertech.leftoversaver.view.ui.donor.DonorLoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class VolunteerMainActivity : AppCompatActivity() {

    private var backPressedTime: Long = 0
    private var backToast: Toast? = null
    private var volunteer: Volunteer? = null
    private var activityBinding: ActivityVolunteerMainBinding? = null
    private val binding get() = activityBinding!!
    private lateinit var mAdView: AdView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityVolunteerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setup view pager with tab layout
        val sectionsPagerAdapter =
            SectionsPagerAdapter(
                this,
                supportFragmentManager
            )
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        //get the toolbar
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        //Display user name
        val pref = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        val obj = pref.getString(Constants.OBJECT, "")
        volunteer = Gson().fromJson(obj, Volunteer::class.java)
        binding.volunteerName.text = volunteer?.name

        //adMob
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu_volunteer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                val intent = Intent(this, VolunteerSettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.log_out -> {
                val intent = Intent(this, VolunteerLoginActivity::class.java)
                applicationContext.getSharedPreferences(Constants.PREF_NAME, 0).edit().clear().apply()
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast?.cancel()
            finish()
            moveTaskToBack(true)
            super.onBackPressed()
            return

        }else {
            backToast =
                Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT)
            backToast?.show()
        }
        backPressedTime = System.currentTimeMillis()
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
        backToast = null
        activityBinding = null
        volunteer = null
        mAdView.adListener = null
        mAdView.destroy()
    }

}