package com.example.md06_clothes.View.Admin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminAddSPActivity extends AppCompatActivity {
    private TextView tvTaoMaQR;
    private ImageView imgQRProduct;
    private Button btnQRProduct, btnDownQRProduct;

    private CircleImageView imgAddLoaiProduct;
    private ImageView btnAddBack, btnRefresh, btnSave;
    private EditText edtTenSP, edtGiatienSP, edtsizeSP, edtchatlieuSP, edtSoluongSP, edtTypeSP, edtMotaSP;
    private ImageButton imgAdd;
    private Button btnDanhmuc, btnDelete;
    private AppCompatButton btnEdit;
    private Spinner spinnerDanhMuc;
    private TextView tvTitle;

    private FirebaseFirestore db;
    private List<String> list;
    private Product product;
    private String image = "";
    private static final int LIBRARY_PICKER = 12312;
    private ProgressDialog dialog;
    private String loaisp = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_spactivity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        InitWidget();
//        Init();

//        // Test: Lấy ra id của hóa đơn từ bảng HoaDon
//        db.collection("HoaDon").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                for (QueryDocumentSnapshot q: queryDocumentSnapshots){
//                    ArrayList<String> list = new ArrayList<>();
//                    list.add(q.getId());
//                    Log.d("checkid", q.getId());
//                }
//            }
//        });
//        Event();
    }
}
