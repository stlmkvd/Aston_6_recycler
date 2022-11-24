package com.stlmkvd.aston_contacts

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.BitmapFactory
import android.provider.ContactsContract

open class Repository protected constructor(private val contentResolver: ContentResolver) {

    open fun loadContactsFromDataBase(): MutableList<Contact> {
        val list: MutableList<Contact> = mutableListOf()
        val projection = arrayOf(
            ContactsContract.Data.RAW_CONTACT_ID,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Photo.PHOTO
        )
        val selection =
            "${ContactsContract.Data.MIMETYPE} IN (\"${ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE}\", " +
                    "\"${ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE}\", " +
                    "\"${ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE}\", " +
                    "\"${ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE}\")"
        val cursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
        cursor?.apply {
            val map: MutableMap<String, Contact> = mutableMapOf()
            moveToFirst()
            while (!isAfterLast) {
                val databaseId = getString(0)
                //as ContactsContract.Data table has different types in same column, we have to
                // do this trick with map and switch to fill contacts with valid data
                map.putIfAbsent(databaseId, Contact(databaseId))
                val contact = map[databaseId]!!
                val mime = getString(1)
                with(contact) {
                    when (mime) {
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {
                            firstName = getString(2)
                            middleName = getString(3)
                            lastName = getString(4)
                        }
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                            phoneNumber = getString(5)
                        }
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                            email = getString(6)
                        }
                        else -> {
                            thumbnailPhoto =
                                getBlob(7)?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                        }
                    }
                }
                moveToNext()
            }
            close()
            list.addAll(map.values)
        }
        return list
    }

    open fun updateContactInDatabase(contact: Contact) {
        val operations = ArrayList<ContentProviderOperation>()
        val uri = ContactsContract.Data.CONTENT_URI
        val selection =
            "${ContactsContract.Data.RAW_CONTACT_ID} = ${contact.id}"
        val removeOldEntries =
            ContentProviderOperation.newDelete(uri).withSelection(selection, arrayOf()).build()
        operations.add(removeOldEntries)
        if (contact.firstName != null || contact.middleName != null || contact.lastName != null) {
            val insertNameAndLastName = ContentProviderOperation.newInsert(uri)
                .withValues(ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, contact.id)
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                    put(
                        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                        contact.firstName
                    )
                    put(
                        ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
                        contact.middleName
                    )
                    put(
                        ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                        contact.lastName
                    )
                }).build()
            operations.add(insertNameAndLastName)
        }
        if (contact.phoneNumber != null) {
            val insertNewPhoneNumber =
                ContentProviderOperation.newInsert(uri)
                    .withValues(ContentValues().apply {
                        put(ContactsContract.Data.RAW_CONTACT_ID, contact.id)
                        put(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        )
                        put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.phoneNumber)
                    }).build()
            operations.add(insertNewPhoneNumber)
        }
        if (contact.email != null) {
            val insertNewEmail = ContentProviderOperation.newInsert(uri)
                .withValues(ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, contact.id)
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Email.ADDRESS, contact.email)
                }).build()
            operations.add(insertNewEmail)
        }
//        if (contact.thumbnailPhoto != null) {
//            val insertNewThumbnailPhoto = ContentProviderOperation.newInsert(uri)
//                .withValues(ContentValues().apply {
//                    put(ContactsContract.Data.RAW_CONTACT_ID, contact.id)
//                    put(
//                        ContactsContract.Data.MIMETYPE,
//                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
//                    )
//                    put(ContactsContract.CommonDataKinds.Photo.PHOTO, contact.thumbnailPhoto)
//                }).build()
//            operations.add(insertNewThumbnailPhoto) TODO PHOTO ADD
//        }
        contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
    }

    open fun deleteContactFromDataBase(contact: Contact) {
        contentResolver.delete(
            ContactsContract.RawContacts.CONTENT_URI,
            "${ContactsContract.RawContacts._ID} = ${contact.id}",
            null
        )
    }

    companion object {
        private lateinit var instance: Repository
        fun init(contentResolver: ContentResolver) {
            if (!this::instance.isInitialized) instance = Repository(contentResolver)
        }

        fun getInstance(): Repository =
            try {
                instance
            } catch (e: UninitializedPropertyAccessException) {
                throw java.lang.IllegalStateException("Repository not initialized")
            }
    }
}

