package com.snipertech.leftoversaver.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.bson.types.ObjectId

@Parcelize
class VolHistoryOrders(
    val list: List<Order>?
): Parcelable