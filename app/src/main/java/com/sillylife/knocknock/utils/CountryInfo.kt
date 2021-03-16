package com.sillylife.knocknock.utils

import android.os.Parcel
import android.os.Parcelable
import java.text.Collator
import java.util.*

class CountryInfo : Comparable<CountryInfo>, Parcelable {

    private val mCollator: Collator
    val locale: Locale?
    val countryCode: Int

    constructor(locale: Locale, countryCode: Int) {
        mCollator = Collator.getInstance(Locale.getDefault())
        mCollator.strength = Collator.PRIMARY
        this.locale = locale
        this.countryCode = countryCode
    }

    protected constructor(`in`: Parcel) {
        mCollator = Collator.getInstance(Locale.getDefault())
        mCollator.strength = Collator.PRIMARY

        locale = `in`.readSerializable() as Locale
        countryCode = `in`.readInt()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as CountryInfo?

        return countryCode == that!!.countryCode && if (locale != null) locale == that.locale else that.locale == null
    }

    override fun hashCode(): Int {
        var result = locale?.hashCode() ?: 0
        result = 31 * result + countryCode
        return result
    }

    override fun toString(): String {
        return localeToEmoji(locale!!) + " " + locale.displayCountry + " +" + countryCode
    }

    override fun compareTo(info: CountryInfo): Int {
        return mCollator.compare(locale!!.displayCountry, info.locale!!.displayCountry)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeSerializable(locale)
        dest.writeInt(countryCode)
    }


    fun localeToEmoji(locale: Locale): String {
        val countryCode = locale.country
        // 0x41 is Letter A
        // 0x1F1E6 is Regional Indicator Symbol Letter A
        // Example :
        // firstLetter U => 20 + 0x1F1E6
        // secondLetter S => 18 + 0x1F1E6
        // See: https://en.wikipedia.org/wiki/Regional_Indicator_Symbol
        val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }

    companion object CREATOR : Parcelable.Creator<CountryInfo> {
        override fun createFromParcel(parcel: Parcel): CountryInfo {
            return CountryInfo(parcel)
        }

        override fun newArray(size: Int): Array<CountryInfo?> {
            return arrayOfNulls(size)
        }
    }
}

