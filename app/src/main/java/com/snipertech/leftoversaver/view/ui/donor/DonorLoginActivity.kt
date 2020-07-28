package com.snipertech.leftoversaver.view.ui.donor

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.ActivityDonorLoginBinding
import com.snipertech.leftoversaver.model.Donor
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.Constants.LAUNCH_SECOND_ACTIVITY
import com.snipertech.leftoversaver.util.Constants.REGISTERED_LOCATION
import com.snipertech.leftoversaver.util.DataState
import com.snipertech.leftoversaver.util.UsefulMethodsUtil
import com.snipertech.leftoversaver.view.ui.MapsActivity
import com.snipertech.leftoversaver.viewModel.DonorViewModel
import com.snipertech.leftoversaver.viewModel.MainStateEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class DonorLoginActivity : AppCompatActivity(), View.OnClickListener {

    private var activityBinding: ActivityDonorLoginBinding? = null
    private val binding get() = activityBinding!!
    private val loginActivityViewModel: DonorViewModel by viewModels()
    private var regIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        binding.loginName?.setText(savedInstanceState?.getString("NAME"))
        binding.loginPhone.setText(savedInstanceState?.getString("PHONE"))
        binding.loginPass.setText(savedInstanceState?.getString("PASSWORD"))
        binding.loginAddress?.text = savedInstanceState?.getString("ADDRESS")
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.login -> {
                if (binding.login.text.toString() == resources.getString(R.string.login)) {
                    login()
                } else {
                    register()
                }
            }
            R.id.donor_switch_login -> {
                goToLogin()
            }
            R.id.donor_switch_register -> {
                goToRegister()
            }
            R.id.address_layout -> {
                if(UsefulMethodsUtil.isServicesOK(this)){
                    regIntent = Intent(applicationContext, MapsActivity::class.java)
                    startActivityForResult(regIntent, LAUNCH_SECOND_ACTIVITY)
                }
            }
        }
    }

    private fun goToRegister() {
        binding.loginName?.visibility = View.VISIBLE
        binding.addressLayout.visibility = View.VISIBLE
        binding.login.text = resources.getString(R.string.register)
        binding.donorSwitchRegister.visibility = View.GONE
        binding.donorSwitchLogin.visibility = View.VISIBLE
    }

    private fun goToLogin() {
        binding.loginName?.visibility = View.INVISIBLE
        binding.addressLayout.visibility = View.INVISIBLE
        binding.login.text = resources.getString(R.string.login)
        binding.donorSwitchLogin.visibility = View.GONE
        binding.donorSwitchRegister.visibility = View.VISIBLE
    }


    //Initialize everything
    private fun init() {
        activityBinding = ActivityDonorLoginBinding.inflate(layoutInflater)
        val view = binding.root

        binding.login.setOnClickListener(this)
        binding.donorSwitchLogin.setOnClickListener(this)
        binding.donorSwitchRegister.setOnClickListener(this)
        binding.addressLayout.setOnClickListener(this)

        subscribeObservers()
        setContentView(view)
    }


    //register method
    private fun register() {
        when {
            binding.loginName?.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.enter_name), Toast.LENGTH_SHORT
                ).show()
            }
            binding.loginAddress?.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.enter_address), Toast.LENGTH_SHORT
                ).show()
            }
            binding.loginPhone.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.enter_phone), Toast.LENGTH_SHORT
                ).show()
            }
            binding.loginPass.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.short_password), Toast.LENGTH_SHORT
                ).show()
            }
            binding.loginPass.text.toString().length < 6 -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.enter_password), Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                val donor = Donor(
                    null,
                    binding.loginName?.text.toString(),
                    binding.loginAddress?.text.toString(),
                    binding.loginPhone.text.toString(),
                    binding.loginPass.text.toString(),
                    "",
                    true
                )
                loginActivityViewModel.setStateEvent(
                    MainStateEvent.RegisterDonor(
                       donor
                    )
                )
                displayProgressBar(true)
            }
        }
    }

    //Login method
    private fun login() {
        when {
            binding.loginPhone.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.enter_phone),
                    Toast.LENGTH_SHORT
                ).show()
            }
            binding.loginPass.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.enter_password),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                loginActivityViewModel.setStateEvent(
                    MainStateEvent.LoginDonor(
                        binding.loginPhone.text.toString(),
                        binding.loginPass.text.toString()
                    )
                )
                displayProgressBar(true)
            }
        }
    }

    private fun subscribeObservers(){
        loginActivityViewModel.dataStateDonor.observe(this, Observer { dataState ->
            when(dataState){
                is DataState.Success<Donor?> -> {
                    displayProgressBar(false)
                    if(dataState.data != null){
                        goToDonor(dataState.data)
                        displayMessage("${resources.getString(R.string.welcome)} ${dataState.data.name}")
                    }else{
                        displayMessage(resources.getString(R.string.wrong_account))
                    }
                }
                is DataState.Error -> {
                    displayProgressBar(false)
                    displayMessage(dataState.exception.message)
                }
                is DataState.Loading -> {
                    displayProgressBar(true)
                }
            }
        })
    }

    private fun displayMessage(message: String?){
        if(message != null){
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun displayProgressBar(isDisplayed: Boolean){
        if(isDisplayed){
            binding.login.visibility = View.GONE
            binding.loginProgress.visibility = View.VISIBLE
        }else {
            binding.login.visibility = View.VISIBLE
            binding.loginProgress.visibility = View.GONE
        }
    }


    private fun goToDonor(it: Donor) {
        val intent = Intent(
            applicationContext,
            DonorMainActivity::class.java
        )

        val pref = UsefulMethodsUtil

        pref.apply {
            saveObjectToSharedPreference(
                applicationContext,
                Constants.PREF_NAME,
                Constants.OBJECT,
                it
            )

            saveStringToSharedPreferences(
                applicationContext,
                Constants.PREF_NAME,
                Constants.TYPE,
                Constants.DONOR
            )
        }
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            when(resultCode) {
                Activity.RESULT_OK -> {
                    binding.loginAddress?.text = data?.getStringExtra(REGISTERED_LOCATION)
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(
                        applicationContext,
                        "No location was picked",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    //save the data to restore it when someone rotate the screen
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("NAME", binding.loginName?.text.toString())
        outState.putString("PHONE", binding.loginPhone.text.toString())
        outState.putString("PASSWORD", binding.loginPass.text.toString())
        outState.putString("ADDRESS", binding.loginAddress?.text.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        activityBinding = null
        regIntent = null
    }
}
