package com.stlmkvd.aston_contacts

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.stlmkvd.aston_contacts.databinding.FragmentContactDetailsBinding
import kotlin.reflect.KMutableProperty1

class ContactDetailsFragment : Fragment() {

    private lateinit var binding: FragmentContactDetailsBinding
    private lateinit var contact: Contact

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contact = when {
            savedInstanceState != null -> savedInstanceState.getSerializable(ARG_SERIALIZED_CONTACT) as Contact?
                ?: throw IllegalArgumentException("you should pass serialized contact to the bundle")
            arguments != null -> requireArguments().getSerializable(ARG_SERIALIZED_CONTACT) as Contact?
                ?: throw IllegalArgumentException("you should pass serialized contact to the bundle")
            else -> Contact()
        }
        Log.d(TAG, contact.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactDetailsBinding.inflate(inflater, container, false)
        binding.contact = contact
        binding.executePendingBindings()
        contact.thumbnailPhoto?.let {
            binding.ivPhoto.setImageBitmap(it)
        }
        Log.d(TAG, "onCreateView")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSaveContact.setOnClickListener { onSaveButtonPressed() }
        binding.btnDeleteContact.setOnClickListener { onDeleteButtonPressed() }
        binding.etFirstname.editText?.addTextChangedListener(FieldWatcher(contact, Contact::firstName))
        binding.etLastName.editText?.addTextChangedListener(FieldWatcher(contact, Contact::lastName))
        binding.etPhone.editText?.addTextChangedListener(FieldWatcher(contact, Contact::phoneNumber))
        binding.etEmail.editText?.addTextChangedListener(FieldWatcher(contact, Contact::email))
        binding.ivPhoto.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "photo adding will be implemented soon",
                Toast.LENGTH_SHORT
            ).show()
        }
        Log.d(TAG, "onViewCreated")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(ARG_SERIALIZED_CONTACT, contact)
        super.onSaveInstanceState(outState)
    }

    private fun onDeleteButtonPressed() {
        val bundle = Bundle().apply { putSerializable(ARG_SERIALIZED_CONTACT, contact) }
        setFragmentResult(DELETE_CONTACT_REQUEST_KEY, bundle)
    }

    private fun onSaveButtonPressed() {
        val bundle = Bundle().apply { putSerializable(ARG_SERIALIZED_CONTACT, contact) }
        setFragmentResult(SAVE_CONTACT_REQUEST_KEY, bundle)
        view?.clearFocus()
    }

    private class FieldWatcher(val contact: Contact, val storageProperty: KMutableProperty1<Contact, String?>) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

        override fun afterTextChanged(s: Editable?) {
            storageProperty.set(contact, s?.toString()?.ifEmpty { null })
        }
    }

    companion object {
        const val TAG = "ContactDetailsFragment"
    }
}