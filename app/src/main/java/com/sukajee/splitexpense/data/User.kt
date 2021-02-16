package com.sukajee.splitexpense.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId

class User(

        @DocumentId
        var userid: String = "",
        var firstName: String = "",
        var lastName: String = "",
        var email: String = "",
        var phone: String = "",
        var circleCode: String = "",
        var totalContribution: Float = 0.0F,
        var totalLifeTimeContribution: Float = 0.0F,
        var lastSettlementDate: String = "",
        var circleTotal: Float = 0.0F,
        var circleHead: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readString().toString(),
            parcel.readFloat(),
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userid)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(email)
        parcel.writeString(phone)
        parcel.writeString(circleCode)
        parcel.writeFloat(totalContribution)
        parcel.writeFloat(totalLifeTimeContribution)
        parcel.writeString(lastSettlementDate)
        parcel.writeFloat(circleTotal)
        parcel.writeByte(if (circleHead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }


}