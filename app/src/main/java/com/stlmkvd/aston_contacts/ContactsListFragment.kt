package com.stlmkvd.aston_contacts

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.stlmkvd.aston_contacts.databinding.FragmentContactsListBinding
import com.stlmkvd.aston_contacts.databinding.ListItemContactBinding

const val SAVE_CONTACT_REQUEST_KEY = "contact_updated"
const val DELETE_CONTACT_REQUEST_KEY = "delete_contact"
const val ARG_SERIALIZED_CONTACT = "contact"
private const val ARG_FRAGMENT_RECREATED_MARKER = "recreated"

class ContactsListFragment : Fragment() {

    private lateinit var binding: FragmentContactsListBinding
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var viewModel: ContactsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ContactsViewModel::class.java)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsListBinding.inflate(inflater, container, false)
        contactsAdapter = ContactsAdapter()
        binding.recyclerView.adapter = contactsAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {

            override fun onChildViewAttachedToWindow(view: View) {
                showOrHideHint()
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                showOrHideHint()
            }
        })
        binding.slidingLayout.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED
        showOrHideHint()
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadContacts()
        contactsAdapter.submitList(viewModel.contacts)
        childFragmentManager.setFragmentResultListener(
            SAVE_CONTACT_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            binding.slidingLayout.closePane()
            val contact = bundle.getSerializable(ARG_SERIALIZED_CONTACT) as Contact?
                ?: throw IllegalArgumentException("you should put serialized contact in bundle")
            if (contact.id != null) {
                val index = viewModel.updateExistingContact(contact)
                contactsAdapter.notifyItemChanged(index)
            } else {
                val index = viewModel.saveNewContact(contact)
                TODO()
            }
        }
        childFragmentManager.setFragmentResultListener(
            DELETE_CONTACT_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            binding.slidingLayout.closePane()
            val contact = bundle.getSerializable(ARG_SERIALIZED_CONTACT) as Contact?
                ?: throw IllegalArgumentException("you should put serialized contact in bundle")
            if (contact.id != null) {
                val index = viewModel.deleteContact(contact)
                Log.d(TAG, index.toString())
                contactsAdapter.notifyItemRemoved(index)
            }
            val currFragment = childFragmentManager.findFragmentByTag(ContactDetailsFragment.TAG)
            currFragment?.let {
                childFragmentManager.commit {
                    remove(it)
                }
            }
        }

        with(requireActivity()) {
            onBackPressedDispatcher.addCallback {
                if (!binding.slidingLayout.closePane()) finish()
            }
        }
    }

    private fun showOrHideHint() {
        if (viewModel.contacts.isEmpty()) {
            binding.hint.visibility = View.VISIBLE
        } else binding.hint.visibility = View.GONE
    }

    private fun onContactSelected(contact: Contact) {
        val bundle = Bundle().apply { putSerializable(ARG_SERIALIZED_CONTACT, contact) }
        childFragmentManager.commit {
            replace(
                R.id.fragment_container_details,
                ContactDetailsFragment::class.java,
                bundle,
                ContactDetailsFragment.TAG
            )
            addToBackStack("")
            setReorderingAllowed(true)
        }
        binding.slidingLayout.openPane()
    }

    private inner class ContactsAdapter : ListAdapter<Contact, ContactHolder>(ContactDiffCallback) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
            val binding = ListItemContactBinding.inflate(layoutInflater, parent, false)
            binding.lifecycleOwner = this@ContactsListFragment.viewLifecycleOwner
            return ContactHolder(binding)
        }

        override fun onBindViewHolder(holder: ContactHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private inner class ContactHolder(private val binding: ListItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            binding.contact = contact
            binding.executePendingBindings()
            binding.root.setOnClickListener {
                onContactSelected(contact)
            }
        }
    }

    private object ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.phoneNumber == newItem.phoneNumber
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        const val TAG = "ContactsListFragment"
    }
}