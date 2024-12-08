package com.example.bepviet02;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bepviet02.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    protected ActivityLoginBinding binding;
    protected FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Initialize Firebase Auth (private FirebaseAuth mAuth;)
        initUi();

    }

    private void initUi() {
        mAuth = FirebaseAuth.getInstance();
        binding.btnLogin.setOnClickListener(view -> validLoginInfo());
        binding.tvSignUp.setOnClickListener(view -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });
        binding.tvGuestLogin.setOnClickListener(view -> loginAsGuest());
    }

    private void loginAsGuest() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void validLoginInfo() {
        binding.edtPassword.clearFocus();
        String email = Objects.requireNonNull(binding.edtEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.edtPassword.getText()).toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Cần nhập email", Toast.LENGTH_SHORT).show();
        }
        else if (password.isEmpty()) {
            Toast.makeText(this, "Cần nhập mật khẩu", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Cần nhập email hợp lệ", Toast.LENGTH_SHORT).show();
        } else {
            login(email, password);
        }
    }

    private void login(String email, String password){
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Đăng nhập thất bại, tài khoản/mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}