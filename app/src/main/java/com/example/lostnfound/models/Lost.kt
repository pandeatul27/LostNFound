package com.example.lostnfound.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.FieldValue

data class Lost(
    val name:String="",
    val phone:String="",
    val place:String="",
    val description:String="",
    val image:String="",
    val user_id:String="",
    val user_email: String="",
    val date_time: String="",
    val item_lost: String="",
    val item_id: String="",
    val timestamp: FieldValue? =null

): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(phone)
        parcel.writeString(place)
        parcel.writeString(description)
        parcel.writeString(image)
        parcel.writeString(user_id)
        parcel.writeString(user_email)
        parcel.writeString(date_time)
        parcel.writeString(item_lost)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Lost> {
        override fun createFromParcel(parcel: Parcel): Lost {
            return Lost(parcel)
        }

        override fun newArray(size: Int): Array<Lost?> {
            return arrayOfNulls(size)
        }
    }

}
