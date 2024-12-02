package com.example.bepviet02;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bepviet02.databinding.ActivitySignUpBinding;
import com.example.bepviet02.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tashila.pleasewait.PleaseWaitDialog;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;
    private PleaseWaitDialog pleaseWaitDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance(); // [END initialize_auth]
        // Initialize ViewBinding
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Set up click listener for the sign up - Login button
        binding.btnSignup.setOnClickListener(view -> signUp());
        binding.txtLogin.setOnClickListener(view -> finish());
    }

    private void signUp() {
        binding.edtConfirmPassword.clearFocus();
        String name = Objects.requireNonNull(binding.edtName.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.edtEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.edtPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.edtConfirmPassword.getText()).toString().trim();
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Cần nhập đủ thông tin", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Cần nhập email hợp lệ", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
        } else {
            // Create new user
            pleaseWaitDialog = new PleaseWaitDialog(this);
            pleaseWaitDialog.setMessage("Đang tải");
            pleaseWaitDialog.setCancelable(false);
            pleaseWaitDialog.show();
            createNewUser(name, email, password);
        }
    }

    private void createNewUser(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    pleaseWaitDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        saveUserInfoToDatabase(name, email);
                    } else {
                        Toast.makeText(this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void saveUserInfoToDatabase(String name, String email) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users");
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        User user = new User(userId, name, email, "", "");
        dbRef.child(userId).setValue(user).addOnCompleteListener(task -> {
            pleaseWaitDialog.dismiss();
            if (task.isComplete()){
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                finishAffinity();
            }
            else {
                Toast.makeText(SignUpActivity.this, "Đã có lỗi` xảy ra", Toast.LENGTH_SHORT).show();
                Log.d("TAG", "onComplete: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

}