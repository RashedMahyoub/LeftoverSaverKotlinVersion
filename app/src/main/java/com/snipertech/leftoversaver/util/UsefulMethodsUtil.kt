package com.snipertech.leftoversaver.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.Gson
import com.snipertech.leftoversaver.util.Constants.ERROR_DIALOG_REQUEST
import com.snipertech.leftoversaver.util.Constants.TAG


object UsefulMethodsUtil {
    fun saveObjectToSharedPreference(
        context: Context,
        preferenceFileName: String?,
        serializedObjectKey: String?,
        obj: Any
    ) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(preferenceFileName, 0)
        val sharedPreferencesEditor = sharedPreferences.edit()
        val gson = Gson()
        val serializedObject = gson.toJson(obj)
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject)
        sharedPreferencesEditor.apply()
    }

    fun saveStringToSharedPreferences(
        context: Context,
        preferenceFileName: String?,
        preferenceKey: String?,
        s: String){
        val sharedPreferences =
            context.getSharedPreferences(preferenceFileName, 0)
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.putString(preferenceKey, s)
        sharedPreferencesEditor.apply()
    }

    fun getSavedStringFromPreference(
        context: Context,
        preferenceFileName: String?,
        preferenceKey: String?
    ): String?{
        val sharedPreferences =
            context.getSharedPreferences(preferenceFileName, 0)

        return sharedPreferences.getString(preferenceKey, "")
    }

    //Checking if the service is ok!
    fun isServicesOK(activity: Activity): Boolean {
        Log.d(TAG, "isServicesOK: checking google services version")
        val available =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)
        when {
            available == ConnectionResult.SUCCESS -> {
                //everything is fine and the user can make map requests
                Log.d(TAG, "isServicesOK: Google Play Services is working")
                return true
            }
            GoogleApiAvailability.getInstance().isUserResolvableError(available) -> {
                //an error occurred but we can resolve it
                Log.d(TAG, "isServicesOK: an error occurred but we can fix it")
                val dialog: Dialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(activity, available, ERROR_DIALOG_REQUEST)
                dialog.show()
            }
            else -> {
                Toast.makeText(
                    activity,
                    "You can't make map requests",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return false
    }

    fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
        observe(owner, object: Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer(value)
            }
        })
    }
}