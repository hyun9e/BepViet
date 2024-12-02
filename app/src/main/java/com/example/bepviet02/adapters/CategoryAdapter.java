package com.example.bepviet02.adapters;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bepviet02.AllRecipesActivity;
import com.example.bepviet02.R;
import com.example.bepviet02.RecipeDetailsActivity;
import com.example.bepviet02.databinding.ItemCategoryBinding;
import com.example.bepviet02.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    // Create a list of categories
    List<Category> categoryList = new ArrayList<>();

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryViewHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        if (category == null) return;
        holder.onBind(category);
    }

    // Return the total number of items in category list
    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        // Initialize views in the item layout
        ItemCategoryBinding binding;
        public CategoryViewHolder(@NonNull ItemCategoryBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        // Bind data to the views in the item layout
        public void onBind(Category category) {
            // Set the category name and image for RecyclerView
            binding.categoryName.setText(category.getName());
            Glide
                    .with(binding.getRoot())
                    .load(category.getImage())
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher)
                    .into(binding.categoryImg);

            // Set click listener for the item view
            binding.getRoot().setOnClickListener(view -> {
                Intent intent = new Intent(binding.getRoot().getContext(), AllRecipesActivity.class);
                intent.putExtra("type", "category");
                intent.putExtra("category_name", category.getName());
                binding.getRoot().getContext().startActivity(intent);
            });

            // Set long click listener for the item view
            binding.getRoot().setOnLongClickListener(view -> {
                Toast.makeText(binding.getRoot().getContext(), category.getName(), Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }
}
