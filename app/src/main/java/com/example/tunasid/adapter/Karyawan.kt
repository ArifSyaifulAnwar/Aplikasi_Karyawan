package com.example.tunasid.adapter

import android.os.Parcel
import android.os.Parcelable

data class Karyawan(
    val name: String,
    val tanggal: String,
    val status: String,
    val terlambat: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(tanggal)
        parcel.writeString(status)
        parcel.writeString(terlambat)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Karyawan> {
        override fun createFromParcel(parcel: Parcel): Karyawan {
            return Karyawan(parcel)
        }

        override fun newArray(size: Int): Array<Karyawan?> {
            return arrayOfNulls(size)
        }
    }
}


