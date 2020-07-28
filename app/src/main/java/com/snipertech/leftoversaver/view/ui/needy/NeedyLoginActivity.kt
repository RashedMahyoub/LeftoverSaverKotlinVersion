package com.snipertech.leftoversaver.view.ui.needy

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.ActivityNeedyLoginBinding
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.Constants.LAUNCH_SECOND_ACTIVITY
import com.snipertech.leftoversaver.util.Constants.REGISTERED_LOCATION
import com.snipertech.leftoversaver.util.UsefulMethodsUtil
import com.snipertech.leftoversaver.view.ui.MapsActivity

class NeedyLoginActivity : AppCompatActivity(), View.OnClickListener {

    private var activityBinding: ActivityNeedyLoginBinding? = null
    private val binding get() = activityBinding!!
    private var mIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        binding.needyPhone.setText(savedInstanceState?.getString("PHONE"))
        binding.needyAddress.text = savedInstanceState?.getString("ADDRESS")
    }

    private fun init(){
        activityBinding = ActivityNeedyLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.addressLayout.setOnClickListener(this)
        binding.enter.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.address_layout ->{
                if(UsefulMethodsUtil.isServicesOK(this)){
                    mIntent = Intent(applicationContext, MapsActivity::class.java)
                    startActivityForResult(mIntent, LAUNCH_SECOND_ACTIVITY)
                }
            }
            R.id.enter ->{
                goToNeedy()
            }
        }
    }

    private fun goToNeedy() {
        when {
            binding.needyPhone.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.enter_phone),
                    Toast.LENGTH_SHORT
                ).show()
            }
            binding.needyAddress.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.enter_address),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                mIntent = Intent(applicationContext, NeedyMainActivity::class.java)

                val pref = UsefulMethodsUtil

                pref.apply {
                    saveStringToSharedPreferences(
                        applicationContext,
                        Constants.PREF_NAME,
                        Constants.OBJECT,
                        binding.needyPhone.text.toString()
                    )

                    saveStringToSharedPreferences(
                        applicationContext,
                        Constants.PREF_NAME,
                        Constants.TYPE,
                        binding.needyAddress.text.toString()
                    )
                }
                startActivity(mIntent)
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            when(resultCode) {
                Activity.RESULT_OK -> {
                    binding.needyAddress.text = data?.getStringExtra(REGISTERED_LOCATION)
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
        outState.putString("PHONE", binding.needyPhone.text.toString())
        outState.putString("ADDRESS", binding.needyAddress.text.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        mIntent = null
        activityBinding = null
    }
}