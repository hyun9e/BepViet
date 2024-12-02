package com.example.bepviet02.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bepviet02.AllRecipesActivity;
import com.example.bepviet02.SettingsActivity;
import com.example.bepviet02.adapters.RecipeAdapter;
import com.example.bepviet02.databinding.FragmentHomeBinding;
import com.example.bepviet02.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment's layout and return the root view
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUi();

    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecipes();
    }



    private void initUi() {
        // Settings
        binding.btnHomeSettings.setOnClickListener(view -> startActivity(new Intent(binding.getRoot().getContext(), SettingsActivity.class)));
        // Search bar
        binding.etSearch.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                sendSearchQuery();
                return true;
            }
            return false;
        });
        binding.rvPopulars.setAdapter(new RecipeAdapter());
        binding.rvFavs.setAdapter(new RecipeAdapter());

        binding.tvHomeSeeAllPopulars.setOnClickListener(view -> {
            Toast.makeText(binding.getRoot().getContext(), "See all popular recipes", Toast.LENGTH_SHORT).show();
        });
    }

    private void sendSearchQuery() {
        String searchQuery = binding.etSearch.getText().toString();
        Intent intent = new Intent(binding.getRoot().getContext(), AllRecipesActivity.class);
        intent.putExtra("type", "search");
        intent.putExtra("search_query", searchQuery);
        binding.getRoot().getContext().startActivity(intent);
    }



    private void loadRecipes() {
        DatabaseReference dbRefFavorites = FirebaseDatabase.getInstance().getReference("Favorites");
        DatabaseReference dbRefRecipes = FirebaseDatabase.getInstance().getReference("Recipes");
        String userId = FirebaseAuth.getInstance().getCurrentUser() == null ? null : FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRefFavorites.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadPopularRecipes(dbRefFavorites, dbRefRecipes);
                if (userId != null) {
                    binding.tvHomeSeeAllFavs.setOnClickListener(view -> {
                        seeAllFavoriteRecipes(userId);
                    });
                    loadFavRecipes(dbRefFavorites, dbRefRecipes, userId);
                }
                else {
                    binding.tvHomeSeeAllFavs.setOnClickListener(view -> {
                        Toast.makeText(binding.getRoot().getContext(), "Cần đăng nhập để xem món ăn yêu thích", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFavRecipes(DatabaseReference dbRefFavorites, DatabaseReference dbRefRecipes, String userId) {
        List<Recipe> favRecipes = new ArrayList<>();
        dbRefFavorites.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favRecipes.clear();

                // Duyệt qua tất cả món ăn trong Favorites
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    String recipeId = recipeSnapshot.getKey();
                    if (recipeSnapshot.child("userLiked").hasChild(userId)) {
                        // Lấy chi tiết món ăn từ Recipes node
                        dbRefRecipes.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot recipeDetailsSnapshot) {
                                Recipe recipe = recipeDetailsSnapshot.getValue(Recipe.class);
                                if (recipe != null) {
                                    favRecipes.add(recipe);
                                }
                                // Cập nhật RecyclerView khi đã tải đủ dữ liệu
                                RecipeAdapter adapter = (RecipeAdapter) binding.rvFavs.getAdapter();
                                if (adapter != null) {
                                    adapter.setRecipeList(favRecipes);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("HomeFragment", "loadFavRecipes - Fetch Recipe Details: " + error.getMessage());
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(binding.getRoot().getContext(), "Không thể tải món ăn yêu thích", Toast.LENGTH_SHORT).show();
                Log.d("HomeFragment", "loadFavRecipes - onCancelled: " + error.getMessage());
            }
        });
    }

    // Load all favorite recipes
    private void seeAllFavoriteRecipes(String userId) {
        Intent intent = new Intent(binding.getRoot().getContext(), AllRecipesActivity.class);
        intent.putExtra("type", "favorite");
        intent.putExtra("user_id", userId);
        binding.getRoot().getContext().startActivity(intent);
    }


    // Load and display popular recipes
    private void loadPopularRecipes(DatabaseReference dbRefFavorites, DatabaseReference dbRefRecipes) {
        List<Recipe> popularRecipes = new ArrayList<>();
        // Fetch popular recipes from Favorites
        dbRefFavorites.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                popularRecipes.clear();
                Map<String, Integer> recipeLikes = new HashMap<>();
                // Traverse through Favorites to count likes
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    String recipeId = recipeSnapshot.getKey();
                    Integer likes = recipeSnapshot.child("likes").getValue(Integer.class);
                    if (recipeSnapshot.child("likes").getValue(Integer.class) == null) {
                        likes = 0;
                    }
                    recipeLikes.put(recipeId, likes);
                }
                // Sort recipes by likes (descending) and get top 10
                List<String> topRecipeIds = recipeLikes.entrySet().stream()
                        .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Sort by likes descending
                        .limit(10) // Take top 10
                        .map(Map.Entry::getKey) // Extract recipe IDs
                        .collect(Collectors.toList());

                // Fetch recipe details from Recipes node
                for (String recipeId : topRecipeIds) {
                    dbRefRecipes.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot recipeSnapshot) {
                            Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                            if (recipe != null) {
                                popularRecipes.add(recipe);
                            }

                            RecipeAdapter adapter = (RecipeAdapter) binding.rvPopulars.getAdapter();
                            if (adapter != null) {
                                adapter.setRecipeList(popularRecipes);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d("HomeFragment", "loadPopularRecipes - onCancelled: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(binding.getRoot().getContext(), "Không thể tải món ăn phổ biến", Toast.LENGTH_SHORT).show();
                Log.d("HomeFragment", "loadPopularRecipes - onCancelled: " + error.getMessage());
            }
        });
    }

//private void loadPopularRecipes(DatabaseReference dbRefFavorites, DatabaseReference dbRefRecipes) {
//        List<Recipe> popularRecipes = new ArrayList<>();
//        List<Recipe> allRecipes = new ArrayList<>();
//        dbRefRecipes.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                allRecipes.clear();
//                snapshot.getChildren().forEach(recipeSnapshot -> {
//                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
//                    if (recipe != null) {
//                        allRecipes.add(recipe);
//                    }
//                });
//                for (int i = 0; i < 5; i++) {
//                    int random = (int) (Math.random() * allRecipes.size());
//                    popularRecipes.add(allRecipes.get(random));
//                }
//                RecipeAdapter adapter = (RecipeAdapter) binding.rvPopulars.getAdapter();
//                if (adapter != null) {
//                    adapter.setRecipeList(popularRecipes);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(binding.getRoot().getContext(), "Không thể tải món ăn phổ biến", Toast.LENGTH_SHORT).show();
//                Log.d("HomeFragment", "loadPopularRecipes - onCancelled: " + error.getMessage());
//            }
//        });
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}