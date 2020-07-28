package com.snipertech.leftoversaver.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.snipertech.leftoversaver.model.Order
import com.snipertech.leftoversaver.model.VolHistoryOrders
import com.snipertech.leftoversaver.model.Volunteer
import com.snipertech.leftoversaver.retrofit.LeftoverSaverApi
import com.snipertech.leftoversaver.util.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class VolunteerRepository(private val retrofit: LeftoverSaverApi) {
    val updateVolunteerLiveData = MutableLiveData<String>()


    //get all orders in a certain city
    suspend fun getOrders(city: String): Flow<DataState<List<Order>?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.getOrdersByCity(city)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "SUCCESS)")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }

    suspend fun updateVol(order: Order, vol: Volunteer){
        try {
            updateVolunteerLiveData.postValue("Loading")
            //Get current vol orders
            val currentOrders = retrofit.searchVolunteer(vol.phoneNumber)
            val orders: ArrayList<Order>? = currentOrders.body()?.history?.list as ArrayList<Order>
            //Add the order to the vol list
            orders?.add(order)
            val history = VolHistoryOrders(orders)
            //Update the vol list
            val responseVol = retrofit.updateVol(vol.phoneNumber, history)
            if(responseVol.isSuccessful){
                //Update Order list
                val response = retrofit.updateOrderVol(order.needy, vol.phoneNumber)

                if (response.isSuccessful) {
                    Log.d("RETROFIT", "SUCCESS)")
                    updateVolunteerLiveData.postValue("Success")
                }
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            updateVolunteerLiveData.postValue("Error")
        }
    }
}