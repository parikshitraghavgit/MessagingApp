package com.example.messengerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth:FirebaseAuth
    private lateinit var refUsers:DatabaseReference
    private var firebaseUserId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar: Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title ="Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener{
            val intent = Intent(this@RegisterActivity,WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        register_btn.setOnClickListener {
            registerUser()
        }

    }

    private fun registerUser() {
        val username:String = username_register.text.toString()
        val email:String = email_register.text.toString()
        val password:String = password_register.text.toString()

        if(username == ""||email==""||password=="")
        {
            Toast.makeText(this@RegisterActivity,"Please fill all the fields...",Toast.LENGTH_LONG).show()
        }
        else{
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                task ->
                if(task.isSuccessful)
                {
                    firebaseUserId = mAuth.currentUser!!.uid
                    refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserId)

                    val userHashMap = HashMap<String,Any>()

                    userHashMap["uid"]=firebaseUserId
                    userHashMap["username"]=username
                    userHashMap["profile"]="https://firebasestorage.googleapis.com/v0/b/messengerapp-e19c2.appspot.com/o/profle.jpg?alt=media&token=9143386a-09a0-407d-a0df-827b0d5b829b"
                    userHashMap["cover"]="https://firebasestorage.googleapis.com/v0/b/messengerapp-e19c2.appspot.com/o/cover.jpg?alt=media&token=63bc057a-1d1b-40d0-aac7-6821739da965"
                    userHashMap["status"]="offline"
                    userHashMap["search"]=username.toLowerCase()
                    userHashMap["facebook"]="https://m.facebook.com"
                    userHashMap["instagram"]="https://m.instagram.com"
                    userHashMap["website"]="https://www.google.com"

                    refUsers.updateChildren(userHashMap)
                        .addOnCompleteListener {
                            task ->
                            if(task.isSuccessful){
                                val intent = Intent(this@RegisterActivity,MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }

                        }


                }
                else{
                    Toast.makeText(this@RegisterActivity,"Error Message "+task.exception!!.message,Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}
