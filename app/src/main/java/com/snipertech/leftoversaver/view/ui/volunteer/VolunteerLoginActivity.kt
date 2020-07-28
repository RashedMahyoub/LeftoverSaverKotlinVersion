package com.snipertech.leftoversaver.view.ui.volunteer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.ActivityVolunteerLoginBinding
import com.snipertech.leftoversaver.model.VolHistoryOrders
import com.snipertech.leftoversaver.model.Volunteer
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.DataState
import com.snipertech.leftoversaver.util.UsefulMethodsUtil
import com.snipertech.leftoversaver.viewModel.VolunteerMainStateEvent
import com.snipertech.leftoversaver.viewModel.VolunteerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class VolunteerLoginActivity : AppCompatActivity(), View.OnClickListener {
    private var activityBinding: ActivityVolunteerLoginBinding? = null
    private val binding get() = activityBinding!!
    private val viewModel: VolunteerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        binding.loginName.setText(savedInstanceState?.getString("NAME"))
        binding.loginPhone.setText(savedInstanceState?.getString("PHONE"))
    }

    private fun init(){
        activityBinding = ActivityVolunteerLoginBinding.inflate(layoutInflater)

        binding.volSwitchRegister.setOnClickListener(this)
        binding.volSwitchLogin.setOnClickListener(this)
        binding.loginRegister.setOnClickListener(this)
        registerObservers()
        setContentView(binding.root)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.vol_switch_login ->{
                goToLogin()
            }
            R.id.vol_switch_register ->{
                goToRegister()
            }
            R.id.login_register ->{
                if (binding.loginRegister.text.toString() == resources.getString(R.string.login)) {
                    login()
                } else {
                    register()
                }
            }
        }
    }

    private fun login() {
        when {
            binding.loginPhone.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.enter_phone),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else ->{
                viewModel.setStateEvent(
                    VolunteerMainStateEvent.SearchVolunteer(binding.loginPhone.text.toString())
                )
            }
        }
    }


    private fun register() {
        when {
            binding.loginName.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.enter_name),
                    Toast.LENGTH_SHORT
                ).show()
            }
            binding.loginPhone.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.enter_phone),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                val vol = Volunteer(
                    null,
                    binding.loginName.text.toString(),
                    binding.loginPhone.text.toString(),
                    VolHistoryOrders(listOf())
                )
                viewModel.setStateEvent(VolunteerMainStateEvent.RegisterVolunteer(vol))
            }
        }
    }


    private fun goToRegister() {
        binding.loginName.visibility = View.VISIBLE
        binding.volSwitchLogin.visibility = View.VISIBLE
        binding.volSwitchRegister.visibility = View.GONE
        binding.loginRegister.text = resources.getString(R.string.register)
    }

    private fun goToLogin() {
        binding.loginName.visibility = View.INVISIBLE
        binding.volSwitchLogin.visibility = View.GONE
        binding.volSwitchRegister.visibility = View.VISIBLE
        binding.loginRegister.text = resources.getString(R.string.login)
    }

    private fun goToVol(it: Volunteer) {
        val intent = Intent(
            applicationContext,
            VolunteerMainActivity::class.java
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
                Constants.VOL
            )
        }
        startActivity(intent)
        finish()
    }

    private fun registerObservers(){
        viewModel.dataStateVolunteer.observe(this, Observer {dataState ->
            when (dataState) {
                is DataState.Success<Volunteer?> -> {
                    Log.d("WOW", "SUCCESS")
                    binding.loginProgress.visibility = View.GONE
                    binding.loginRegister.visibility = View.VISIBLE
                    if(dataState.data != null){
                        goToVol(dataState.data)
                        displayMessage("${resources.getString(R.string.welcome)} ${dataState.data.name}")
                    }else{
                        displayMessage(resources.getString(R.string.wrong_account))
                    }
                }
                is DataState.Error -> {
                    Log.d("WOW", "NOPE")
                    binding.loginProgress.visibility = View.GONE
                    binding.loginRegister.visibility = View.VISIBLE
                    displayMessage(resources.getString(R.string.error))
                }
                is DataState.Loading -> {
                    Log.d("WOW", "LOADING")
                    binding.loginRegister.visibility = View.GONE
                    binding.loginProgress.visibility = View.VISIBLE
                }
            }
        })
    }

    //Display a message
    private fun displayMessage(message: String) {
        Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("NAME", binding.loginName.text.toString())
        outState.putString("PHONE", binding.loginPhone.text.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        activityBinding = null
    }
}