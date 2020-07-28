package com.snipertech.leftoversaver.view.ui.needy

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NeedyMainActivity : AppCompatActivity() {

    private var backPressedTime: Long = 0
    private lateinit var backToast: Toast
    private lateinit var navController: NavController
    private lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_needy_main)
        init()
    }

    private fun init() {
        //change the color of actionbar
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FFFFFF")))
        supportActionBar?.elevation = 0F

        //setup nav controller for nav host
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_needy) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupActionBarWithNavController(this, navController)

        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu_needy, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                when (navController.currentDestination?.id) {
                    R.id.restaurantListFragment -> navController.navigate(
                        R.id.action_restaurantListFragment_to_needySettingsFragment
                    )
                    R.id.restauantItemsFragment -> navController.navigate(
                        R.id.action_restauantItemsFragment_to_needySettingsFragment
                    )
                    R.id.needyHomeFragment -> navController.navigate(
                        R.id.action_needyHomeFragment_to_needySettingsFragment
                    )
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        when (navController.graph.startDestination) {
            navController.currentDestination?.id ->
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    backToast.cancel()
                    finish()
                    moveTaskToBack(true)
                    super.onBackPressed()
                    return

                } else {
                    backToast =
                        Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT)
                    backToast.show()
                }
            else -> super.onBackPressed()
        }
        backPressedTime = System.currentTimeMillis()
    }

    //handle back arrow button
    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()
        return super.onSupportNavigateUp()
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
