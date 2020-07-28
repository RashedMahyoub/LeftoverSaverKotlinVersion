package com.snipertech.leftoversaver.retrofit

import com.snipertech.leftoversaver.model.*
import retrofit2.Response
import retrofit2.http.*

interface LeftoverSaverApi {
    //Donor
    @POST("donor/add")
    suspend fun insertDonor(@Body donor: Donor?): Response<Donor>

    @GET("donor/{phoneNumber}/{password}/get")
    suspend fun searchDonor(
        @Path("phoneNumber") username: String?,
        @Path("password") pass: String?
    ): Response<Donor>

    @GET("donor/{city}/getCertain")
    suspend fun getDonorsByCity(@Path("city") city: String?): Response<List<Donor>>

    @GET("donor/{phone}/{pass}/{imageUrl}/update")
    suspend fun updateDonor(
        @Path("phone") email: String?,
        @Path("pass") password: String?,
        @Path("imageUrl") profImage: String?
    ): Response<Donor>

    @GET("donor/{phone}/{status}/update")
    suspend fun updateDonorStatus(
        @Path("phone") email: String?,
        @Path("status") status: Boolean?
    ): Response<Boolean>

    //.................................................................................
    //Volunteer
    @POST("volunteer/add")
    suspend fun insertVolunteer(@Body volunteer: Volunteer?): Response<Volunteer>

    @GET("volunteer/{phoneNumber}/get")
    suspend fun searchVolunteer(
        @Path("phoneNumber") phoneNumber: String?
    ): Response<Volunteer>

    @POST("volunteer/{phoneNumber}/update")
    suspend fun updateVol(
        @Path("phoneNumber") phoneNumber: String?,
        @Body list: VolHistoryOrders?
    ): Response<Boolean>
    //.................................................................................
    //Item
    @POST("item/add")
    suspend fun insertItem(@Body item: Item): Response<Boolean>

    @GET("item/{id}/getCertain")
    suspend fun getItemLists(@Path("id") Id: String?): Response<List<Item>>

    @GET("item/{id}/{name}/del")
    suspend fun delItem(
        @Path("id") donorId: String?,
        @Path("name") itemName: String?
    ): Response<Boolean>

    @GET("item/{phone}/{name}/{amount}/update")
    suspend fun updateItem(
        @Path("phone") donorPhone: String?,
        @Path("name") name: String?,
        @Path("amount") amount: Double?
    ): Response<Boolean>
    //.................................................................................
    //Order
    @POST("order/add")
    suspend fun insertOrder(@Body order: Order?): Response<Order>

    @GET("order/{phone}/get")
    suspend fun getOrder(@Path("phone") phoneNumber: String?): Response<Order>

    @GET("order/{phoneNumber}/update")
    suspend fun updateOrder(
        @Path("phoneNumber") phoneNumber: String?
    ): Response<Boolean>

    @GET("order/{phoneNumber}/{vol}/orderUpdate")
    suspend fun updateOrderVol(
        @Path("phoneNumber") phoneNumber: String?,
        @Path("vol") status: String?
    ): Response<Boolean>

    @GET("order/{city}/getAll")
    suspend fun getOrdersByCity(@Path("city") city: String?): Response<List<Order>>

    @GET("order/{phoneNumber}/getOrders")
    suspend fun getDonorOrders(@Path("phoneNumber") name: String?): Response<List<Order>>

    @GET("order/{id}/del")
    suspend fun delOrder(@Path("id") postId: String?): Response<Boolean>
    //.................................................................................
}