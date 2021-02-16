package com.sukajee.splitexpense.data

import android.os.Parcel
import android.os.Parcelable

class Transaction(
        var userId: String = "",
        var dataEntryDate: Long = 0L,
        var dateOfTransaction: Long = 0L,
        var circleCode: String = "",
        var description: String? = "",
        var storeName: String = "",
        var amount: Float = 0.0F,
        var note: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readFloat(),
            parcel.readString().toString()) {
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction {
            return Transaction(parcel)
        }

        override fun newArray(size: Int): Array<Transaction?> {
            return arrayOfNulls(size)
        }
    }


}