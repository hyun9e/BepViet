package com.example.bepviet02;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.bepviet02.databinding.ActivityAddRecipeBinding;
import com.example.bepviet02.models.Category;
import com.example.bepviet02.models.Recipe;
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

public class AddRecipeActivity extends AppCompatActivity {
    private ActivityAddRecipeBinding binding;
    private Uri recipeImageUri;
    private String name, category, time, ingredients, description;
    //For Edit recipe
    boolean mIsEdit = false;
    boolean isImageNew = true;
    String mRecipeId;
    Recipe mRecipe;
    List<String> categoryList = new ArrayList<>();
    private PleaseWaitDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUi();
    }

    // GET WIDGET
    private void initUi() {
        resetInputFields();
        loadCategoryList();
        binding.btnGoBack.setOnClickListener(view -> finish());
        binding.ivAddRecipeFood.setOnClickListener(view -> openImagePicker());
        binding.btnAddRecipe.setOnClickListener(view -> addRecipe());
        isActivityEdit();
    }

    // LOAD CATEGORIES SIMULATING SPINNER
    private void loadCategoryList() {
        // Load categories from Firebase Realtime Database
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Categories");
        // Create an ArrayAdapter to populate the AutoCompleteTextView
        ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(AddRecipeActivity.this, android.R.layout.simple_list_item_1, categoryList);
        binding.etCategory.setAdapter(adapterCategory);
        // Add a listener to the database reference
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String categoryName = dataSnapshot.getValue(Category.class).getName();
                        categoryList.add(categoryName);
                    }
                    adapterCategory.notifyDataSetChanged();
                    // SET AUTO SELCT FIRST ITEM BY DEFAULT
                    if (!categoryList.isEmpty()) {
                        binding.etCategory.setText(categoryList.get(0), false); // false to not show the dropdown
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }


    // ADD NEW RECIPE
    private void addRecipe() {
        // Validate input fields
        name = Objects.requireNonNull(binding.etName.getText()).toString().trim();
        category = binding.etCategory.getText().toString();
        time = Objects.requireNonNull(binding.etTime.getText()).toString();
        ingredients = Objects.requireNonNull(binding.etIngredients.getText()).toString().trim();
        description = Objects.requireNonNull(binding.etDescription.getText()).toString().trim();
        String authorId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        // Check if input fields are valid
        if (isInputValidated()) {
            // Show progress dialog
            showProgressDialog();
            // Create a new Recipe object
            Recipe newRecipe = new Recipe(name, category, Integer.parseInt(time), ingredients, description, "", authorId);
            // Upload recipe image to Cloudinary
            uploadRecipeImage(recipeImageUri, newRecipe);
        }
    }

    // CHECK IF NEED TO TRANSFORM TO EDIT ACTIVITY
    private void isActivityEdit() {
        mIsEdit = getIntent().getBooleanExtra("isEdit", false);
        mRecipe = (Recipe) getIntent().getSerializableExtra("recipe");
        if (mRecipe != null) {
            mRecipeId = Objects.requireNonNull(mRecipe).getId();
            binding.tvAddRecipeTitle.setText("Sửa công thức");
            binding.btnAddRecipe.setText("Sửa");
            // Auto fill all fields
            mRecipeId = Objects.requireNonNull(mRecipe).getId();
            binding.etName.setText(mRecipe.getName());
            binding.etCategory.setText(mRecipe.getCategory());
            binding.etTime.setText(String.valueOf(mRecipe.getTime()));
            binding.etIngredients.setText(mRecipe.getIngredients());
            binding.etDescription.setText(mRecipe.getDescription());
            Glide.with(this).load(mRecipe.getImageUrl()).placeholder(R.drawable.add_recipe_placeholder).into(binding.ivAddRecipeFood);
        } else {
            Log.e("AddRecipeActivity", "isActivityEdit: Edit mode is true, but recipe is null!");
        }
    }

    // PICK IMAGE
    private void openImagePicker() {
        ImagePicker.with(AddRecipeActivity.this)
                .crop()                    // Crop the image (optional)
                .compress(1024)            // Compress the image to a maximum size in KB (optional)
                .maxResultSize(1080, 1080) // Set the maximum dimensions for the image (optional)
                .createIntent(intent -> {
                    startForAddReciperImageResult.launch(intent); // Start the activity result
                    return null;
                });
    }

    // ACTIVITY RESULT FOR PICK IMAGE
    private final ActivityResultLauncher<Intent> startForAddReciperImageResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                int resultCode = result.getResultCode();
                Intent data = result.getData();

                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Image Uri will not be null for RESULT_OK
                    Uri imgUri = data.getData();
                    recipeImageUri = imgUri;
                    binding.ivAddRecipeFood.setImageURI(imgUri);
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(AddRecipeActivity.this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddRecipeActivity.this, "Hủy chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            });

    // VALIDATE INPUT FIELDS
    private boolean isInputValidated() {
        if (name.isEmpty()) {
            binding.etName.setError("Tên món ăn không được để trống");
            return false;
        }

        if (category.isEmpty()) {
            binding.etCategory.setError("Danh mục không được để trống");
            return false;
        }

        if (time.isEmpty()) {
            binding.etTime.setError("Thời gian nấu không được để trống");
            return false;
        }

        try {
            int timeInt = Integer.parseInt(time);
            if (timeInt < 1) {
                binding.etTime.setError("Thời gian nấu phải lớn hơn 0");
                return false;
            }
        } catch (NumberFormatException e) {
            binding.etTime.setError("Thời gian nấu phải là số nguyên");
            return false;
        }

        if (ingredients.isEmpty()) {
            binding.etIngredients.setError("Nguyên liệu không được để trống");
            return false;
        }

        if (description.isEmpty()) {
            binding.etDescription.setError("Mô tả hướng dẫn không được để trống");
            return false;
        }

        if (recipeImageUri == null && !mIsEdit) {
            Toast.makeText(AddRecipeActivity.this, "Vui lòng chọn ảnh minh họa cho món ăn", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (recipeImageUri == null && mIsEdit) {
            isImageNew = false;
        }
        return true;
    }

    // SHOW PROGRESS DIALOG
    private void showProgressDialog() {
        progressDialog = new PleaseWaitDialog(this);
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

    // UPLOAD RECIPE IMAGE TO CLOUDINARY AND GENRATE LINK
    private void uploadRecipeImage(Uri imageUri, Recipe newRecipe) {
        if(!isImageNew && mRecipe != null) {
            newRecipe.setImageUrl(mRecipe.getImageUrl());
            uploadRecipeToDataBase(newRecipe);
            Log.e("AddRecipeActivity", "uploadRecipeImage, isImageNew: "+ isImageNew+" mRecipe: "+ mRecipe.toString() );
            return;
        }
        Toast.makeText(AddRecipeActivity.this, "Đang tải ảnh", Toast.LENGTH_SHORT).show();
        // Initialize Cloudinary MediaManager
        String requestId = MediaManager.get().upload(imageUri)
                .option("folder", "Android/Recipes")
                .option("resource_type", "image")
                .unsigned("bepviet_unsigned")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {

                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // Get the URL of the uploaded image
                        String imageUrl = Objects.requireNonNull(resultData.get("secure_url")).toString();
                        Log.d("imageUrl", imageUrl);
                        // Set the image URL for the newRecipe
                        newRecipe.setImageUrl(imageUrl);
                        // Upload recipe to Firebase Realtime Database
                        uploadRecipeToDataBase(newRecipe);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        dismissPleaseWaitDialog(progressDialog);
                        Toast.makeText(getBaseContext(), "Tải ảnh đại diện thất bại", Toast.LENGTH_SHORT).show();
                        Log.e("imageUrl", "onError: " + error.getDescription());
                        finish();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        dismissPleaseWaitDialog(progressDialog);
                        Log.e("imageUrl", "onReschedule: " + error.getDescription());
                        }
                }).dispatch();
    }

    private void uploadRecipeToDataBase(Recipe recipe) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Recipes");
        String recipeId = db.push().getKey();
        // ADD NEW RECIPE TO DATABASE
        if (!mIsEdit){
            recipe.setId(recipeId);
            db.child(recipeId).setValue(recipe).addOnCompleteListener(task -> {
                dismissPleaseWaitDialog(progressDialog);
                if (task.isSuccessful()) {
                    Toast.makeText(AddRecipeActivity.this, "Thêm món ăn thành công", Toast.LENGTH_SHORT).show();
                    resetInputFields();
                    finish();
                } else {
                    Toast.makeText(AddRecipeActivity.this, "Đã có lỗi xảy ra khi thêm món ăn", Toast.LENGTH_SHORT).show();
                    Log.e("Add Recipe", "onError: " + task.getException());
                }
            });
        }
        // EDIT RECIPE
        else {
            Recipe old_recipe = (Recipe) getIntent().getSerializableExtra("recipe");
            // Keep the same id and authorId
            recipe.setId(mRecipeId);
            recipe.setAuthorId(Objects.requireNonNull(old_recipe).getAuthorId());
            Log.e("AddRecipeActivity", "New Recipe "+ recipe.toString());
            db.child(mRecipeId).setValue(recipe).addOnCompleteListener(task -> {
                dismissPleaseWaitDialog(progressDialog);
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    Log.e("Edit Recipe", "New Recipe onSuccess: "+ recipe.toString());
                    navigateToNewRecipe(recipe);

                } else {
                    Toast.makeText(AddRecipeActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    Log.e("Edit Recipe", "onError: " + task.getException());
                }
            });
        }
    }

    private void navigateToNewRecipe(Recipe newRecipe) {
        Intent intent = new Intent(binding.getRoot().getContext(), RecipeDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("updated_recipe", newRecipe);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    // RESET INPUT FIELDS
    private void resetInputFields() {
        recipeImageUri = null;
        binding.etName.setText("");
        binding.etCategory.setText("");
        binding.etTime.setText("");
        binding.etIngredients.setText("");
        binding.etDescription.setText("");
        binding.ivAddRecipeFood.setImageResource(R.drawable.add_recipe_placeholder);
    }
}