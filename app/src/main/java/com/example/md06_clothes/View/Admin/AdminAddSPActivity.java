package com.example.md06_clothes.View.Admin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminAddSPActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
        Event();
    }

    private void Event() {
        btnAddBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        imgAddLoaiProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminAddSPActivity.this, AdminAddLoaiSPActivity.class);
                intent.putExtra("loaisp", loaisp);
                startActivity(intent);
            }
        });
        btnDanhmuc.setOnClickListener(view -> spinnerDanhMuc.performClick());
        spinnerDanhMuc.setOnItemSelectedListener(this);
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edtsizeSP.setText("");
                edtchatlieuSP.setText("");
                edtTenSP.setText("");
                edtGiatienSP.setText("");
                edtTypeSP.setText("");
                edtMotaSP.setText("");
                edtSoluongSP.setText("");
                image = "";
                imgAdd.setImageResource(R.drawable.pl);
            }
        });
        // Nếu xóa bất kỳ 1 sản phẩm nào đó thì những hóa đơn có chứa sản phẩm đó cũng phải bị xóa hoặc dùng nhiều cách khác.
        // Ở đây lựa chọn xóa luôn hóa đơn chứa sản phẩm bị xóa.
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("SanPham").document(product.getId()).delete().addOnSuccessListener(unused -> {
                    db.collection("IDUser").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot q: queryDocumentSnapshots){
                                Log.d("checkiduser", q.getString("iduser"));

                                // Từ iduser mà ta có, lấy ra tất cả id_hoadon có id_product là KlUnpxIGoIFkHlvshil2
                                db.collection("ChitietHoaDon").document(q.getString("iduser")).
                                        collection("ALL").whereEqualTo("id_product", product.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for (QueryDocumentSnapshot d: queryDocumentSnapshots){
                                                    Log.d("checkidhoadon", d.getString("id_hoadon"));

                                                    // Từ id_hoadon mà ta có, thực hiện xóa id hóa đơn của bảng HoaDon
                                                    db.collection("HoaDon").document(d.getString("id_hoadon")).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Toast.makeText(AdminAddSPActivity.this, "Xoá sản phẩm thành công!!!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
//                                            db.collection("QRproduct").document(product.getId()).delete();
                                                }
                                            }
                                        });
                            }

                        }
                    });

                    setResult(RESULT_OK);
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(AdminAddSPActivity.this, "Xoá sản phẩm thất bại!!!", Toast.LENGTH_SHORT).show();
                });

            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) {
                    return;
                }
                try {
                    Product sp = new Product();
                    sp.setGiatien(Long.parseLong(edtGiatienSP.getText().toString()));
                    sp.setMota(edtMotaSP.getText().toString());
                    sp.setsize(edtsizeSP.getText().toString());
                    sp.setType(Long.parseLong(edtTypeSP.getText().toString()));
                    sp.setTensp(edtTenSP.getText().toString());
                    sp.setSoluong(Long.parseLong(edtSoluongSP.getText().toString()));
                    sp.setchatlieu(edtchatlieuSP.getText().toString());
                    sp.setLoaisp(spinnerDanhMuc.getSelectedItem().toString());
                    sp.setHinhanh(image);

                    db.collection("SanPham").add(sp).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(@NonNull DocumentReference documentReference) {
                            Toast.makeText(AdminAddSPActivity.this, "Thành công!!!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AdminAddSPActivity.this, "Thất bại!!!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void pickImage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    LIBRARY_PICKER);
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), LIBRARY_PICKER);
        }
    }
    private boolean validate() {
        if (TextUtils.isEmpty(image)) {
            Toast.makeText(this, "Vui lòng chọn hình ảnh", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(edtGiatienSP.getText().toString())) {
            Toast.makeText(this, "Vui lòng nhập giá tiền", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(edtTenSP.getText().toString())) {
            Toast.makeText(this, "Vui lòng nhập tên sản phẩm", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(edtsizeSP.getText().toString())) {
            Toast.makeText(this, "Vui lòng nhập Size", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(edtchatlieuSP.getText().toString())) {
            Toast.makeText(this, "Vui lòng nhập Chất liệu", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(edtSoluongSP.getText().toString())) {
            Toast.makeText(this, "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(edtTypeSP.getText().toString())) {
            Toast.makeText(this, "Vui lòng nhập type", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(edtMotaSP.getText().toString())) {
            Toast.makeText(this, "Vui lòng nhập mô tả", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        if (position > 0) {
            btnDanhmuc.setText(spinnerDanhMuc.getSelectedItem().toString());
            String s = list.get(position);
            loaisp = s;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
