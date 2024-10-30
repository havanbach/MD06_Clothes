package com.example.md06_clothes.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class DetailSPActivity extends AppCompatActivity {

    private ImageView viewAnimation;
    private View viewEndAnimation;


    private AppCompatToggleButton toggleButtonFavorite;
    private FloatingActionButton btnAddCartDetail;
    private LinearLayout linearShowAllCmt;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private TextView tvGiaDetail, tvsizeDetail, tvchatlieuDetail, tvMoTaDetail;
    private ImageView imgDetail;
    private Toolbar toolbar;
    private RecyclerView rcvBinhluan;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();



    private BottomSheetDialog bottomSheetDialog;
    private TextView tvTenBottom, tvGiaBottom, tvSoluongBottom, tvMotaBottom;
    private ImageView imgBottom, btnMinusBottom, btnPlusBottom;
    private Button btnBottom;
    private int slBottom = 1;



}