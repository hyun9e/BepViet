package com.example.bepviet02;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.cloudinary.android.MediaManager;
import com.example.bepviet02.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate and set content view using View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUi();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showConfirmDialog();
            }
        });
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thoát ứng dụng")
                .setMessage("Bạn có muốn thoát ứng dụng không?")
                .setNegativeButton("Có", (dialog, which) -> finishAffinity())
                .setPositiveButton("Không", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void initUi() {
        // Set up BottomNavigationView with NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
        // Initialize Cloudinary MediaManager (Once)
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Hãy đăng nhập để cùng chia sẻ công thức món ăn", Toast.LENGTH_SHORT).show();
            binding.fabAdd.setOnClickListener(view -> {
                new AlertDialog.Builder(this)
                        .setTitle("Bạn có muốn đăng nhập không?")
                        .setMessage("Để chia sẻ công thức món ăn, bạn cần đăng nhập tài khoản.")
                        .setNegativeButton("Có", (dialog, which) ->
                                startActivity(new Intent(this, LoginActivity.class)))
                        .setPositiveButton("Không", (dialog, which) -> dialog.dismiss())
                        .setCancelable(false)
                        .show();
            });
        }
        else {
            MediaManager.init(this);
            // FloatingActionButton click listener
            binding.fabAdd.setOnClickListener(view -> startActivity(new Intent(this, AddRecipeActivity.class)) );
        }
    }

}