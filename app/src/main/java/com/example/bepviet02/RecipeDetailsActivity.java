package com.example.bepviet02;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.example.bepviet02.databinding.ActivityRecipeDetailsBinding;
import com.example.bepviet02.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class RecipeDetailsActivity extends AppCompatActivity {
    private ActivityRecipeDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUi();
    }

    private void initUi() {
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        if (recipe == null && recipe.getId() == null) {
            finish();
            return;
        }
        binding.btnGoBack.setOnClickListener(view -> finish());
        Glide.with(this).load(recipe.getImageUrl()).into(binding.ivRecipeDetails);
        binding.tvRecipeDetailsName.setText(recipe.getName());
        binding.tvRecipeDetailsCategory.setText(recipe.getCategory());
        binding.tvRecipeDetailsTime.setText(recipe.getTime() + " phút");
        binding.tvRecipeDetailsIngredients.setText(recipe.getIngredients());
        binding.tvRecipeDetailsDescription.setText(recipe.getDescription());
        fetchAuthorName(recipe.getAuthorId());
        // Make Edit Button visible
        getLikeButtonStatus(recipe);
        getLikesCount(recipe);
        if (!isCurrentUserAuthor(recipe)) {
            binding.btnRecipeDetailAction.setVisibility(View.GONE);
        } else {
            binding.btnRecipeDetailAction.setVisibility(View.VISIBLE);
            binding.btnRecipeDetailAction.setOnClickListener(view -> chooseActionDialog(recipe));
        }
        binding.btnDetailsShareRecipe.setOnClickListener(view -> copyRecipeToClipboard(recipe));
    }



    // Show dialog to choose action
    private void chooseActionDialog(Recipe recipe) {
        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa món ăn")
                .setMessage("Bạn muốn cập nhật hay xóa món ăn này?")
                .setNegativeButton("Cập nhật", (updateDialog, i) -> confirmEditDialog(recipe))
                .setPositiveButton("Xóa", (actionDialog, i) -> confirmDeleteDialog(recipe))
                .setNeutralButton("Hủy", (actionDialog, i) -> actionDialog.dismiss())
                .show();
    }

    // Show dialog to confirm edit
    private void confirmEditDialog(Recipe recipe) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận cập nhật")
                .setMessage("Bạn có chắc chắn muốn cập nhật món ăn này?")
                .setNegativeButton("Cập nhật", (editDialog, i) -> upDateRecipe(recipe))
                .setPositiveButton("Hủy", (editDialog, i) -> editDialog.dismiss())
                .show();
    }

    // Show dialog to confirm delete
    private void confirmDeleteDialog(Recipe recipe) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa món ăn này?")
                .setNegativeButton("Xóa", (deleteDialog, i) -> deleteRecipe(recipe))
                .setPositiveButton("Hủy", (deleteDialog, i) -> deleteDialog.dismiss())
                .show();
    }


    // Check if current user is the author of the recipe
    private boolean isCurrentUserAuthor(Recipe recipe) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return false;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return currentUserId.equals(recipe.getAuthorId());
    }

    // Fetch author name from database
    private void fetchAuthorName(String recipeAuthorId) {
        String unknown = "Không rõ";
        binding.tvRecipeDetailsAuthor.setText(unknown);
        // Check if `authorId` is not empty
        if (!TextUtils.isEmpty(recipeAuthorId)) {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users/" + recipeAuthorId);
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String authorName = snapshot.child("name").getValue(String.class);
                    binding.tvRecipeDetailsAuthor.setText(TextUtils.isEmpty(authorName) ? unknown : authorName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(RecipeDetailsActivity.this,
                            "Không lấy được thông tin người sở hữu", Toast.LENGTH_SHORT).show();
                    Log.d("RecipeDetailsActivity", "fetchAuthorName - onCancelled: " + error.getMessage());
                }
            });
        }
    }

    // Launcher AddRecipeActivity for editing
    private final ActivityResultLauncher<Intent> startForEditRecipeResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Recipe updatedRecipe = (Recipe) result.getData().getSerializableExtra("updated_recipe");
                    if (updatedRecipe != null) {
                        updateUI(updatedRecipe);
                    }
                }
            });

    // Launch AddRecipeActivity for editing
    private void upDateRecipe(Recipe recipe) {
        Intent intent = new Intent(this, AddRecipeActivity.class);
        intent.putExtra("recipe", recipe);
        intent.putExtra("isEdit", true);
        startForEditRecipeResult.launch(intent);
    }

    // Update UI with new recipe
    private void updateUI(Recipe updatedRecipe) {
        Glide.with(this).load(updatedRecipe.getImageUrl()).into(binding.ivRecipeDetails);
        binding.tvRecipeDetailsName.setText(updatedRecipe.getName());
        binding.tvRecipeDetailsCategory.setText(updatedRecipe.getCategory());
        binding.tvRecipeDetailsTime.setText(updatedRecipe.getTime() + " phút");
        binding.tvRecipeDetailsIngredients.setText(updatedRecipe.getIngredients());
        binding.tvRecipeDetailsDescription.setText(updatedRecipe.getDescription());
    }

    // Delete recipe from database
    private void deleteRecipe(Recipe recipe) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Recipes/" + recipe.getId());
        db.removeValue().addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Xóa món ăn thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Xóa món ăn thất bại", Toast.LENGTH_SHORT).show();
                });
        finish();
    }

    //
    private void getLikeButtonStatus(Recipe recipe) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            binding.btnLike.setOnClickListener(view -> Toast.makeText(this, "Cần đăng nhập để thêm vào yêu thích", Toast.LENGTH_SHORT).show());
        } else {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            isCurrentlyLiked(currentUserId, recipe);
            binding.btnLike.setOnClickListener(view -> likeRecipe(currentUserId, recipe));
        }
    }

    // Fetch likes count
    private void getLikesCount(Recipe recipe) {
        DatabaseReference dbLikes = FirebaseDatabase.getInstance().getReference("Favorites").child(recipe.getId());
        // get likes
        dbLikes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer likes = snapshot.child("likes").getValue(Integer.class);
                // 0 if likes is null
                likes = (likes != null) ? likes : 0;
                // Set likes count
                binding.tvLikesCount.setText(String.valueOf(likes));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailsActivity.this, "Lỗi khi lấy số lượng yêu thích", Toast.LENGTH_SHORT).show();
                Log.d("RecipeDetailsActivity", "getLikesCount - onCancelled: " + error.getMessage());
            }
        });
    }

    private void isCurrentlyLiked(String currentUserId, Recipe recipe) {
        DatabaseReference dbUserLiked = FirebaseDatabase.getInstance().getReference("Favorites").child(recipe.getId()).child("userLiked");
        dbUserLiked.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // if liked, set like button to liked and vice versa
                if (snapshot.exists()) {
                    binding.btnLike.setImageResource(R.drawable.ic_like_liked);
                } else binding.btnLike.setImageResource(R.drawable.ic_like);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailsActivity.this, "Lỗi khi kiểm tra danh sách yêu thích", Toast.LENGTH_SHORT).show();
                Log.d("RecipeDetailsActivity", "isCurrentlyLiked - onCancelled: " + error.getMessage());
            }
        });
    }

    private void likeRecipe(String currentUserId, Recipe recipe) {
        DatabaseReference dbFavoriteCurrentRecipe = FirebaseDatabase.getInstance().getReference("Favorites").child(recipe.getId());
        dbFavoriteCurrentRecipe.child("userLiked").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // if not liked, add to favorites, else remove
                if (!snapshot.exists()) {
                    addRecipeToFavorites(dbFavoriteCurrentRecipe, currentUserId);
                } else {
                    removeRecipeFromFavorites(dbFavoriteCurrentRecipe, currentUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailsActivity.this, "Lỗi khi kiểm tra danh sách yêu thích", Toast.LENGTH_SHORT).show();
                Log.e("RecipeDetailsActivity", "likeRecipe - onCancelled: " + error.getMessage());
            }
        });
    }

    private void addRecipeToFavorites(DatabaseReference dbFavoriteCurrentRecipe, String currentUserId) {
        // Update UI immediately
        setLikeButtonStatus(true);
        dbFavoriteCurrentRecipe.child("userLiked").child(currentUserId).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RecipeDetailsActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    updateLikesCount(dbFavoriteCurrentRecipe, 1); // Increase likes count
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RecipeDetailsActivity.this, "Thêm vào yêu thích thất bại", Toast.LENGTH_SHORT).show();
                    Log.d("RecipeDetailsActivity", "addRecipeToFavorites (userLiked) - onFailureListener: " + e.getMessage());
                    // If error, reset like button
                    setLikeButtonStatus(false);
                });
    }

    private void removeRecipeFromFavorites(DatabaseReference dbFavoriteCurrentRecipe, String currentUserId) {
        // Update UI immediately
        setLikeButtonStatus(false);
        dbFavoriteCurrentRecipe.child("userLiked").child(currentUserId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RecipeDetailsActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    updateLikesCount(dbFavoriteCurrentRecipe, -1); // Decrease likes count
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RecipeDetailsActivity.this, "Xóa khỏi yêu thích thất bại", Toast.LENGTH_SHORT).show();
                    Log.d("RecipeDetailsActivity", "removeRecipeFromFavorites (userLiked) - onFailureListener: " + e.getMessage());
                    // If error, reset like button
                    setLikeButtonStatus(true);
                });
    }

    private void updateLikesCount(DatabaseReference dbFavoriteCurrentRecipe, int change) {
        dbFavoriteCurrentRecipe.child("likes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer likes = snapshot.getValue(Integer.class);
                likes = (likes != null) ? likes : 0;
                int finalLike  = likes + change;
                if (finalLike < 0) {
                    dbFavoriteCurrentRecipe.child("likes").setValue(0).addOnCompleteListener(runnable -> {
                        if (runnable.isSuccessful()) {
                            binding.tvLikesCount.setText("0");
                        }
                    });
                    Toast.makeText(RecipeDetailsActivity.this, "Đã lỗi xảy ra", Toast.LENGTH_SHORT).show();
                    Log.d("RecipeDetailsActivity", "updateLikesCount - onDataChange: " + (finalLike));

                }
                else dbFavoriteCurrentRecipe.child("likes").setValue(finalLike).addOnCompleteListener(runnable -> {
                    if (runnable.isSuccessful()) {
                        binding.tvLikesCount.setText(String.valueOf(finalLike));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailsActivity.this, "Đã có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                Log.d("RecipeDetailsActivity", "updateLikesCount, (int) change = " + change + " - onCancelled: " + error.getMessage());
            }
        });
    }

    // Set like button status
    private void setLikeButtonStatus(Boolean isLiked) {
        // Tạo hiệu ứng phóng to và thu nhỏ
        float scale = isLiked ? 1.2f : 1.0f;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.btnLike, "scaleX", scale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.btnLike, "scaleY", scale);

        scaleX.setDuration(300);
        scaleY.setDuration(300);

        scaleX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Thay đổi icon sau khi animation kết thúc
                binding.btnLike.setImageResource(isLiked ? R.drawable.ic_like_liked : R.drawable.ic_like);
                // Phóng to lại về kích thước ban đầu
                ObjectAnimator.ofFloat(binding.btnLike, "scaleX", 1.0f).setDuration(300).start();
                ObjectAnimator.ofFloat(binding.btnLike, "scaleY", 1.0f).setDuration(300).start();
            }
        });

        scaleX.start();
        scaleY.start();
    }

    // Copy recipe to clipboard
    private void copyRecipeToClipboard(Recipe recipe) {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        String sharedMessage = "Xin chào, tôi muốn chia sẻ công thức này với bạn!\n"+
                "Tên công thức: " + recipe.getName() + "\n"+
                "Loại: " + recipe.getCategory() + "\n"+
                "Thời gian: " + recipe.getTime() + " phút\n"+
                "Nguyên liệu chuẩn bị:\n " + recipe.getIngredients() + "\n"+
                "Mô tả:\n" + recipe.getDescription();
        cm.setPrimaryClip(ClipData.newPlainText("Recipe", sharedMessage));
        Toast.makeText(this, "Đã sao chép món ăn vào bộ nhớ tạm", Toast.LENGTH_SHORT).show();
    }


}
