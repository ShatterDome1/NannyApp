package com.example.nannyapp.main.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
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
    private static final String TAG = CardAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<CardModel> cardModelArrayList;

    private OnItemClickListener listener;

    public CardAdapter(Context context, ArrayList<CardModel> cardModelArrayList, OnItemClickListener listener) {
        this.context = context;
        this.cardModelArrayList = cardModelArrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new ViewHolder(view, listener);
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

    public CardModel getItemAtPosition(int position) {
        return cardModelArrayList.get(position);
    }

    // View holder class for initializing of
    // your views such as TextView and Imageview.
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView profilePicture;
        private TextView fullName;
        private TextView location;
        private TextView rating;
        private OnItemClickListener listener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.card_profile_picture);
            fullName = itemView.findViewById(R.id.card_full_name);
            location = itemView.findViewById(R.id.card_location);
            rating = itemView.findViewById(R.id.card_rating);
            this.listener = listener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            this.listener.onItemClick(getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
