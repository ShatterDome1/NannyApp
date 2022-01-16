package com.example.nannyapp.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nannyapp.R;
import com.example.nannyapp.entity.Review;
import com.example.nannyapp.entity.User;
import com.example.nannyapp.login.LoginActivity;
import com.example.nannyapp.entity.Role;
import com.example.nannyapp.main.ui.details.NannyDetailsFragment;
import com.example.nannyapp.main.ui.dialog.ReviewDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nannyapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity implements ReviewDialog.ReviewDialogListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    TextView nameText;
    TextView emailText;
    ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_nanny, R.id.nav_parent, R.id.nav_nanny_profile, R.id.nav_parent_profile)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        initDrawerHeader(drawer);
        initToolbarMenu();
        initFloatingActionButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void initDrawerHeader(DrawerLayout drawer) {
        View header = binding.navView.getHeaderView(0);
        nameText = header.findViewById(R.id.nav_header_name);
        emailText = header.findViewById(R.id.nav_header_email);
        profileImageView = header.findViewById(R.id.nav_header_image);

        initProfilePicturePicker(profileImageView);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        emailText.setText(currentUser.getEmail());

        firebaseFirestore.collection("Users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null) {
                        User currentUser = task.getResult().toObject(User.class);
                        nameText.setText(currentUser.getFirstName() + " " + currentUser.getLastName());

                        initDrawerOptionsBasedOnRole(currentUser);
                    }
                } else {
                    Log.d(TAG, "onComplete: Failed to get user", task.getException());
                }
            }
        });

        StorageReference profileImageStorageReference = storageReference.child("images/" + currentUser.getUid());
        final long ONE_MB = 1024 * 1024;
        profileImageStorageReference
                .getBytes(ONE_MB)
                .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        if (task.isSuccessful()) {
                            byte[] documentSnapshot = task.getResult();
                            Bitmap profileImage = BitmapFactory.decodeByteArray(documentSnapshot, 0, documentSnapshot.length);
                            profileImageView.setImageBitmap(profileImage);
                        } else {
                            Log.d(TAG, "onComplete: Failed to get profile image", task.getException());
                            profileImageView.setImageResource(R.mipmap.ic_launcher_round);
                        }
                    }
                });
    }

    private void initDrawerOptionsBasedOnRole(User currentUser) {
        NavigationView navigationView = binding.navView;
        Menu menu = navigationView.getMenu();
        if (currentUser.getRole().equals(Role.PARENT)) {
            menu.findItem(R.id.nav_nanny_profile).setVisible(false);
            menu.findItem(R.id.nav_nanny).setVisible(false);
        } else {
            menu.findItem(R.id.nav_parent_profile).setVisible(false);
            menu.findItem(R.id.nav_parent).setVisible(false);
        }
    }

    private void initProfilePicturePicker(ImageView profileImageView) {
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startImageCropActivity();
            }
        });
    }

    private void startImageCropActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1, 1)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                StorageReference uploadImageRef = storageReference.child("images/" + firebaseAuth.getCurrentUser().getUid());
                UploadTask uploadTask = uploadImageRef.putFile(resultUri);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Image upload failed. Please try again", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), "Image upload successful", Toast.LENGTH_SHORT).show();
                        profileImageView.setImageURI(resultUri);
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, "onActivityResult: failed", error);
            }
        }
    }

    private void initFloatingActionButton() {
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void initToolbarMenu() {
        binding.appBarMain.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_logout) {
                    firebaseAuth.signOut();
                    navigateToLogin();
                    return true;
                }
                return false;
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onSubmitReviewClick(String userId, String reviewerId, String comment, Float rating) {
        Log.d(TAG, "onSubmitReviewClick: Submit rating: " + userId + " " + reviewerId + " " + comment + " " + rating);
        Review review = new Review();
        review.setComment(comment);
        review.setRating(rating);
        firebaseFirestore.collection("Reviews")
                .document(userId + " " + reviewerId)
                .set(review)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Review posted", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "onComplete: failed to push review", task.getException());
                        }
                    }
                });
    }
}