package com.quickblox.sample.chatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.quickblox.sample.chatapp.Activites.ChatActivity;
import com.quickblox.sample.chatapp.Activites.ChatMessageActivity;
import com.quickblox.sample.chatapp.Activites.ShowProfileActivity;
import com.quickblox.sample.chatapp.Model.Chat;
import com.quickblox.sample.chatapp.R;
import com.quickblox.sample.chatapp.Model.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private ArrayList<Chat> chatList;

    public ChatListAdapter(ArrayList<Chat> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_chat, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);

        ChatListViewHolder rcv = new ChatListViewHolder(view);

        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatListViewHolder holder, final int position) {
        holder.mLstMsg.setText(chatList.get(position).getLastMsg());
        holder.mName.setText(chatList.get(position).getChatName());
        final Uri chatImage = chatList.get(position).getChatImage();

        if(chatImage != null)
        Glide.with(holder.context).load(chatImage).into(holder.mProfileImage);

        holder.mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(view.getContext(), ShowProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("chatId", chatList.get(holder.getAdapterPosition()).getChatId());
                bundle.putString("chatName",chatList.get(holder.getAdapterPosition()).getChatName());
                if(chatImage != null)
                bundle.putString("profileImageUrl", chatImage.toString());
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
            }
        });

        String time = chatList.get(position).getLastMsgTime();
        time = time.substring(0,5) + time.substring(8,11);
        holder.mLastMsgTime.setText(time);

        holder.chatContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(view.getContext(), ChatMessageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("chat", chatList.get(holder.getAdapterPosition()));
                view.getContext().startActivity(intent);
            }
        });


    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }


    public class ChatListViewHolder extends RecyclerView.ViewHolder {

        public TextView mName,mLstMsg,mLastMsgTime;
        public CircleImageView mProfileImage;
        public LinearLayout chatContainer;
        Context context;

        public ChatListViewHolder(View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.chat_name);
            mLstMsg = itemView.findViewById(R.id.last_msg);
            mLastMsgTime = itemView.findViewById(R.id.last_msg_time);
            mProfileImage = itemView.findViewById(R.id.chat_profile_image);
            chatContainer = itemView.findViewById(R.id.whole_chat_container);
            context = itemView.getContext();
        }
    }
}

