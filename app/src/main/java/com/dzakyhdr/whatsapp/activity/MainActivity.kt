package com.dzakyhdr.whatsapp.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dzakyhdr.whatsapp.R
import com.dzakyhdr.whatsapp.adapter.SectionPagerAdapter
import com.dzakyhdr.whatsapp.fragment.ChatsFragment
import com.dzakyhdr.whatsapp.listener.FailureCallback
import com.dzakyhdr.whatsapp.util.DATA_USERS
import com.dzakyhdr.whatsapp.util.DATA_USER_PHONE
import com.dzakyhdr.whatsapp.util.PERMISSION_REQUEST_READ_CONTACT
import com.dzakyhdr.whatsapp.util.REQUEST_NEW_CHATS
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*

class MainActivity : AppCompatActivity(), FailureCallback {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var mSectionAdapter: SectionPagerAdapter? = null
    private val firebaseDb = FirebaseFirestore.getInstance()
    private val chatsFragment = ChatsFragment()

    companion object{
        const val PARAM_NAME = "name"
        const val PARAM_PHONE = "phone"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chatsFragment.setFailureCallback(this)
        setSupportActionBar(toolbar)
        supportActionBar?.title = resources.getString(R.string.app_name)
        mSectionAdapter = SectionPagerAdapter(supportFragmentManager)

        container.adapter = mSectionAdapter

        fab.setOnClickListener {
            onNewChat()
        }

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        resizeTabs()
        tabs.getTabAt(1)?.select()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0 -> fab.hide()
                    1 -> fab.show()
                    2 -> fab.hide()
                }
            }
        })

    }

    private fun onNewChat() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)){
                AlertDialog.Builder(this)
                    .setTitle("Izin Kontak")
                    .setMessage("Aplikasi ingin mengakses Kontak di Hp anda")
                    .setPositiveButton("Ya"){dialog, which ->
                        requestContactPermission()
                    }
                    .setNegativeButton("Tidak"){dialog, which ->

                    }
                    .show()
            }
            else {
                requestContactPermission()
            }
        }else{
            startNewActivity()
        }
    }

    private fun startNewActivity() {
        val intent = Intent(this, ContactsActivity::class.java)
        startActivityForResult(intent, REQUEST_NEW_CHATS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Activity.RESULT_OK){
            when(requestCode){
                REQUEST_NEW_CHATS ->{
                    val name = data?.getStringExtra(PARAM_NAME) ?: ""
                    val phone = data?.getStringExtra(PARAM_PHONE) ?: ""
                    checkNewChatUser(name, phone)
                }
            }
        }
    }

    private fun checkNewChatUser(name: String, phone: String) {
        if (!name.isNullOrEmpty()&&!phone.isNullOrEmpty()){
            // mengakases tabel user di firestore ketika data phone sama dengan phone di kontak
            firebaseDb.collection(DATA_USERS)
                .whereEqualTo(DATA_USER_PHONE,phone)
                .get()
                .addOnSuccessListener {
                    //jika terdapat data lanjutkan dan memulai chat
                    if (it.documents.size > 0){
                        chatsFragment.newChat(it.documents[0].id)
                    }else{
                        //aksi ini dilakukak ketika user belum menggunakan whatsapp
                        AlertDialog.Builder(this).setTitle("User Tidak Ditemukan")
                                //mengirim pesan agar nomor tujuan menginstal whatsapp
                            .setMessage("$name akun tidak ditemukan, kirimkan pesan sms untuk menginstal aplikasi !")
                            .setPositiveButton("OK"){dialog, which ->
                                val intent = Intent(Intent.ACTION_VIEW) //intent implicit mengirim pesan
                                intent.data = Uri.parse("sms:$phone") //query untuk mengirim pesan
                                intent.putExtra("sms_body", "Hello guys jika ingin menginstal Whatsapp kamu bisa menginstallnya sekarang")
                                startActivity(intent)
                            }

                            .setNegativeButton("Cancel", null)
                            .setCancelable(false)
                            .show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this,"An Error occured, Please Try Again", Toast.LENGTH_SHORT).show()
                    it.printStackTrace()
                }
        }
    }

    private fun requestContactPermission() {
        ActivityCompat.requestPermissions(this,
        arrayOf(Manifest.permission.READ_CONTACTS), PERMISSION_REQUEST_READ_CONTACT)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_REQUEST_READ_CONTACT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startNewActivity()
                }
            }
        }
    }

    private fun resizeTabs() {
        val layout = (tabs.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.4f
        layout.layoutParams = layoutParams
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onUserError(){
        Toast.makeText(this, "User Not Found", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_logout -> onLogout()
            R.id.action_profile -> onProfile()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    private fun onLogout() {
        firebaseAuth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


}