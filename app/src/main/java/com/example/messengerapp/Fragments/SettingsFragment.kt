package com.example.messengerapp.Fragments


import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.messengerapp.ModelClasses.Users

import com.example.messengerapp.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*


/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {

    var userReference: DatabaseReference?= null
    var firebaseUser : FirebaseUser ? = null
    private val rrequestCode = 438
    private var imageUri : Uri? = null
    private var storageRef: StorageReference?=null
    private var CoverChecker :String = ""
    private var socialChecker : String =""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view:View =  inflater.inflate(R.layout.fragment_settings, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference  = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")


        userReference!!.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot) {
if(p0.exists()){
    val user:Users? = p0.getValue(Users::class.java)
   if(context!=null){
       view.username_settings.text= user!!.getUserName()
       Picasso.get().load(user.getProfile()).into(view.profile_image_settings)
       Picasso.get().load(user.getCover()).into(view.cover_image_settings)
   }

}
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        }
        )

        view.profile_image_settings.setOnClickListener {
            pickImage()
        }

        view.cover_image_settings.setOnClickListener {
            CoverChecker = "cover"

            pickImage()
        }


        view.set_facebook.setOnClickListener {
            socialChecker = "facebook"
           setSocialLinks()

        }

        view.set_instagram.setOnClickListener {
            socialChecker = "instagram"
            setSocialLinks()

        }

        view.set_website.setOnClickListener {
            socialChecker = "website"
            setSocialLinks()

        }



        return view
    }  //onCreate view ended here so below are fun(s)


    private fun setSocialLinks() {
        val buider:AlertDialog.Builder = AlertDialog.Builder(context!!,R.style.Theme_AppCompat_DayNight_Dialog_Alert)
      var editText = EditText(context)

        if (socialChecker == "website")
        {
            buider.setTitle("Write url of your website")
            editText.hint="example - www.google.com"
        }
        else if (socialChecker == "facebook"){
            buider.setTitle("Enter facebook username")
            editText.hint="example - facebookUsername123"

        }
        else {
            // last cond.. for instagram
            buider.setTitle("Instagram")
            editText.hint="example - InstagramUsername123"

        }



        buider.setPositiveButton("Add link",DialogInterface.OnClickListener{
            dialog,which->
            var  string:String = editText.text.toString()
            if (string==""){
                Toast.makeText(context,"Enter something ..",Toast.LENGTH_SHORT).show()
            }else{
                saveSocialLink(string)
            }

        })

        buider.setNegativeButton("Cancel",DialogInterface.OnClickListener { dialogInterface, i ->
            dialogInterface.cancel()
        })
        buider.setView(editText)

        buider.show()

    }

    private fun saveSocialLink(s: String) {

        val map = HashMap<String,Any>()

        when(socialChecker){

            "facebook" ->
            { map["facebook"] = "https://m.facebook.com/$s" }

            "instagram" ->
            { map["instagram"] = "https://instagram.com/$s" }

            "website" ->
            { map["website"] = "https://$s" }

        }

         userReference!!.updateChildren(map)

        .addOnCompleteListener{
            task ->
            if(task.isSuccessful){
                Toast.makeText(context,"Link Uploaded Successfully",Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun pickImage() {
        val intent = Intent()
        intent.type="image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,rrequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == rrequestCode  && resultCode == Activity.RESULT_OK && data!!.data!=null ){

            imageUri = data.data
            Toast.makeText(context,"Uploading image...",Toast.LENGTH_LONG).show()
            uploadImageToDatabase()

        }
    }

    private fun uploadImageToDatabase() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("image is uploading, please wait...")
        progressBar.show()

    if (imageUri!=null){

        var fileRef = storageRef
        if (CoverChecker=="cover"){
            //" "  -->  means cover image now
            fileRef = storageRef!!.child("coverImageInFireStorage" + ".jpg" )

        }
        else{
            fileRef = storageRef!!.child("profileImageInFireStorage" + ".jpg" )
        }

        var uploadTask:StorageTask<*>
         uploadTask = fileRef.putFile(imageUri!!)
        uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ taask ->
            if (!taask.isSuccessful){
                taask.exception?.let {
                    throw it
                }
            }
            return@Continuation  fileRef.downloadUrl


        }).addOnCompleteListener {
            task ->
        if(task.isSuccessful){

            val downloadUrl = task.result
            val url = downloadUrl.toString()

            if (CoverChecker == "cover")
            {
                val map = HashMap<String,Any>()
                map["cover"] = url
                userReference!!.updateChildren(map)
                CoverChecker = ""

            }
            else { //this is for profile image
                val map = HashMap<String,Any>()
                map["profile"] = url
                userReference!!.updateChildren(map)

            }
progressBar.dismiss()

        }

        }

    }

    }


}
