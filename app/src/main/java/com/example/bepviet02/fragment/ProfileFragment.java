package com.example.bepviet02.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.bepviet02.LoginActivity;
import com.example.bepviet02.R;
import com.example.bepviet02.SettingsActivity;
import com.example.bepviet02.adapters.RecipeAdapter;
import com.example.bepviet02.databinding.FragmentProfileBinding;
import com.example.bepviet02.models.Recipe;
import com.example.bepviet02.models.User;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tashila.pleasewait.PleaseWaitDialog;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private User user;
    private String userID;
    PleaseWaitDialog progressDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            checkLogin();
        } else {
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            initUi();
            loadProfile();
        }
    }

    private void checkLogin() {
        new AlertDialog.Builder(getContext())
                .setTitle("Yêu cầu đăng nhập")
                .setNegativeButton("Đăng nhập", (dialog, which) -> {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish(); // Tùy chọn để kết thúc activity hiện tại
                })
                .setPositiveButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void initUi() {
        // Change profile image
        binding.btnEdit.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Chọn ảnh đại diện mới cho tài khoản", Toast.LENGTH_SHORT).show();
            ImagePicker.with(this)
                    .cropSquare() // Cho phép cắt ảnh
                    .compress(1024) // Nén ảnh
                    .maxResultSize(1024, 1024) // Kích thước tối đa
                    .createIntent(intent -> {
                        startForChangeProfileImageResult.launch(intent); // Start the activity result
                        return null;
                    });
        });

        binding.btnEditCover.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Chọn ảnh bìa mới cho tài khoản", Toast.LENGTH_SHORT).show();
            ImagePicker.with(this)
                    .crop(16f, 9f) // Cho phép cắt ảnh
                    .compress(1024) // Nén ảnh
                    .maxResultSize(1280, 720) // Kích thước tối đa
                    .createIntent(intent -> {
                        startForChangeProfileCoverResult.launch(intent); // Start the activity result
                        return null;
                    });
        });
        binding.btnSettings.setOnClickListener(view -> {
            startActivity(new Intent(requireContext(), SettingsActivity.class));
        });
    }

    // ACTIVITY RESULT FOR PICKING AVATAR IMAGE
    private final ActivityResultLauncher<Intent> startForChangeProfileImageResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> handleImageResult(result, "avatar"));
    // ACTIVITY RESULT FOR PICKING COVER IMAGE
    private final ActivityResultLauncher<Intent> startForChangeProfileCoverResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> handleImageResult(result, "cover"));

    // LOAD USER PROFILE
    private void loadProfile() {
        if(!TextUtils.isEmpty(userID)) {
            String User_Id = userID;
            // Get user data from Firebase Realtime Database
            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
            // Get the current user's ID
            // Add a listener to the user data
            db.child("Users").child(User_Id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Get the user data from the snapshot
                    user = snapshot.getValue(User.class);
                    if (user != null && isAdded() && binding != null) {
                        binding.tvUserName.setText(user.getName());
                        binding.tvEmail.setText(user.getEmail());
                        Glide
                                .with(requireContext())
                                .load(user.getAvatar())
                                .centerCrop()
                                .placeholder(R.drawable.ic_user_placeholder)
                                .into(binding.ivAvatar);
                        Glide
                                .with(requireContext())
                                .load(user.getCover())
                                .centerCrop()
                                .placeholder(R.drawable.bg_default_recipe)
                                .into(binding.ivCover);
                    } else {
                        Log.e("ProfileFragment", "onDataChange: User data is null");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ProfileFragment", "onCancelled: " + error.getMessage());
                }
            });
        loadRecipes();
        }
    }

    // LOAD USER RECIPES
    private void loadRecipes() {
        // Create a list to store the recipes
        String User_Id = userID;
        List<Recipe> MyRecipes = new ArrayList<>();
        // Get recipes from Database
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Recipes");
        db.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    MyRecipes.clear();
                    // Add recipes to MyRecipes
                    for (DataSnapshot recipeData : snapshot.getChildren()) {
                        // Get authorId from the recipe data
                        String authorId = recipeData.child("authorId").getValue(String.class);
                        // Check if the authorId matches the userID
                        if (authorId != null && authorId.equals(User_Id)) {
                            MyRecipes.add(recipeData.getValue(Recipe.class));
                        }
                    }
                    // Set up RecyclerView adapter for MyRecipes
                    RecipeAdapter recipeAdapter = (RecipeAdapter) binding.rvProfile.getAdapter();
                    if (recipeAdapter == null) {
                        recipeAdapter = new RecipeAdapter();
                        binding.rvProfile.setAdapter(recipeAdapter);
                    }
                    recipeAdapter.setRecipeList(MyRecipes);
                    // adapter.notifyDataSetChanged();
                    // Set the RecyclerView layout manager with X column(s)/row (only once)
                    binding.rvProfile.setLayoutManager(new GridLayoutManager(getContext(), 3));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Log.e("ProfileFragment", "onCancelled: " + error.getMessage());
            }
        });
    }



    // Handle image result
    private void handleImageResult(ActivityResult result, String avatarOrCover) {
        int resultCode = result.getResultCode();
        Intent data = result.getData();
        if (data != null && resultCode == Activity.RESULT_OK) {
            // Image Uri will not be null for RESULT_OK
            Uri imageUri = data.getData();
            uploadImageToCloudinary(imageUri, avatarOrCover);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(getContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Hủy chọn ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    // UPLOAD PROFILE IMAGE TO CLOUDINARY BASE ON PATH ("avatar"/"cover")
    private void uploadImageToCloudinary(Uri imageUri, String avatarOrCover) {
        showProgressDialog();
        String cloudinary_image_path = (avatarOrCover.equals("avatar")) ? "Avatar" : "Cover";
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String requestId = MediaManager.get().upload(imageUri)
                .option("folder", "Android/Users/" + userID + "/Profile_" + cloudinary_image_path)
                .option("resource_type", "image")
                .unsigned("bepviet_unsigned")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(getContext(), "Đang tải ảnh", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {

                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // Get the URL of the uploaded image
                        String imageUrl = Objects.requireNonNull(resultData.get("secure_url")).toString();
                        // Return the URL to the calling activity
                        Log.d("imageUrl", "onSuccess: " + imageUrl);
                        uploadProfileImageToDatabase(userID, imageUrl, avatarOrCover);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        dismissPleaseWaitDialog(progressDialog);
                        Toast.makeText(getContext(), "Tải ảnh đại diện thất bại", Toast.LENGTH_SHORT).show();
                        Log.e("imageUrl", "onError: " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        dismissPleaseWaitDialog(progressDialog);
                        Log.e("imageUrl", "onReschedule: " + error.getDescription());
                    }
                }).dispatch();
    }

    private void uploadProfileImageToDatabase(String userID, String imageUrl, String path) {
        String avatarOrCover = (path.equals("avatar")) ? "avatar" : "cover";
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("Users").child(userID).child(avatarOrCover).setValue(imageUrl).addOnCompleteListener(task -> {
            dismissPleaseWaitDialog(progressDialog);
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Tải ảnh thành công", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("imageUrl", "onError: " + task.getException());
                Toast.makeText(getContext(), "Tải ảnh thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // SHOW PROGRESS DIALOG
    private void showProgressDialog() {
        progressDialog = new PleaseWaitDialog(requireContext());
        progressDialog.setMessage("Đang tải công thức...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    // DISMISS PROGRESS DIALOG
    private void dismissPleaseWaitDialog(PleaseWaitDialog progressDialog) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadProfile();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
