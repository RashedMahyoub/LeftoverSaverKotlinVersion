package com.snipertech.leftoversaver.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.snipertech.leftoversaver.model.Donor
import com.snipertech.leftoversaver.model.Item
import com.snipertech.leftoversaver.model.Order
import com.snipertech.leftoversaver.retrofit.LeftoverSaverApi
import com.snipertech.leftoversaver.util.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DonorRepository constructor(
    private val retrofit: LeftoverSaverApi
) {
    val itemUpdatedLiveData = MutableLiveData<Boolean>()

    suspend fun getItems(id: String): Flow<DataState<List<Item>?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.getItemLists(id)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "SUCCESS")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }

    //get all orders in a for a certain donor
    suspend fun getOrders(phoneNumber: String): Flow<DataState<List<Order>?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.getDonorOrders(phoneNumber)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "BbbB")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }

    //delete item
    suspend fun deleteItem(item: Item): Flow<DataState<Boolean?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.delItem(item.donor.phoneNumber, item.name)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "SUCCESS")
                emit(DataState.Success(true))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }

    //update item amount
    suspend fun updateItem(
        donorPhone: String,
        itemName: String,
        itemAmount: Double
    ){
        try {
            val response = retrofit.updateItem(donorPhone,itemName, itemAmount)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "SUCCESS")
                itemUpdatedLiveData.postValue(response.body())
            }
        } catch (e: Exception) {
            itemUpdatedLiveData.postValue(false)
            Log.e("RETROFIT", e.message!!)
        }
    }

    //delete item
    suspend fun deleteOrder(order: Order): Flow<DataState<Boolean?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.delOrder(order.needy)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "SUCCESS")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }

    //add Item
    suspend fun addItem(item: Item): Flow<DataState<Boolean?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.insertItem(item)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "SUCCESS")
                emit(DataState.Success(response.body()))
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
            emit(DataState.Error(e))
        }
    }

    //add Item
    suspend fun update(phone: String, status: Boolean): Flow<DataState<Boolean?>> = flow {
        try {
            val response = retrofit.updateDonorStatus(phone, status)
            if (response.isSuccessful) {
                Log.d("RETROFIT", "SUCCESS")
            }
        } catch (e: Exception) {
            Log.e("RETROFIT", e.message!!)
        }
    }

    //change profile picture
    suspend fun changeProfilePic(
        phone: String,
        imageUrl: String,
        password: String
    ): Flow<DataState<Donor?>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofit.updateDonor(phone, password, imageUrl)
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