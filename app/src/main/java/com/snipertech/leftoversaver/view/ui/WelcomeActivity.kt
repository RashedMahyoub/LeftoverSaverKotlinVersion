package com.snipertech.leftoversaver.view.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.ActivityWelcomeBinding
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.UsefulMethodsUtil
import com.snipertech.leftoversaver.view.ui.donor.DonorLoginActivity
import com.snipertech.leftoversaver.view.ui.donor.DonorMainActivity
import com.snipertech.leftoversaver.view.ui.needy.NeedyLoginActivity
import com.snipertech.leftoversaver.view.ui.volunteer.VolunteerLoginActivity
import com.snipertech.leftoversaver.view.ui.volunteer.VolunteerMainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class WelcomeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setContentView(binding.root)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.donor -> {
                goToDonor()
            }
            R.id.needy -> {
                goToNeedy()
            }
            R.id.volunteer -> {
                goToVolunteer()
            }
        }
    }

    private fun goToDonor() {
        val intent = Intent(this, DonorLoginActivity::class.java)
        startActivity(intent)
    }

    private fun goToNeedy() {
        val intent = Intent(this, NeedyLoginActivity::class.java)
        startActivity(intent)
    }

    private fun goToVolunteer() {
        val intent = Intent(this, VolunteerLoginActivity::class.java)
        startActivity(intent)
    }

    //initialize everything
    private fun init(){
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        //setup click listeners
        binding.donor.setOnClickListener(this)
        binding.needy.setOnClickListener(this)
        binding.volunteer.setOnClickListener(this)
        checkIfLoggedIn()
    }


    //getSharedPreferences
    private fun checkIfLoggedIn() {
        val pref = UsefulMethodsUtil
        pref.apply {
            val data = getSavedStringFromPreference(
                applicationContext,
                Constants.PREF_NAME,
                Constants.TYPE
            )
            when (data) {
                Constants.VOL -> {
                    val intent = Intent(
                        this@WelcomeActivity, VolunteerMainActivity::class.java
                    )
                    startActivity(intent)
                    finish()
                }

                Constants.DONOR -> {
                    val intent = Intent(
                        this@WelcomeActivity, DonorMainActivity::class.java
                    )
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
