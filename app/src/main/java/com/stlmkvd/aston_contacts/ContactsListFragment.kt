package com.stlmkvd.aston_contacts

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.res.ResourcesCompat
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
        if (savedInstanceState != null) {
            val savedFragment =
                childFragmentManager.getFragment(savedInstanceState, ContactDetailsFragment.TAG)
            savedFragment?.let {
                childFragmentManager.commit {
                    replace(
                        R.id.fragment_container_details,
                        it,
                        ContactDetailsFragment.TAG
                    )
                }
            }
        }
        viewModel = ViewModelProvider(this).get(ContactsViewModel::class.java)
        Log.d(TAG, "oncreate")
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsListBinding.inflate(inflater, container, false)
        contactsAdapter = ContactsAdapter()
        binding.recyclerView.adapter = contactsAdapter
        binding.slidingLayout.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED
        Log.d(TAG, "oncreateview")
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
        showOrHideHint()
        Log.d(TAG, "onViewCreated")
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadContacts()
        contactsAdapter.submitList(viewModel.contacts)
        childFragmentManager.setFragmentResultListener(
            SAVE_CONTACT_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val contact = bundle.getSerializable(ARG_SERIALIZED_CONTACT) as Contact?
                ?: throw IllegalArgumentException("you should put serialized contact in bundle")
            saveContact(contact)
        }
        childFragmentManager.setFragmentResultListener(
            DELETE_CONTACT_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val contact = bundle.getSerializable(ARG_SERIALIZED_CONTACT) as Contact?
                ?: throw IllegalArgumentException("you should put serialized contact in bundle")
            deleteContact(contact)
        }

        //set onBackPressed listener in activity
        with(requireActivity()) {
            onBackPressedDispatcher.addCallback {
                val paneClosed = binding.slidingLayout.closePane()
                if (paneClosed) childFragmentManager.popBackStack()
                else finish()
            }
        }
        Log.d(TAG, "onStart")
    }

    private fun saveContact(contact: Contact) {
        closeDetailsIfCloseable()
        when {
            contact.isNew -> TODO("adding will be implemented")
            contact.hasAllDataEmpty -> deleteContact(contact)
            else -> {
                val index = viewModel.updateExistingContact(contact)
                contactsAdapter.notifyItemChanged(index)
            }
        }
    }

    private fun deleteContact(contact: Contact) {
        closeDetailsIfCloseable()
        childFragmentManager.popBackStack()
        if (!contact.isNew) {
            val index = viewModel.deleteContact(contact)
            contactsAdapter.notifyItemRemoved(index)
        }
    }

    private fun closeDetailsIfCloseable() {
        if (binding.slidingLayout.closePane()) childFragmentManager.popBackStack()
    }

    private fun openDetailsFor(contact: Contact) {
        val bundle = Bundle().apply { putSerializable(ARG_SERIALIZED_CONTACT, contact) }
        childFragmentManager.commit {
            replace(
                R.id.fragment_container_details,
                ContactDetailsFragment::class.java,
                bundle,
                ContactDetailsFragment.TAG
            )
            addToBackStack("open details")
            setReorderingAllowed(true)
        }
        binding.slidingLayout.openPane()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val currFragment = childFragmentManager.findFragmentByTag(ContactDetailsFragment.TAG)
        currFragment?.let {
            childFragmentManager.putFragment(
                outState,
                ContactDetailsFragment.TAG,
                it
            )
        }
    }

    private fun showOrHideHint() {
        if (viewModel.contacts.isEmpty()) {
            binding.hint.visibility = View.VISIBLE
        } else binding.hint.visibility = View.GONE
    }

    private inner class ContactsAdapter : ListAdapter<Contact, ContactHolder>(ContactDiffCallback) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
            val binding = ListItemContactBinding.inflate(layoutInflater, parent, false)
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
            if (contact.thumbnailPhoto == null) binding.ivPhoto.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.profile,
                    null
                )
            )
            else binding.ivPhoto.setImageBitmap(contact.thumbnailPhoto)
            binding.root.setOnClickListener {
                openDetailsFor(contact)
            }
        }
    }

    private object ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        const val TAG = "ContactsListFragment"
    }
}