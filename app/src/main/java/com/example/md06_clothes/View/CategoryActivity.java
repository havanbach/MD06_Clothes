package com.example.md06_clothes.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md06_clothes.Adapter.CategoryAdapter;
import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.Models.SizeQuantity;
import com.example.md06_clothes.R;
import com.example.md06_clothes.my_interface.IClickOpenBottomSheet;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {
    private ImageView imgBack;
    private TextView tvCategory;
    private EditText edtSearch;
    private RecyclerView rcvCategory;

    private CategoryAdapter categoryAdapter;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private Product product;

    // Khai báo các mảng danh sách
    private ArrayList<Product> arr_khac;
    private ArrayList<Product> arr_micay;
    private ArrayList<Product> arr_chaosup;
    private ArrayList<Product> arr_pizza;
    private ArrayList<Product> arr_sandwich;
    private ArrayList<Product> arr_douong;
    private ArrayList<Product> arr_lau;
    private ArrayList<Product> arr_doannhanh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Khởi tạo các thành phần giao diện và dữ liệu
        InitWidget();
        Init();
        Event();
    }

    // Khởi tạo dữ liệu ban đầu
    private void Init() {
        Intent intent = getIntent();
        String loaiProduct = intent.getStringExtra("loaiproduct"); // Nhận loại sản phẩm từ Intent
        tvCategory.setText(loaiProduct); // Hiển thị tên loại sản phẩm

        // Xác định danh sách hiện tại dựa trên loại sản phẩm
        ArrayList<Product> currentList = getListByCategory(loaiProduct);

        // Truy vấn dữ liệu từ Firestore
        firestore.collection("SanPham")
                .whereEqualTo("loaisp", loaiProduct)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot d : queryDocumentSnapshots) {
                                List<SizeQuantity> sizes = new ArrayList<>();
                                List<Map<String, Object>> sizesData = (List<Map<String, Object>>) d.get("sizes");
                                if (sizesData != null) {
                                    for (Map<String, Object> size : sizesData) {
                                        sizes.add(new SizeQuantity((String) size.get("size"), ((Long) size.get("soluong")).intValue()));
                                    }
                                }
                                currentList.add(new Product(
                                        d.getId(),
                                        d.getString("tensp"),
                                        d.getId(),
                                        d.getLong("giatien"),
                                        d.getString("hinhanh"),
                                        d.getString("loaisp"),
                                        d.getString("mota"),
                                        sizes,
                                        d.getLong("type"),
                                        d.getString("chatlieu")
                                ));
                            }
                            // Cập nhật adapter với danh sách sản phẩm
                            categoryAdapter = new CategoryAdapter(CategoryActivity.this, currentList, new IClickOpenBottomSheet() {
                                @Override
                                public void onClickOpenBottomSheet(int position) {
                                    product = currentList.get(position);
                                    SendData();
                                }
                            });
                            rcvCategory.setLayoutManager(new LinearLayoutManager(CategoryActivity.this, RecyclerView.VERTICAL, false));
                            rcvCategory.setAdapter(categoryAdapter);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("error", "Lỗi: " + e.getMessage());
                        Toast.makeText(CategoryActivity.this, "Lỗi khi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Gửi dữ liệu sản phẩm sang `DetailSPActivity`
    private void SendData() {
        Intent intent = new Intent(CategoryActivity.this, DetailSPActivity.class);
        intent.putExtra("search", product);
        startActivity(intent);
    }

    // Quản lý sự kiện giao diện
    private void Event() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Quay lại màn hình trước đó
            }
        });
    }

    // Khởi tạo các thành phần giao diện
    private void InitWidget() {
        imgBack = findViewById(R.id.img_back);
        tvCategory = findViewById(R.id.tv_category);
        edtSearch = findViewById(R.id.edt_search);
        rcvCategory = findViewById(R.id.rcv_category);

        // Khởi tạo các mảng danh sách sản phẩm
        arr_khac = new ArrayList<>();
        arr_micay = new ArrayList<>();
        arr_chaosup = new ArrayList<>();
        arr_pizza = new ArrayList<>();
        arr_sandwich = new ArrayList<>();
        arr_douong = new ArrayList<>();
        arr_lau = new ArrayList<>();
        arr_doannhanh = new ArrayList<>();
    }

    // Trả về danh sách sản phẩm dựa theo loại
    private ArrayList<Product> getListByCategory(String loaiProduct) {
        switch (loaiProduct.toLowerCase()) {
            case "micay":
                return arr_micay;
            case "chaosup":
                return arr_chaosup;
            case "pizza":
                return arr_pizza;
            case "sandwich":
                return arr_sandwich;
            case "douong":
                return arr_douong;
            case "lau":
                return arr_lau;
            case "doannhanh":
                return arr_doannhanh;
            default:
                return arr_khac; // Mặc định trả về danh sách `arr_khac`
        }
    }
}
