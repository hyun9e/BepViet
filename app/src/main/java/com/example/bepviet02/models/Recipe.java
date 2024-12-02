package com.example.bepviet02.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Recipe implements Serializable {
    private String id, name, category, ingredients,  description, imageUrl, authorId;
    private int time;

    public Recipe() {
    }

    public Recipe(String name, String category, int time, String ingredients, String description, String imageUrl, String authorId) {
        this.name = name;
        this.category = category;
        this.time = time;
        this.ingredients = ingredients;
        this.description = description;
        this.imageUrl = imageUrl;
        this.authorId = authorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getAuthorId() {
        return authorId;
    }
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    @NonNull
    @Override
    public String toString() {
        return "Recipe{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", ingredients='" + ingredients + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", authorId='" + authorId + '\'' +
                ", time=" + time +
                '}';
    }
}
