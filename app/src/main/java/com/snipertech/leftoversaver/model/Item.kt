package com.snipertech.leftoversaver.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.bson.types.ObjectId

@Parcelize
class Item(
    val uuid: ObjectId?,
    val name: String,
    var amount: Double,
    val donor: Donor,
    var imageUrl: String
): Parcelable