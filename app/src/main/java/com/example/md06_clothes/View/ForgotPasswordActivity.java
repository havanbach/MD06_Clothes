package com.example.md06_clothes.View;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.md06_clothes.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText edtEmailForgot;
    private Button btnForgot;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        InitWidget();
        Event();
    }

    private void InitWidget() {
        edtEmailForgot = findViewById(R.id.edt_email_forgot);
        btnForgot = findViewById(R.id.btn_forgot);
    }

    private void Event() {
        btnForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stremail = edtEmailForgot.getText().toString().trim();

                if (stremail.isEmpty()) {
                    edtEmailForgot.setError("Vui lòng nhập email");
                    edtEmailForgot.requestFocus();
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(stremail).matches()) {
                    edtEmailForgot.setError("Email không hợp lệ");
                    edtEmailForgot.requestFocus();
                    return;
                }

                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.fetchSignInMethodsForEmail(stremail)
                        .addOnSuccessListener(new OnSuccessListener<com.google.firebase.auth.SignInMethodQueryResult>() {
                            @Override
                            public void onSuccess(com.google.firebase.auth.SignInMethodQueryResult result) {
                                if (result.getSignInMethods() == null || result.getSignInMethods().isEmpty()) {
                                    Toast.makeText(ForgotPasswordActivity.this, "Email chưa được đăng ký tài khoản. Vui lòng tạo tài khoản trước.", Toast.LENGTH_LONG).show();
                                } else {
                                    auth.sendPasswordResetEmail(stremail)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(ForgotPasswordActivity.this, "Email khôi phục mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư của bạn", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(ForgotPasswordActivity.this, "Yêu cầu thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ForgotPasswordActivity.this, "Yêu cầu thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
}
