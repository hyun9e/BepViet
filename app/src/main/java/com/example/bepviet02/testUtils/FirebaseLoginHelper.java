package com.example.bepviet02.testUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser ;

public class FirebaseLoginHelper {

    public interface LoginCallback {
        void onLoginSuccess(FirebaseUser  user);
        void onLoginFailure(String errorMessage);
    }

    public static void login(String email, String password, LoginCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser  user = mAuth.getCurrentUser ();
                callback.onLoginSuccess(user);
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed";
                callback.onLoginFailure(errorMessage);
            }
        });
    }

    public static void logout() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser () != null) {
            mAuth.signOut();
        }
    }

    public static boolean isLoggedIn() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser () != null;
    }
}