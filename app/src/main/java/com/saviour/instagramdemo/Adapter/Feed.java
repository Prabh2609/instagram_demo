package com.saviour.instagramdemo.Adapter;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.saviour.instagramdemo.Model.FeedModel;
import com.saviour.instagramdemo.R;

import java.util.ArrayList;

public class Feed extends FirebaseRecyclerAdapter<FeedModel,Feed.ViewHolder> {


    String Id;
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public Feed(@NonNull FirebaseRecyclerOptions<FeedModel> options,String Id) {
        super(options);
        Id = this.Id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_list,parent,false);
        return new ViewHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull FeedModel model) {
        Glide.with(holder.itemView.getContext()).load(model.getUrl()).into(holder.image);

    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }
    }
}
