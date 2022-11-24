package com.stlmkvd.aston_contacts

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import androidx.lifecycle.LifecycleOwner
import com.stlmkvd.aston_contacts.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        grantPermissons()
        if (savedInstanceState != null) {
            val fragment =
                supportFragmentManager.getFragment(savedInstanceState, ContactsListFragment.TAG)
            fragment?.let {
                supportFragmentManager.commit {
                    replace(
                        R.id.fragment_container_list,
                        it,
                        ContactsListFragment.TAG
                    )
                    addToBackStack("open contacts list")
                    setReorderingAllowed(true)
                }
            }

        } else {
            supportFragmentManager.commit {
                replace(
                    R.id.fragment_container_list,
                    ContactsListFragment::class.java,
                    null,
                    ContactsListFragment.TAG
                )
                addToBackStack("")
                setReorderingAllowed(true)
            }
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        val currFragment = supportFragmentManager.findFragmentByTag(ContactsListFragment.TAG)
        currFragment?.let {
            supportFragmentManager.putFragment(
                outState,
                ContactsListFragment.TAG,
                it
            )
        }
    }

    private fun grantPermissons() {
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED
        ) {
            val contactsPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
            contactsPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_CONTACTS,
                    android.Manifest.permission.WRITE_CONTACTS
                )
            )
        }
    }
}

fun Activity.setMenuProviderFor(menuProvider: MenuProvider, lifecycleOwner: LifecycleOwner) {
    val menuHost = this as MenuHost
    menuHost.addMenuProvider(menuProvider, lifecycleOwner)
}