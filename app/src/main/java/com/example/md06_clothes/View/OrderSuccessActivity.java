package com.example.md06_clothes.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.R;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OrderSuccessActivity extends AppCompatActivity {
    private TextView tvIDHoadon, tvDateHoadon, tvSanphamHoadon,
            tvNameHoadon, tvDiachiHoadon, tvSDTHoadon, tvPhuongthucHoadon, tvGhichuHoadon, tvTongtienHoadon;
    private Button btnHoanthanhHoadon, btnXuatPDFHoadon;

    private String idhoadon, ngaydat, hoten, diachi, sdt, phuongthuc, ghichu, sanpham, tienthanhtoan;
    private Bitmap bmp,scalebmp;

    int pageWidth = 1200;
    private Date dateObj;
    private DateFormat dateFormat;

    private Product product;
    private ArrayList<Product> mlist;
    private int i = 0;
    private int j = 0;
    private String total = "";
    private int tong = 0;
    private int num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        InitWidget();
        Init();
        Event();
    }

    private void Event() {

        btnHoanthanhHoadon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void Init() {

        Intent intent = getIntent();
        idhoadon = intent.getStringExtra("idhoadon");
        ngaydat = intent.getStringExtra("ngaydat");
        hoten = intent.getStringExtra("hoten");
        diachi = intent.getStringExtra("diachi");
        sdt = intent.getStringExtra("sdt");
        phuongthuc = intent.getStringExtra("phuongthuc");
        ghichu = intent.getStringExtra("ghichu");
        sanpham = intent.getStringExtra("sanpham");
        tienthanhtoan = intent.getStringExtra("tienthanhtoan");

        mlist = new ArrayList<>();
        mlist = (ArrayList<Product>) intent.getSerializableExtra("serialzable");


        tvIDHoadon.setText(idhoadon);
        tvDateHoadon.setText(ngaydat);
        tvNameHoadon.setText(hoten);
        tvDiachiHoadon.setText(diachi);
        tvSDTHoadon.setText(sdt);
        tvPhuongthucHoadon.setText(phuongthuc);
        tvGhichuHoadon.setText(ghichu);
        tvSanphamHoadon.setText(sanpham);
        tvTongtienHoadon.setText(tienthanhtoan);


        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);


        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pizzahead);
        scalebmp = Bitmap.createScaledBitmap(bmp, 1200, 518, false);

    }

    private void InitWidget() {
        tvIDHoadon = findViewById(R.id.tv_id_hoadon);
        tvDateHoadon = findViewById(R.id.tv_date_hoadon);
        tvNameHoadon = findViewById(R.id.tv_name_hoadon);
        tvDiachiHoadon = findViewById(R.id.tv_diachi_hoadon);
        tvSDTHoadon = findViewById(R.id.tv_sdt_hoadon);
        tvPhuongthucHoadon = findViewById(R.id.tv_phuongthuc_hoadon);
        tvGhichuHoadon = findViewById(R.id.tv_ghichu_hoadon);
        tvSanphamHoadon = findViewById(R.id.tv_sanpham_hoadon);
        tvTongtienHoadon = findViewById(R.id.tv_tongtien_hoadon);
        btnHoanthanhHoadon = findViewById(R.id.btn_hoanthanh_hoadon);
    }
}