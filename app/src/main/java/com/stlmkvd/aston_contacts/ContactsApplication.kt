package com.stlmkvd.aston_contacts

import android.app.Application

class ContactsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FakeRepository.init(contentResolver)
    }
}