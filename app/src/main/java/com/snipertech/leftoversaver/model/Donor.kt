package com.snipertech.leftoversaver.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.bson.types.ObjectId

@Parcelize
class Donor(
    val uuid: ObjectId? = null,
    val name: String,
    val address: String,
    val phoneNumber: String,
    val password: String,
    var imageUrl: String,
    val empty: Boolean
) : Parcelable
