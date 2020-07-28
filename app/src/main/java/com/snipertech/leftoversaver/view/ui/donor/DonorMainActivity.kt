package com.snipertech.leftoversaver.view.ui.donor

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.util.ApplicationLanguageHelper
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.UsefulMethodsUtil
import com.snipertech.leftoversaver.view.ui.WelcomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class DonorMainActivity : AppCompatActivity() {

    private var navController: NavController? = null
    private var backToast: Toast? = null
    private var backPressedTime: Long = 0
    private lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_main)
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu_donor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                when (navController?.currentDestination?.id) {
                    R.id.donorAddItemFragment -> navController?.navigate(
                        R.id.action_donorAddItemFragment_to_donorSettingsFragment
                    )
                    R.id.donorProfileFragment -> navController?.navigate(
                        R.id.action_donorProfileFragment_to_donorSettingsFragment
                    )
                    R.id.donorHomeFragment -> navController?.navigate(
                        R.id.action_donorHomeFragment_to_donorSettingsFragment
                    )
                    R.id.donorOrdersFragment -> navController?.navigate(
                        R.id.action_donorOrdersFragment_to_donorSettingsFragment
                    )
                }
                return true
            }
            R.id.log_out -> {
                val intent = Intent(this, WelcomeActivity::class.java)
                applicationContext.getSharedPreferences(Constants.PREF_NAME, 0).edit().clear().apply()
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
                return true
            }
            R.id.profile -> {
                when (navController?.currentDestination?.id) {
                    R.id.donorAddItemFragment -> navController?.navigate(
                        R.id.action_donorAddItemFragment_to_donorProfileFragment
                    )
                    R.id.donorSettingsFragment -> navController?.navigate(
                        R.id.action_donorSettingsFragment_to_donorProfileFragment
                    )
                    R.id.donorHomeFragment -> navController?.navigate(
                        R.id.action_donorHomeFragment_to_donorProfileFragment
                    )
                    R.id.donorOrdersFragment -> navController?.navigate(
                        R.id.action_donorOrdersFragment_to_donorProfileFragment
                    )
                }
                return true
            }
            R.id.donorOrders -> {
                when (navController?.currentDestination?.id) {
                    R.id.donorAddItemFragment -> navController?.navigate(
                        R.id.action_donorAddItemFragment_to_donorOrdersFragment
                    )
                    R.id.donorSettingsFragment -> navController?.navigate(
                        R.id.action_donorSettingsFragment_to_donorOrdersFragment
                    )
                    R.id.donorHomeFragment -> navController?.navigate(
                        R.id.action_donorHomeFragment_to_donorOrdersFragment
                    )
                    R.id.donorProfileFragment -> navController?.navigate(
                        R.id.action_donorProfileFragment_to_donorOrdersFragment
                    )
                }
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onBackPressed() {
        when (navController?.graph?.startDestination) {
            navController?.currentDestination?.id ->
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    backToast?.cancel()
                    finish()
                    moveTaskToBack(true)
                    super.onBackPressed()
                    return

                } else {
                    backToast =
                        Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT)
                    backToast?.show()
                }
            else -> super.onBackPressed()
        }
        backPressedTime = System.currentTimeMillis()
    }

    //handle back button
    override fun onSupportNavigateUp(): Boolean {
        when (navController?.currentDestination?.id) {
            R.id.donorAddItemFragment -> navController?.navigate(
                R.id.action_donorAddItemFragment_to_donorHomeFragment
            )
            R.id.donorProfileFragment -> navController?.navigate(
                R.id.action_donorProfileFragment_to_donorHomeFragment
            )
            R.id.donorSettingsFragment -> navController?.navigate(
                R.id.action_donorSettingsFragment_to_donorHomeFragment
            )
            R.id.donorOrdersFragment -> navController?.navigate(
                R.id.action_donorOrdersFragment_to_donorHomeFragment
            )
        }
        return super.onSupportNavigateUp()
    }
    //initialize everything
    private fun init(){
        //change the color of actionbar
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FFFFFF")))
        supportActionBar?.elevation = 0F

        //setup nav controller for nav host
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupActionBarWithNavController(this, navController!!)

        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
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
        mAdView.adListener = null
        mAdView.destroy()
    }
}
