package com.quickblox.sample.chatapp.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.quickblox.sample.chatapp.Model.Message;
import com.quickblox.sample.chatapp.Model.Chat;
import com.quickblox.sample.chatapp.R;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;


public class MessageListAdapter extends RecyclerView.Adapter {

    private ArrayList<Message> msgList;


    public MessageListAdapter(ArrayList<Message> msgList) {
        this.msgList = msgList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view ;

        switch (viewType)
        {
            case Message.SENT:
                view = inflater.inflate(R.layout.item_message_sent, parent,false);
                return new SentMessageViewHolder(view);
                case Message.RECEIVED:
                    view = inflater.inflate(R.layout.item_message_received, parent,false);
                    return new RcvdMessageViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        final Message msg = msgList.get(position);

        if(msg != null)
        {
            switch (msg.getType())
            {
                case Message.SENT:
                    final ArrayList<String> uriList = msgList.get(((SentMessageViewHolder)holder).getAdapterPosition()).getMediaUrlList();

                    if(msgList.get(((SentMessageViewHolder)holder).getAdapterPosition()).getMediaUrlList().isEmpty())
                        ((SentMessageViewHolder)holder).media.setVisibility(View.GONE);
                    else{

                        if(!uriList.isEmpty())
                        {
                            ((SentMessageViewHolder)holder).media.setVisibility(View.VISIBLE);
                            Glide.with(((SentMessageViewHolder)holder).context).load(Uri.parse(uriList.get(0))).into(((SentMessageViewHolder)holder).media);
                            ((SentMessageViewHolder)holder).media.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    new ImageViewer.Builder(view.getContext(), uriList)
                                            .setStartPosition(0)
                                            .show();
                                }
                            });

                        }
                    }
                    ((SentMessageViewHolder)holder).mMsgTxt.setText(msg.getMessageText());
                    ((SentMessageViewHolder)holder).mTime.setText(msg.getMessageTime().substring(0,5) + msg.getMessageTime().substring(8,11));
                    break;
                case Message.RECEIVED:
                    final ArrayList<String> otherUriList = msgList.get(((RcvdMessageViewHolder)holder).getAdapterPosition()).getMediaUrlList();
                    if(msgList.get(((RcvdMessageViewHolder)holder).getAdapterPosition()).getMediaUrlList().isEmpty())
                        ((RcvdMessageViewHolder)holder).media.setVisibility(View.GONE);
                    else
                    {
                        if(otherUriList.size() >  0)
                        {
                            ((RcvdMessageViewHolder)holder).media.setVisibility(View.VISIBLE);
                            Glide.with(((RcvdMessageViewHolder)holder).context).load(Uri.parse(otherUriList.get(0))).into(((RcvdMessageViewHolder)holder).media);
                            ((RcvdMessageViewHolder)holder).media.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    new ImageViewer.Builder(view.getContext(), otherUriList)
                                            .setStartPosition(0)
                                            .show();
                                }
                            });

                        }
                    }
                    ((RcvdMessageViewHolder)holder).mMsgTxt.setText(msg.getMessageText());
                    ((RcvdMessageViewHolder)holder).mTime.setText(msg.getMessageTime().substring(0,5) + msg.getMessageTime().substring(8,11));
                    break;


            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        switch (msgList.get(position).getType())
        {
            case 0:
                return Message.SENT;
            case 1:
                return Message.RECEIVED;
        }

        return position;
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(msgList.get(position).getMessageId());
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }


    class SentMessageViewHolder extends RecyclerView.ViewHolder {

         TextView mTime,mMsgTxt;
         ImageView media;
         Context context;
         SentMessageViewHolder(View itemView) {
            super(itemView);
             mTime = itemView.findViewById(R.id.textview_time);
             mMsgTxt = itemView.findViewById(R.id.textview_message);
             media = (ImageView) itemView.findViewById(R.id.media);
             context = itemView.getContext();
         }
    }


    public  class RcvdMessageViewHolder extends RecyclerView.ViewHolder {


        TextView mTime,mMsgTxt;
        ImageView media;
        Context context;

        RcvdMessageViewHolder(View itemView) {
            super(itemView);
            mTime = itemView.findViewById(R.id.textview_time);
            mMsgTxt = itemView.findViewById(R.id.textview_message);
            media = (ImageView) itemView.findViewById(R.id.media);
            context = itemView.getContext();
        }
    }

}

