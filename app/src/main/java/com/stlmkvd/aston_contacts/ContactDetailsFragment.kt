package com.stlmkvd.aston_contacts

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.stlmkvd.aston_contacts.databinding.FragmentContactDetailsBinding

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactDetailsBinding.inflate(inflater, container, false)
        binding.contact = contact
        binding.executePendingBindings()
        contact.thumbnailPhoto?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            binding.ivPhoto.setImageBitmap(bitmap)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSaveContact.setOnClickListener { onSaveButtonPressed() }
        binding.btnDeleteContact.setOnClickListener { onDeleteButtonPressed() }
        binding.ivPhoto.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "photo adding will be implemented soon",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        writeChangesToObj(contact)
        outState.putSerializable(ARG_SERIALIZED_CONTACT, contact)
        super.onSaveInstanceState(outState)
    }

    private fun writeChangesToObj(contact: Contact) {
        contact.apply {
            firstName = binding.etFirstname.editText?.text?.toString()
            lastName = binding.etLastName.editText?.text?.toString()
            phoneNumber = binding.etPhone.editText?.text?.toString()
            email = binding.etEmail.editText?.text?.toString()
            //TODO add photo serialization
        }
    }

    private fun onDeleteButtonPressed() {
        val bundle = Bundle().apply { putSerializable(ARG_SERIALIZED_CONTACT, contact) }
        setFragmentResult(DELETE_CONTACT_REQUEST_KEY, bundle)
    }

    private fun onSaveButtonPressed() {
        writeChangesToObj(contact)
        val bundle = Bundle().apply { putSerializable(ARG_SERIALIZED_CONTACT, contact) }
        setFragmentResult(SAVE_CONTACT_REQUEST_KEY, bundle)
        view?.clearFocus()
    }

    companion object {
        const val TAG = "ContactDetailsFragment"
    }
}