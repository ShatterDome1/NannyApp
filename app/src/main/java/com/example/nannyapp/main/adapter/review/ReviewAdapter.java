package com.example.nannyapp.main.adapter.review;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nannyapp.R;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private static final String TAG = ReviewAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<ReviewModel> reviewModelArrayList;

    public ReviewAdapter(Context context, ArrayList<ReviewModel> reviewModelArrayList) {
        this.context = context;
        this.reviewModelArrayList = reviewModelArrayList;
    }

    @NonNull
    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_card, parent, false);
        return new ReviewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ViewHolder holder, int position) {
        // to set data to textview and imageview of each card layout
        ReviewModel model = reviewModelArrayList.get(position);
        holder.profileInitials.setText(new StringBuilder().append(model.getFirstName().charAt(0)).append(model.getLastName().charAt(0)).toString());
        holder.fullName.setText(model.getFirstName() + " " + model.getLastName());
        holder.rating.setText(model.getRating().toString());
        holder.comment.setText(model.getComment());
    }

    @Override
    public int getItemCount() {
        return reviewModelArrayList.size();
    }

    // View holder class for initializing of
    // your views such as TextView and Imageview.
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView profileInitials;
        private TextView fullName;
        private TextView rating;
        private TextView comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileInitials = itemView.findViewById(R.id.review_card_initials);
            fullName = itemView.findViewById(R.id.review_card_full_name);
            rating = itemView.findViewById(R.id.review_card_rating);
            comment = itemView.findViewById(R.id.review_card_comment);
        }
    }
}
