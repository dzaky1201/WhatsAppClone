package com.dzakyhdr.whatsapp.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzakyhdr.whatsapp.R
import com.dzakyhdr.whatsapp.adapter.ContactsAdapter
import com.dzakyhdr.whatsapp.listener.ContactsClickListener
import com.dzakyhdr.whatsapp.util.Contact
import kotlinx.android.synthetic.main.activity_contacts.*

class ContactsActivity : AppCompatActivity(), ContactsClickListener {

    private val contactsList = ArrayList<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        setupList()
        getContacts()
    }

    private fun getContacts() {
        progress_layout_contact.visibility = View.VISIBLE
        contactsList.clear()
        val newList = ArrayList<Contact>()
        val phone = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, null, null,null
        )
        while (phone!!.moveToNext()){
            val name = phone.getString(
                phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            )
            val phoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            newList.add(Contact(name, phoneNumber))
        }
        progress_layout_contact.visibility = View.GONE

        contactsList.addAll(newList)
        phone.close()
    }

    private fun setupList() {
        progress_layout_contact.visibility = View.GONE
        val contactsAdapter = ContactsAdapter(contactsList)
        contactsAdapter.setOnItemClickListener(this)
        rv_contacts.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = contactsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun onContactClicked(name: String?, phone: String?) {
        val intent = Intent()
        intent.putExtra(MainActivity.PARAM_NAME, name)
        intent.putExtra(MainActivity.PARAM_PHONE, phone)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}