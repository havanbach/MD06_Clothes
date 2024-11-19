package com.example.md06_clothes.View.Admin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.md06_clothes.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminAddLoaiSPActivity extends AppCompatActivity {
    private ImageView imgBackThemLoai, imgThemLoai;
    private EditText edtThemLoai;
    private Button btnThemLoai;
    private static final int library_picker = 987;
    private ProgressDialog dialog;
    private String image = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_loai_spactivity);
//
//        Init();
//        Event();

    }
}
