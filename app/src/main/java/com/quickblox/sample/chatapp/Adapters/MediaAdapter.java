package com.quickblox.sample.chatapp.Adapters;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.quickblox.sample.chatapp.R;

import java.util.ArrayList;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    Context context;
    ArrayList<String> uris;

    public MediaAdapter(Context context, ArrayList<String> uris)
    {
        this.context = context;
        this.uris = uris;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_media,null, false);
        MediaViewHolder mvh = new MediaViewHolder(view);

        return mvh;
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {

        Glide.with(context).load(Uri.parse(uris.get(position))).into(holder.media_item);

    }

    @Override
    public int getItemViewType(int position) {
        return position ;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return uris.size();
    }


    public class MediaViewHolder extends RecyclerView.ViewHolder {

        ImageView media_item;

        public MediaViewHolder(View itemView) {
            super(itemView);
            media_item = (ImageView) itemView.findViewById(R.id.media_item);
        }
    }



}
