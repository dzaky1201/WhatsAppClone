package com.dzakyhdr.whatsapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dzakyhdr.whatsapp.R
import com.dzakyhdr.whatsapp.activity.ContactsActivity
import com.dzakyhdr.whatsapp.listener.ContactsClickListener
import com.dzakyhdr.whatsapp.util.Contact
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_contact.*

class ContactsAdapter(val contacts: ArrayList<Contact>) :
    RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    private var clickListener: ContactsClickListener? = null

    class ContactsViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bindItem(contact: Contact, listener: ContactsClickListener?){
            txt_contact_name.text = contact.name
            txt_contact_number.text = contact.phone

            itemView.setOnClickListener {
                listener?.onContactClicked(contact.name, contact.phone)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)=
        ContactsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false))

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.bindItem(contacts[position], clickListener)
    }

    fun setOnItemClickListener(listener: ContactsClickListener){
        clickListener = listener
        notifyDataSetChanged()
    }
}