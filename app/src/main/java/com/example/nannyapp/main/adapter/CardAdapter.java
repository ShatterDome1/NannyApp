package com.example.nannyapp.main.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nannyapp.R;

import java.util.ArrayList;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    private Context context;
    private ArrayList<CardModel> cardModelArrayList;

    public CardAdapter(Context context, ArrayList<CardModel> cardModelArrayList) {
        this.context = context;
        this.cardModelArrayList = cardModelArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // to set data to textview and imageview of each card layout
        CardModel model = cardModelArrayList.get(position);
        holder.fullName.setText(model.getFullName());
        holder.location.setText(model.getLocation());
        holder.rating.setText(model.getRating());
        holder.profilePicture.setImageBitmap(BitmapFactory.decodeByteArray(model.getProfilePicture(), 0, model.getProfilePicture().length));
    }

    @Override
    public int getItemCount() {
        return cardModelArrayList.size();
    }

    // View holder class for initializing of
    // your views such as TextView and Imageview.
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView profilePicture;
        private TextView fullName;
        private TextView location;
        private TextView rating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.card_profile_picture);
            fullName = itemView.findViewById(R.id.card_full_name);
            location = itemView.findViewById(R.id.card_location);
            rating = itemView.findViewById(R.id.card_rating);
        }
    }
}