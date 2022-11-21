package com.stlmkvd.aston_contacts

import android.graphics.Bitmap

data class Contact(
    val id: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var phoneNumber: String? = null,
    var email: String? = null,
    var thumbnailPhoto: ByteArray? = null
) : java.io.Serializable {

    val isEmpty: Boolean
        get() {
            return firstName == null &&
                    lastName == null &&
                    phoneNumber == null &&
                    email == null &&
                    thumbnailPhoto == null
        }

    fun getDisplayedName(): String{
            return "${firstName ?: ""} ${lastName ?: ""}".trim()
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Contact

        if (id != other.id) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (phoneNumber != other.phoneNumber) return false
        if (email != other.email) return false
        if (thumbnailPhoto != null) {
            if (other.thumbnailPhoto == null) return false
            if (!thumbnailPhoto.contentEquals(other.thumbnailPhoto)) return false
        } else if (other.thumbnailPhoto != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (firstName?.hashCode() ?: 0)
        result = 31 * result + (lastName?.hashCode() ?: 0)
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (thumbnailPhoto?.contentHashCode() ?: 0)
        return result
    }

    companion object {
        const val ARG_KEY = "contact"
    }
}