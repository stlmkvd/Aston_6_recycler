package com.stlmkvd.aston_contacts

import android.util.Log
import androidx.lifecycle.ViewModel

private const val TAG = "ContactsViewModel"

class ContactsViewModel : ViewModel() {

    private val repository = Repository.getInstance()
    lateinit var contacts: MutableList<Contact>
        private set

    init {
        loadContacts()
    }

    fun loadContacts() {
        contacts = repository.loadContactsFromDataBase()
        Log.d(TAG, "contacts loaded")
    }

    fun updateExistingContact(contact: Contact): Int {
        repository.updateContactInDatabase(contact)
        val index = contacts.indexOfFirst { it.id == contact.id }
        contacts[index] = contact
        return index
    }

    fun deleteContact(contact: Contact): Int {
        repository.deleteContactFromDataBase(contact)
        val index = contacts.indexOfFirst { it.id == contact.id }
        contacts.removeAt(index)
        Log.d(TAG, index.toString())
        return index
    }

    fun saveNewContact(contact: Contact) {
        TODO()
    }
}