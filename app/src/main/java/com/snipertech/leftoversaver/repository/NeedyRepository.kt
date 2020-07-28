package com.snipertech.leftoversaver.repository

import android.util.Log
import com.snipertech.leftoversaver.model.Donor
import com.snipertech.leftoversaver.model.Item
import com.snipertech.leftoversaver.model.Order
import com.snipertech.leftoversaver.retrofit.LeftoverSaverApi
import com.snipertech.leftoversaver.util.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NeedyRepository(private val retrofit: LeftoverSaverApi) {

    //get all donors in a certain city
    suspend fun getDonors(city: String): Flow<DataState<List<Donor>?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.getDonorsByCity(city)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "SUCCESS")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }

    //get items for the donor
    suspend fun getItemsByDonors(donorId: String): Flow<DataState<List<Item>?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.getItemLists(donorId)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "SUCCESS")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }


    //confirm order
    suspend fun updateItems(
        hashMap: HashMap<String, Double>,
        items: List<Item>,
        order: Order
    ): Flow<DataState<Order?>> = flow {
        emit(DataState.Loading)
        try {
            for (item in items) {
                for (n in hashMap.keys) {
                    if (item.name == n) {
                        val amount = hashMap[item.name]!!.minus(item.amount)
                        Log.d("TAG", amount.toString())
                        retrofit.updateItem(item.donor.phoneNumber, item.name, amount)
                    }
                }
            }

            val response = retrofit.insertOrder(order)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "SUCCESS")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }


    //confirm order
    suspend fun updateOrder(phoneNumber: String): Flow<DataState<Boolean?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.updateOrder(phoneNumber)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "SUCCESS")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }


    //confirm order
    suspend fun getOrder(id: String): Flow<DataState<Order?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.getOrder(id)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "SUCCESS")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }
}