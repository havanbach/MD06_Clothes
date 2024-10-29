package com.example.md06_clothes;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignInActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;
    private EditText edt_passin, edt_mailin;
    private ImageView hidein;
    private Button btn_signin;
    private TextView txtsignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        hidein = findViewById(R.id.hidein);
        edt_passin = findViewById(R.id.edt_matkhau_user);
        btn_signin = findViewById(R.id.btn_signin);
        txtsignup = findViewById(R.id.txtSignup);


        hidein = findViewById(R.id.hidein);
        edt_passin = findViewById(R.id.edt_matkhau_user);
        btn_signin = findViewById(R.id.btn_signin);
        txtsignup = findViewById(R.id.txtSignup);
        hidein.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    edt_passin.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    hidein.setImageResource(R.drawable.hide);
                } else {
                    edt_passin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    hidein.setImageResource(R.drawable.hideon);
                }
                isPasswordVisible = !isPasswordVisible;
                edt_passin.setSelection(edt_passin.getText().length());
            }
        });
        txtsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });
        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, MainActivity.class));

            }
        });
    }
}