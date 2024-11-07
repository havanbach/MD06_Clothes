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

import com.example.md06_clothes.Models.User;
import com.example.md06_clothes.ultil.MyReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;
    private EditText edtSignUpEmail, edtSignUpPassword, edtSignUpConfirm;
    private ImageView hideup, hideupp;
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

                // Kiểm tra dữ liệu nhập vào
                if (validateInputs(email, pass, confirm)) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("iduser", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                hashMap.put("email", email);
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("IDUser").add(hashMap);

                                // Tạo dữ liệu người dùng trong Realtime Firebase
                                String username = "any name";
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
                            } else {
                                Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                Log.w("signup", "failed", task.getException());
                            }
                        }
                    });
                }
            }
        });
    }

    // Kiểm tra dữ liệu đầu vào
    private boolean validateInputs(String email, String password, String confirm) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Bạn chưa nhập Email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Bạn chưa nhập mật khẩu", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp.\nVui lòng nhập lại!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Kiểm tra email có đúng định dạng hay không
    private boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailPattern);
        return pattern.matcher(email).matches();
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

    // Hàm kiểm tra kết nối mạng
    public void broadcastIntent() {
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
}
