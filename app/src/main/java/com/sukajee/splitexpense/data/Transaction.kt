package com.sukajee.splitexpense.data

import android.os.Parcel
import android.os.Parcelable

class Transaction : Parcelable {
    var userId : String?
    var dataEntryDate: Long
    var dateOfTransaction: Long
    var circleCode: String?
    var description: String?
    var storeName: String?
    var amount: Float
    var note: String?

    protected constructor(`in`: Parcel) {
        userId = `in`.readString()
        dataEntryDate = `in`.readLong()
        dateOfTransaction = `in`.readLong()
        circleCode = `in`.readString()
        description = `in`.readString()
        storeName = `in`.readString()
        amount = `in`.readFloat()
        note = `in`.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(userId)
        dest.writeLong(dataEntryDate)
        dest.writeLong(dateOfTransaction)
        dest.writeString(circleCode)
        dest.writeString(description)
        dest.writeString(storeName)
        dest.writeFloat(amount)
        dest.writeString(note)
    }

    override fun describeContents(): Int {
        return 0
    }


    constructor(userId : String?, dataEntryDate: Long, dateOfTransaction: Long, circleCode: String?, description: String?, storeName: String?, amount: Float, note: String?) {
        this.userId = userId
        this.dataEntryDate = dataEntryDate
        this.dateOfTransaction = dateOfTransaction
        this.circleCode = circleCode
        this.description = description
        this.storeName = storeName
        this.amount = amount
        this.note = note
    }

    companion object {
        val CREATOR: Parcelable.Creator<Transaction?> = object : Parcelable.Creator<Transaction?> {
            override fun createFromParcel(`in`: Parcel): Transaction? {
                return Transaction(`in`)
            }

            override fun newArray(size: Int): Array<Transaction?> {
                return arrayOfNulls(size)
            }
        }
    }
}