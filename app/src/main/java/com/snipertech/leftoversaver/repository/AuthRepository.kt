package com.snipertech.leftoversaver.repository

import android.util.Log
import com.snipertech.leftoversaver.model.Donor
import com.snipertech.leftoversaver.model.Volunteer
import com.snipertech.leftoversaver.retrofit.LeftoverSaverApi
import com.snipertech.leftoversaver.util.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository(private val retrofit: LeftoverSaverApi) {

    //register a new donor account
    suspend fun insertDonor(donor: Donor): Flow<DataState<Donor?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.insertDonor(donor)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "response.body()")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }

    //register a new volunteer account
    suspend fun insertVol(vol: Volunteer): Flow<DataState<Volunteer?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.insertVolunteer(vol)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "response.body()")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }

    //.........................................................................................
    //Check if the user is in the database or not
    suspend fun searchDonor(phoneNumber: String, password: String): Flow<DataState<Donor?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.searchDonor(phoneNumber, password)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "response.body()")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }


    //Check if the user is in the database or not
    suspend fun searchVol(phoneNumber: String): Flow<DataState<Volunteer?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.searchVolunteer(phoneNumber)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "response.body()")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }

}