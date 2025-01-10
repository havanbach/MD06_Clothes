package com.example.md06_clothes;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.metrics.Event;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.md06_clothes.Models.Admin;
import com.example.md06_clothes.View.ForgotPasswordActivity;
import com.example.md06_clothes.ultil.MyReceiver;
import com.example.md06_clothes.ultil.NetworkUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignInActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;
    private EditText edt_passin, edt_mailin;
    private ImageView hidein;
    private Button btn_signin;
    private TextView txtsignup;
    private Button btnDangNhap;
    private TextView btnDangKy;
    private EditText edtEmailUser, edtMatKhauUser;
    private TextView tvForgotPassword;
    private CircleImageView cirDangNhapFacebook, cirDangNhapGoogle;
    private static final int TIME_DELAY = 2000;
    private static long back_pressed;
    private String sosanh;
    private ProgressDialog progressDialog;

    private BroadcastReceiver MyReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        hidein = findViewById(R.id.hidein);
        edt_passin = findViewById(R.id.edt_matkhau_user);
        btnDangNhap = findViewById(R.id.btn_dangnhap);
        btnDangKy = findViewById(R.id.btn_dangky);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        InitWidget();
        MyReceiver = new MyReceiver();
        broadcastIntent();
        if (NetworkUtil.isNetworkConnected(this)) {
            Event();
        }

        hidein = findViewById(R.id.hidein);
        edt_passin = findViewById(R.id.edt_matkhau_user);
        btnDangNhap = findViewById(R.id.btn_dangnhap);
        btnDangKy = findViewById(R.id.btn_dangky);
        btnDangKy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this,SignUpActivity.class));
            }
        });
        hidein.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    edt_passin.setTransformationMethod(PasswordTransformationMethod.getInstance()); //An mat khau
                    hidein.setImageResource(R.drawable.hide);
                } else {
                    edt_passin.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); // Hien mat khau
                    hidein.setImageResource(R.drawable.hideon);
                }
                isPasswordVisible = !isPasswordVisible;
                edt_passin.setSelection(edt_passin.getText().length());
            }
        });
    }
    private void Event() {
        btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                progressDialog.setContentView(R.layout.layout_loading);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                String strEmail = edtEmailUser.getText().toString().trim();
                String strMatKhau = edtMatKhauUser.getText().toString().trim();
                ArrayList<Admin> arrayList = new ArrayList<>();
                if (strEmail.length() > 0) {
                    if (strMatKhau.length() > 0) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Admin").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                                    arrayList.add(new Admin(q.getString("username"), q.getString("pass")));
                                }
                                String tkadmin;
                                String mkadmin;
                                int tk = 0;
                                for (int i = 0; i < arrayList.size(); i++) {
                                    Admin admin = arrayList.get(i);
                                    tkadmin = admin.getUsername();
                                    mkadmin = admin.getPassword();
                                    sosanh = tkadmin;
                                    if (strEmail.equals(tkadmin) && strMatKhau.equals(mkadmin)) {
                                        tk = 1;
                                        break;
                                    } else {
                                        tk = 2;
                                    }
                                }
                                //xu ly ket qua
                                switch (tk) {
                                    case 1:
                                    case 2:
                                        FirebaseAuth auth = FirebaseAuth.getInstance();
                                        auth.signInWithEmailAndPassword(strEmail, strMatKhau).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finishAffinity();
                                                    progressDialog.dismiss();
                                                } else if (!isEmailValid(strEmail) && !sosanh.equals(strEmail)) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(SignInActivity.this, "Email định dạng không đúng", Toast.LENGTH_SHORT).show();

                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(SignInActivity.this, "Tài khoản hoặc mật khẩu không đúng.\nVui lòng kiểm tra lại!", Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });

                                        break;
                                }
                            }
                        });

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(SignInActivity.this, "Bạn chưa nhập mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(SignInActivity.this, "Bạn chưa nhập tài khoản", Toast.LENGTH_SHORT).show();
                }

            }
        });
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentForgot = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
                startActivity(intentForgot);
            }
        });
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    @Override
    public void onBackPressed() {
        progressDialog.dismiss();
        // Sự kiện nhấn back 2 lần để out app
        if (back_pressed + TIME_DELAY > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), "Press once again to exit!",
                    Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();

    }
    private void InitWidget() {
        btnDangNhap = findViewById(R.id.btn_dangnhap);
        btnDangKy = findViewById(R.id.btn_dangky);
        edtEmailUser = findViewById(R.id.edt_email_user);
        edtMatKhauUser = findViewById(R.id.edt_matkhau_user);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        progressDialog = new ProgressDialog(this);
    }

    public void broadcastIntent() {
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(MyReceiver);
    }
}