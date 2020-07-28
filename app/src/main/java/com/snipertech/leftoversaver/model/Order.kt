package com.snipertech.leftoversaver.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.bson.types.ObjectId

@Parcelize
class Order(
    val uuid: ObjectId?,
    val address: String,
    val itemList: List<Item>,
    val type: String,
    val needy: String,
    val stage: String,
    val volunteer: String?,
    val city: String
): Parcelable

