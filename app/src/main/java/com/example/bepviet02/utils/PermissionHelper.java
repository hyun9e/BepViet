package com.example.bepviet02.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    private final Activity activity;
    private static final int REQUEST_CODE_PERMISSIONS = 100;

    public PermissionHelper(Activity activity) {
        this.activity = activity;
    }

    public void requestPermissions(String[] permissions, PermissionCallback callback) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            callback.onPermissionsGranted();
        } else {
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_PERMISSIONS);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, PermissionCallback callback) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                callback.onPermissionsGranted();
            } else {
                callback.onPermissionsDenied();
            }
        }
    }

    public interface PermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied();
    }
}