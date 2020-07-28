package com.snipertech.leftoversaver.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.bson.types.ObjectId

@Parcelize
class Volunteer(
    val uuid: ObjectId?,
    val name: String,
    val phoneNumber: String,
    var history: VolHistoryOrders?
) : Parcelable
