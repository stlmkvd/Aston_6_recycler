package com.stlmkvd.aston_contacts

import android.content.ContentResolver

class FakeRepository private constructor(contentResolver: ContentResolver) :
    Repository(contentResolver) {

    private var contacts: MutableList<Contact>? = null
    private val generator = ContactsGenerator()


    override fun loadContactsFromDataBase(): MutableList<Contact> {
        if (contacts == null) {
            contacts = generator.generateContactsList(150).toMutableList()
        }
        return contacts!!
    }

    override fun updateContactInDatabase(contact: Contact) {
    }

    override fun deleteContactFromDataBase(contact: Contact) {
    }

    private inner class ContactsGenerator {

        val namesMale = listOf(
            "John",
            "Jacob",
            "Pavel",
            "Danil",
            "Victor",
            "Lol",
            "Kek",
            "Sanya",
            "Kostya",
            "Mark",
            "Vasya",
            "Aboba",
            "Vladimir",
            "Joe",
            "Jason"
        )
        val lastNamesMale = listOf(
            "Vinnik",
            "Smith",
            "Lolovich",
            "Kekovich",
            "Abobovich",
            "Putin",
            "Biden",
            "Obama",
            "Gardner",
            "Black",
            "Smirnov",
            "Ivanov",
            "Wick",
            "Statham"
        )
        val randomName: String
            get() {
                val index = (Math.random() * namesMale.size).toInt()
                return namesMale[index]
            }
        val randomLastName: String
            get() {
                val index = (Math.random() * lastNamesMale.size).toInt()
                return lastNamesMale[index]
            }
        val randomPhoneNumber: String
            get() {
                return buildString {
                    append("+79")
                    repeat(9) {
                        append((Math.random() * 10).toInt())
                    }
                }
            }

        fun generateContactsList(count: Int): List<Contact> {
            return buildList {
                repeat(count) { add(generateContact()) }
            }
        }

        private fun generateContact(): Contact {
            return Contact(idIncremental++.toString()).apply {
                firstName = randomName
                lastName = randomLastName
                phoneNumber = randomPhoneNumber
            }
        }
    }

    companion object {
        private var idIncremental = 0
        private var instance: FakeRepository? = null
        fun init(contentResolver: ContentResolver) {
            if (instance == null) instance = FakeRepository(contentResolver)
        }

        fun getInstance(): FakeRepository {
            if (instance != null) return instance!!
            else throw java.lang.IllegalStateException("Repository not initialized")
        }
    }

}