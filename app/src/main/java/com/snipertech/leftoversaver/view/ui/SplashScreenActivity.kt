package com.snipertech.leftoversaver.view.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.UsefulMethodsUtil
import com.snipertech.leftoversaver.view.ui.donor.DonorLoginActivity
import com.snipertech.leftoversaver.view.ui.volunteer.VolunteerMainActivity
import gr.net.maroulis.library.EasySplashScreen
import java.util.*

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = EasySplashScreen(this@SplashScreenActivity)
            .withFullScreen()
            .withTargetActivity(WelcomeActivity::class.java)
            .withSplashTimeOut(2000)
            .withBackgroundColor(Color.parseColor("#FFFFFF"))
            .withLogo(R.mipmap.ic_launcher)

        val easySplashScreen = config.create()
        setContentView(easySplashScreen)
    }
}
