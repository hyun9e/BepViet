package com.example.bepviet02;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser ;
import com.google.firebase.auth.AuthResult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LoginActivityTest {

    @Mock
    FirebaseAuth mockFirebaseAuth;

    @Mock
    AuthResult mockAuthResult;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoginWithEmailAndPassword() {
        String email = "ntfakeasf@gmail.com";
        String password = "123456";

        // Giả lập hành vi của signInWithEmailAndPassword
        when(mockFirebaseAuth.signInWithEmailAndPassword(email, password)).thenReturn(notNull());

        // Thực hiện kiểm tra đăng nhập
        // Gọi phương thức đăng nhập của bạn ở đây và xác minh kết quả
    }
}