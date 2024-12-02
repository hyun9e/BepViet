package com.example.bepviet02;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bepviet02.databinding.ActivitySettingsBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUi();
    }

    private void initUi() {
        binding.btnSignout.setOnClickListener(view -> signOut());
        binding.linearLayoutShare.setOnClickListener(view -> shareApp());
        binding.linearLayoutContact.setOnClickListener(view -> contactDeveloper());
        binding.linearLayoutPrivacy.setOnClickListener(view -> viewPrivacy());
        binding.btnGoBack.setOnClickListener(view -> finish());
    }

    private void signOut() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc muốn đăng xuất không?")
                    .setPositiveButton("Không",(dialogInterface, i) -> dialogInterface.dismiss())
                    .setNegativeButton("Có", (dialogInterface, i) -> {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finishAffinity();
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    private void shareApp() {
        String app_link = "https://drive.google.com/drive/folders/1t2e8J6Z-sa9ie3U6dP9_EGK3Kv5ok1SR?usp=sharing";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(app_link)));
    }

    private void contactDeveloper() {
        String emailsend = "ntfakeasf@gmail.com";
        String emailsubject = "Yêu cầu hỗ trợ từ Bếp Việt";
        String emailbody = "Chào bạn, tôi cần hỗ trợ về...";
        // Define the email URI
        Uri emailUri = Uri.parse("mailto:" + emailsend)
                .buildUpon()
                .appendQueryParameter("subject", emailsubject)
                .appendQueryParameter("body", emailbody)
                .build();
        // Create the intent
        Intent intent = new Intent(Intent.ACTION_SENDTO, emailUri);
        // Check if there is an app that can handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the email activity
            startActivity(Intent.createChooser(intent, "Chọn ứng dụng để thao tác"));
        } else {
            // Handle the case where no email app is available
            Toast.makeText(this, "Không có ứng dụng email nào được cài đặt.", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewPrivacy() {
        String privacy_link = "https://www.privacypolicies.com/live/7d678374-4ab4-47d9-946f-8fa8c233b72c";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(privacy_link)));
    }

}