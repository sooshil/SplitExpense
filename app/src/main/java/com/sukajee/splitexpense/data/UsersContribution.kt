package com.sukajee.splitexpense.data

import android.os.Parcel
import android.os.Parcelable

data class UsersContribution(val userId: String, val usersName: String, val amount: String, val percentage: Float) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readFloat()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(usersName)
        parcel.writeString(amount)
        parcel.writeFloat(percentage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UsersContribution> {
        override fun createFromParcel(parcel: Parcel): UsersContribution {
            return UsersContribution(parcel)
        }

        override fun newArray(size: Int): Array<UsersContribution?> {
            return arrayOfNulls(size)
        }
    }
}