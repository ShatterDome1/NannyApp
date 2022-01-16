package com.example.nannyapp.main.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.nannyapp.R;

public class ReviewDialog extends DialogFragment {
    private static final String TAG = ReviewDialog.class.getSimpleName();

    private TextView userNameView;
    private TextView reviewerNameView;
    private RatingBar ratingBarView;
    private TextView reviewerInitialView;
    private TextView commentView;

    private String userName;
    private String reviewerFirstName;
    private String reviewerLastName;
    private String reviewerId;
    private String userId;

    public interface ReviewDialogListener {
        void onSubmitReviewClick(String userId, String reviewerId, String comment, Float rating);
    }

    ReviewDialogListener listener;

    public ReviewDialog(String userName,
                        String reviewerFirstName,
                        String reviewerLastName,
                        String reviewerId,
                        String userId) {
        this.userName = userName;
        this.reviewerFirstName = reviewerFirstName;
        this.reviewerLastName = reviewerLastName;
        this.reviewerId = reviewerId;
        this.userId = userId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_review, null);

        userNameView = view.findViewById(R.id.review_user_name);
        reviewerNameView = view.findViewById(R.id.review_reviewer_name);
        ratingBarView = view.findViewById(R.id.review_rating_bar);
        reviewerInitialView = view.findViewById(R.id.review_reviewer_initial);
        commentView = view.findViewById(R.id.review_reviewer_comment);

        userNameView.setText(this.userName);
        reviewerNameView.setText(this.reviewerFirstName + " " + reviewerLastName);
        reviewerInitialView.setText(this.reviewerFirstName.charAt(0) + String.valueOf(this.reviewerLastName.charAt(0)));

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Post", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onSubmitReviewClick(userId,
                                reviewerId,
                                commentView.getText().toString(),
                                ratingBarView.getRating());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ReviewDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (ReviewDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity()
                    + " must implement ReviewDialogListener");
        }
    }
}
