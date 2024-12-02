package com.example.bepviet02.adapters;
import static com.example.bepviet02.databinding.ItemRecipeBinding.inflate;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bepviet02.R;
import com.example.bepviet02.RecipeDetailsActivity;
import com.example.bepviet02.databinding.ItemRecipeBinding;
import com.example.bepviet02.models.Recipe;

import java.util.ArrayList;
import java.util.List;

// Adapter for displaying recipes in a RecyclerView
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeHolder> {
    List<Recipe> recipeList = new ArrayList<>();

    // Set the list of recipes to display
    public void setRecipeList(List<Recipe> recipeList){
        this.recipeList = recipeList;
        notifyDataSetChanged();
    }

    // Create and return a new ViewHolder (RecipeHolder)
    @NonNull
    @Override
    public RecipeAdapter.RecipeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecipeHolder(inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    // Bind data to the ViewHolder at the specified position
    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.RecipeHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.onBind(recipe);

    }

    // Return the total number of items in the data set
    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // ViewHolder class for displaying a single recipe
    public static class RecipeHolder extends RecyclerView.ViewHolder {
        ItemRecipeBinding binding;

        // Constructor to initialize the ViewHolder
        public RecipeHolder(@NonNull ItemRecipeBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        // Bind recipe data to the views in the item layout
        public void onBind(Recipe recipe){
            // Set the recipe name and image for RecyclerView
            binding.tvRecipeName.setText(recipe.getName());
            Glide.with(binding.getRoot().getContext())
                    .load(recipe.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.bg_default_recipe)
                    .into(binding.bgImgRecipe);

            // Set click listener for the item view
            binding.getRoot().setOnClickListener(view -> {
                Intent intent = new Intent(binding.getRoot().getContext(), RecipeDetailsActivity.class);
                intent.putExtra("recipe", recipe);
                binding.getRoot().getContext().startActivity(intent);
            });
            // Set long click listener for the item view
            binding.getRoot().setOnLongClickListener(view -> {
                Toast.makeText(binding.getRoot().getContext(), recipe.getName(), Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }
}
