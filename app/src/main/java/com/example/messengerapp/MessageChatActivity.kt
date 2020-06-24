package com.example.messengerapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.AdapterClasses.ChatsAdapter
import com.example.messengerapp.ModelClasses.Chat
import com.example.messengerapp.ModelClasses.Users
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*

class MessageChatActivity : AppCompatActivity() {

    var userIdVisit : String = ""
    var firebaseUser:FirebaseUser?=null
    var chatsAdapter:ChatsAdapter? = null
    var mChatList:List<Chat>?=null
lateinit var recycler_view_chats :RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recycler_view_chats = findViewById(R.id.recycler_view_chats)
        recycler_view_chats.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_view_chats.layoutManager = linearLayoutManager



        //below code is for setting image and username
        val reference = FirebaseDatabase.getInstance().reference.child("Users")
            .child(userIdVisit)
        reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

            val user: Users ? = p0.getValue(Users::class.java)
                username_mchat.text = user!!.getUserName()
Picasso.get().load(user.getProfile()).into(profile_image_mchat)
retrieveMessages(firebaseUser!!.uid,userIdVisit,user.getProfile())

            }
            override fun onCancelled(p0: DatabaseError) {

            }

        })


//below is for sending message
        send_message_btn.setOnClickListener {
            val message = text_message.text.toString()

            if (message==""){
Toast.makeText(this@MessageChatActivity,"Write something ...",Toast.LENGTH_SHORT).show()
            }
      else{
                sendMessageToUser(firebaseUser!!.uid,userIdVisit,message)

            }
text_message.setText("")

        }

        attach_image_file_btn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick Image"),438)

        }

    }



    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String) {

        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key
        val messageHashMap = HashMap<String,Any?>()

        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey


reference.child("Chats").child(messageKey!!).setValue(messageHashMap)
    //listener is for last seen
    .addOnCompleteListener {
        task ->
        if (task.isSuccessful){

            val chatsListReference  = FirebaseDatabase.getInstance()
                .reference
                .child("ChatList")
                .child(firebaseUser!!.uid)
                .child(userIdVisit)

            chatsListReference.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    if (!p0.exists()){
                        chatsListReference.child("id").setValue(userIdVisit)
                    }

                    val chatsListReceiverRef  = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatList")
                        .child(userIdVisit)
                        .child(firebaseUser!!.uid)

                    chatsListReceiverRef.child("id").setValue(firebaseUser!!.uid)


                }

                override fun onCancelled(p0: DatabaseError) {

                }


            })


            //below is for push noti using fcm
        val reference = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(firebaseUser!!.uid)
        }
    }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 438 && resultCode == RESULT_OK && data!=null &&data!!.data!=null){

            val progressBar = ProgressDialog(this)
            progressBar.setMessage("image is uploading, please wait...")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chats Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)
            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ taask ->
                if (!taask.isSuccessful){
                    taask.exception?.let {
                        throw it
                    }
                }
                return@Continuation  filePath.downloadUrl
            })
                .addOnCompleteListener {
                    task ->
                    if (task.isSuccessful){

                        val downloadUrl = task.result
                        val url = downloadUrl.toString()

                        val messageHashMap = HashMap<String,Any?>()
                        messageHashMap["sender"] = firebaseUser!!.uid
                        messageHashMap["message"] = "Sent you a message"
                        messageHashMap["receiver"] = userIdVisit
                        messageHashMap["isseen"] = false
                        messageHashMap["url"] = url
                        messageHashMap["messageId"] = messageId

                        ref.child("Chats").child(messageId!!).setValue(messageHashMap)

                        progressBar.dismiss()

                    }
                }


        }



    }

    private fun retrieveMessages(senderId: String,recieverId: String?, recieverImageUrl: String?) {

        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (snapshot in p0.children)
                {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(senderId)&&chat.getSender().equals(recieverId)
                        ||chat!!.getReceiver().equals(recieverId)&&chat.getSender().equals(senderId)
                    )
                    {
                        ( mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatsAdapter = ChatsAdapter(this@MessageChatActivity,( mChatList as ArrayList<Chat>),recieverImageUrl!! )
recycler_view_chats.adapter = chatsAdapter

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}
















