package com.stlmkvd.aston_contacts

data class Contact(
    val id: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var phoneNumber: String? = null,
    var email: String? = null,
    var thumbnailPhoto: String? = null
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

    companion object {
        const val ARG_KEY = "contact"
    }
}