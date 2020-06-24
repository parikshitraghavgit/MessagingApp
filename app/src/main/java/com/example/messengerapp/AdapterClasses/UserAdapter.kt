package com.example.messengerapp.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.MainActivity
import com.example.messengerapp.MessageChatActivity
import com.example.messengerapp.ModelClasses.Users
import com.example.messengerapp.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter (mContext: Context
,mUsers:List<Users>,
isChatCheck:Boolean
):RecyclerView.Adapter<UserAdapter.ViewHolder?>()

{
     private val mContext: Context
     private val mUsers:List<Users>
     private val isChatCheck:Boolean

init{
    this.mUsers = mUsers
    this.isChatCheck = isChatCheck
    this.mContext = mContext
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
val view:View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout,parent,false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val user:Users = mUsers[position]
        holder.userNameTxt.text = user!!.getUserName()
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.profle).into(holder.profileImageView)

        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "send message",
                "visit profile"
            )
        val builder:AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("What Do u want ?")
            builder.setItems(options,DialogInterface.OnClickListener { dialogInterface, i ->
                if (i==0){//send message
                    val intent = Intent(mContext, MessageChatActivity::class.java)
               intent.putExtra("visit_id",user.getUID())
                    mContext.startActivity(intent)

                }
                else{//visit profile

                }

            })
            builder.show()
        }


    }

    class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){

        var userNameTxt: TextView
       var profileImageView: CircleImageView
        var onlineImageView: CircleImageView
        var offlineImageView: CircleImageView
        var lastMessageTxt: TextView

        init {
            userNameTxt = itemView.findViewById(R.id.username)
            profileImageView = itemView.findViewById(R.id.profile_image)
            onlineImageView = itemView.findViewById(R.id.image_online)
            offlineImageView = itemView.findViewById(R.id.image_offline)
            lastMessageTxt = itemView.findViewById(R.id.message_last)


        }

    }


}