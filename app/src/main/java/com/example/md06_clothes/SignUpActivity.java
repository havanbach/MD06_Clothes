package com.example.md06_clothes;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.md06_clothes.Models.User;
import com.example.md06_clothes.ultil.MyReceiver;
import com.example.md06_clothes.ultil.NetworkUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;
    private EditText edtSignUpEmail,edtSignUpPassword,edtSignUpConfirm;
    private ImageView hideup,hideupp;
    private BroadcastReceiver MyReceiver = null;
    DatabaseReference reference1, reference2;

    private Button btnSignUpDangKy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        TextView txtSignin = findViewById(R.id.txtSignin);
        InitWidget();
        MyReceiver = new MyReceiver();      // Check Internet
        broadcastIntent();                  // Check Internet
//        if (NetworkUtil.isNetworkConnected(this)){
//            Event();
//        }

        hideup = findViewById(R.id.hidein);
        hideupp = findViewById(R.id.hideinn);
        hideup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    edtSignUpPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    hideup.setImageResource(R.drawable.hide);
                } else {
                    edtSignUpPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    hideup.setImageResource(R.drawable.hideon);
                }
                isPasswordVisible = !isPasswordVisible;
                edtSignUpPassword.setSelection(edtSignUpPassword.getText().length());
            }
        });
        txtSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            }
        });
        hideupp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    edtSignUpConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    hideupp.setImageResource(R.drawable.hide);
                } else {
                    edtSignUpConfirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    hideupp.setImageResource(R.drawable.hideon);
                }
                isPasswordVisible = !isPasswordVisible;
                edtSignUpConfirm.setSelection(edtSignUpConfirm.getText().length());
            }
        });
        btnSignUpDangKy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtSignUpEmail.getText().toString().trim();
                String pass = edtSignUpPassword.getText().toString().trim();
                String confirm = edtSignUpConfirm.getText().toString().trim();
                if (email.length() > 0){
                    if (pass.length() > 0){
                        if (pass.equals(confirm)){
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        HashMap<String,String> hashMap =  new HashMap<>();
                                        hashMap.put("iduser",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        hashMap.put("email", email);
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        db.collection("IDUser").add(hashMap);

                                        // Realtime Firebase: Tạo 1 database có tên Users, id tự động đặt cho tài khoản
                                        String username= "any name";
                                        reference1 = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        HashMap<String, String> mapRealtime = new HashMap<>();
                                        mapRealtime.put("iduser", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        mapRealtime.put("name", username);
                                        mapRealtime.put("avatar", "default");
                                        mapRealtime.put("status", "online");
                                        mapRealtime.put("search", username.toLowerCase());
                                        reference1.setValue(mapRealtime);

                                        reference2 = FirebaseDatabase.getInstance().getReference("Chatlist").child("WvPK8OV0erKJP8w2KZNp")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        HashMap<String, String> mapRealtime2 = new HashMap<>();
                                        mapRealtime2.put("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        reference2.setValue(mapRealtime2);


                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        User user = new User();
                                        user.setIduser(auth.getUid());
                                        user.setEmail(email);
                                        finishAffinity();
                                        Toast.makeText(SignUpActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                    } else if (!isEmailValid(email)){
                                        Toast.makeText(SignUpActivity.this, "Email định dạng không đúng", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                        Log.w("signup","failed", task.getException());
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Mật khẩu xác nhận không khớp.\nVui lòng nhập lại!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Bạn chưa nhập mật khẩu", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(SignUpActivity.this, "Bạn chưa nhập Email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void InitWidget() {
        edtSignUpEmail = findViewById(R.id.edt_sign_up_email);
        edtSignUpPassword = findViewById(R.id.edt_sign_up_password);
        edtSignUpConfirm = findViewById(R.id.edt_sign_up_confirm);
        btnSignUpDangKy = findViewById(R.id.btn_sign_up_dangky);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(MyReceiver);
    }
    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    public void broadcastIntent() {
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

}