package com.example.md06_clothes;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUpActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;
    private EditText edt_sign_up_email,edt_sign_up_password,textPassword;
    private ImageView hideup,hideupp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        TextView txtSignin = findViewById(R.id.txtSignin);
        edt_sign_up_email = findViewById(R.id.edt_sign_up_email);
        edt_sign_up_password = findViewById(R.id.edt_sign_up_password);
        textPassword = findViewById(R.id.textPassword);
        hideup = findViewById(R.id.hidein);
        hideupp = findViewById(R.id.hideinn);
        hideup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    edt_sign_up_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    hideup.setImageResource(R.drawable.hide);
                } else {
                    edt_sign_up_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    hideup.setImageResource(R.drawable.hideon);
                }
                isPasswordVisible = !isPasswordVisible;
                edt_sign_up_password.setSelection(edt_sign_up_password.getText().length());
            }
        });
        txtSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignUpActivity.class));
            }
        });
        hideupp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    textPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    hideupp.setImageResource(R.drawable.hide);
                } else {
                    textPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    hideupp.setImageResource(R.drawable.hideon);
                }
                isPasswordVisible = !isPasswordVisible;
                textPassword.setSelection(textPassword.getText().length());
            }
        });
    }
}