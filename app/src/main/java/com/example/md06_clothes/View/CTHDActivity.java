package com.example.md06_clothes.View;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md06_clothes.Adapter.HoaDonDaGiaoAdapter;
import com.example.md06_clothes.Models.HoaDon;
import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.Models.SizeQuantity;
import com.example.md06_clothes.Presenter.GioHangPresenter;
import com.example.md06_clothes.Presenter.HoaDonPreSenter;
import com.example.md06_clothes.R;
import com.example.md06_clothes.my_interface.GioHangView;
import com.example.md06_clothes.my_interface.HoaDonView;
import com.example.md06_clothes.my_interface.IClickCTHD;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CTHDActivity extends AppCompatActivity implements GioHangView, HoaDonView {

    private ImageView imgBackCTHD;
    private TextView tvMaCTHD, tvNgaydatCTHD, tvHotenCTHD, tvDiachiCTHD, tvSdtCTHD, tvTongtienCTHD, tvTrangthaiCTHD;
    private RecyclerView rcvCTHD;
    private Button btnCapnhat;
    private HoaDon hoaDon;
    private ArrayList<Product> mlist;
    private HoaDonDaGiaoAdapter hoaDonDaGiaoAdapter;
    private GioHangPresenter gioHangPresenter;
    private HoaDonPreSenter hoaDonPreSenter;
    boolean cm;
    String danhgia = "";
    String r="5";
    private TextView tvPhuongthucCTHD;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cthdactivity);

        InitWidget();
        Init();

        imgBackCTHD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnCapnhat.setOnClickListener(view -> {
            // Cập nhật trạng thái đơn hàng
            hoaDonPreSenter.CapNhatTrangThai(4, hoaDon.getId());
            mlist.clear();
            // Cập nhật lại số lượng tồn kho cho tất cả các size của sản phẩm
            updateProductStock(mlist);
            hoaDonDaGiaoAdapter.notifyDataSetChanged();
            Toast.makeText(CTHDActivity.this, "Đã hủy đơn hàng và cập nhật tồn kho", Toast.LENGTH_SHORT).show();
            finish();
        });

    }

    private void updateProductStock(ArrayList<Product> products) {
        for (Product product : products) {
            String productId = product.getIdsp(); // Lấy ID sản phẩm
            List<SizeQuantity> sizesToUpdate = product.getSizes(); // Lấy danh sách size cần cập nhật

            db.collection("SanPham").document(productId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Lấy danh sách size từ Firestore
                            List<HashMap<String, Object>> stockSizes =
                                    (List<HashMap<String, Object>>) documentSnapshot.get("sizes");

                            if (stockSizes != null) {
                                boolean isUpdated = false;

                                for (SizeQuantity sizeToUpdate : sizesToUpdate) {
                                    for (HashMap<String, Object> stock : stockSizes) {
                                        if (stock.get("size").equals(sizeToUpdate.getSize())) {
                                            int currentStock = ((Long) stock.get("soluong")).intValue();
                                            int updatedStock = currentStock + sizeToUpdate.getSoluong(); // Cộng lại số lượng hủy
                                            stock.put("soluong", updatedStock);
                                            isUpdated = true;
                                            break; // Tiếp tục với size tiếp theo
                                        }
                                    }
                                }

                                if (isUpdated) {
                                    // Cập nhật lại toàn bộ danh sách size vào Firestore
                                    db.collection("SanPham").document(productId)
                                            .update("sizes", stockSizes)
                                            .addOnSuccessListener(aVoid -> Log.d("UpdateStock", "Tồn kho đã được cập nhật cho sản phẩm: " + productId))
                                            .addOnFailureListener(e -> Log.e("UpdateStock", "Lỗi khi cập nhật tồn kho cho sản phẩm: " + productId, e));
                                } else {
                                    Log.w("UpdateStock", "Không tìm thấy size phù hợp để cập nhật cho sản phẩm: " + productId);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("UpdateStock", "Lỗi khi lấy dữ liệu sản phẩm: " + productId, e));
        }
    }

    private void Init() {
        mlist = new ArrayList<>();
        Intent intent = getIntent();
        hoaDon = (HoaDon) intent.getSerializableExtra("HD");
        cm = intent.getBooleanExtra("CM", false);
        tvMaCTHD.setText(hoaDon.getId());
        tvNgaydatCTHD.setText(hoaDon.getNgaydat());
        tvHotenCTHD.setText(hoaDon.getHoten());
        tvDiachiCTHD.setText(hoaDon.getDiachi());
        tvSdtCTHD.setText(hoaDon.getSdt());
        tvTongtienCTHD.setText(hoaDon.getTongtien());
        tvPhuongthucCTHD.setText(hoaDon.getPhuongthuc());

        switch ((int) hoaDon.getType()){
            case  1:
                tvTrangthaiCTHD.setText("Đang xử lý");
                break;
            case  2:
                tvTrangthaiCTHD.setText("Đang giao hàng");
                btnCapnhat.setVisibility(View.GONE);
                break;
            case  3:
                tvTrangthaiCTHD.setText("Giao Hàng Thành Công");
                btnCapnhat.setVisibility(View.GONE);
                break;
            case  4:
                tvTrangthaiCTHD.setText("Hủy Đơn Hàng");
                btnCapnhat.setVisibility(View.GONE);
                break;
        }
        hoaDonPreSenter = new HoaDonPreSenter(this);
        gioHangPresenter = new GioHangPresenter(this);
        gioHangPresenter.HandlegetDataCTHD(hoaDon.getId(),hoaDon.getUid());

    }

    private void InitWidget() {
        imgBackCTHD = findViewById(R.id.img_back_cthd);
        tvMaCTHD = findViewById(R.id.tv_ma_cthd);
        tvNgaydatCTHD = findViewById(R.id.tv_ngaydat_cthd);
        tvHotenCTHD = findViewById(R.id.tv_hoten_cthd);
        tvDiachiCTHD = findViewById(R.id.tv_diachi_cthd);
        tvSdtCTHD = findViewById(R.id.tv_sdt_cthd);
        tvTongtienCTHD = findViewById(R.id.tv_tongtien_cthd);
        tvTrangthaiCTHD = findViewById(R.id.tv_trangthai_cthd);
        rcvCTHD = findViewById(R.id.rcv_cthd);
        btnCapnhat = findViewById(R.id.btn_capnhat_cthd);
        hoaDonDaGiaoAdapter = new HoaDonDaGiaoAdapter();
        tvPhuongthucCTHD = findViewById(R.id.tv_phuongthuc_cthd);

    }

    @Override
    public void getDataSanPham(String id, String id_product, String tensp, Long giatien, String hinhanh, String loaisp, String mota, List<SizeQuantity> sizes, Long type, String chatlieu) {
        mlist.add(new Product(id, id_product, tensp, giatien, hinhanh, loaisp, mota, sizes, type, chatlieu));

        Log.d("cm", "cm: " + cm+"");
        if (cm){
            hoaDonDaGiaoAdapter.setDataHoaDonDaGiao(this, mlist, 1, new IClickCTHD() {
                @Override
                public void onClickCTHD(int pos) {
                    if (cm){
                        ShowDialog(pos);
                    }
                }
            });
        } else {
            hoaDonDaGiaoAdapter.setDataHoaDonDaGiao(this, mlist, 0, new IClickCTHD() {
                @Override
                public void onClickCTHD(int pos) {
                }
            });
        }

        rcvCTHD.setLayoutManager(new LinearLayoutManager(this));
        rcvCTHD.setAdapter(hoaDonDaGiaoAdapter);

    }


    private void ShowDialog(int pos){
        Product product = mlist.get(pos);
        Dialog dialog = new Dialog(CTHDActivity.this);
        dialog.setContentView(R.layout.dialog_danhgia);
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        CustomDialog(dialog, product);
    }

    private void CustomDialog(Dialog dialog, Product product) {
        ImageView imgDanhgia = dialog.findViewById(R.id.img_danhgia);
        TextView tvNameDanhgia = dialog.findViewById(R.id.tv_name_danhgia);
        TextView tvNumberDanhgia = dialog.findViewById(R.id.tv_number_danhgia);
        RadioGroup rdgDanhgia = dialog.findViewById(R.id.rdg_danhgia);
        RadioButton rdoDanhgiaTot = dialog.findViewById(R.id.rdo_danhgia_tot);
        RadioButton rdoDanhgiaKhongTot = dialog.findViewById(R.id.rdo_danhgia_khongtot);
        RadioButton rdoDanhgiaKhac = dialog.findViewById(R.id.rdo_danhgia_khac);
        EditText edtDanhgiaKhac = dialog.findViewById(R.id.edt_danhgia_khac);
        RatingBar ratingDanhgia = dialog.findViewById(R.id.rating_danhgia);
        Button btnGuiDanhgia = dialog.findViewById(R.id.btn_danhgia_gui);
        Button btnHuyDanhgia = dialog.findViewById(R.id.btn_danhgia_huy);

        Picasso.get().load(product.getHinhanh()).into(imgDanhgia);
        tvNameDanhgia.setText(product.getTensp());
        tvNumberDanhgia.setText(String.valueOf(product.getSizes().get(0).getSoluong()));
        rdgDanhgia.clearCheck();
        edtDanhgiaKhac.setEnabled(false);
        rdgDanhgia.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (rdoDanhgiaTot.isChecked()){
                    danhgia = "Sản phẩm rất tốt";
                    edtDanhgiaKhac.setEnabled(false);
                } else if (rdoDanhgiaKhongTot.isChecked()){
                    danhgia = "Không hài lòng";
                    edtDanhgiaKhac.setEnabled(false);
                } else if (rdoDanhgiaKhac.isChecked()){
                    edtDanhgiaKhac.setEnabled(true);
                    edtDanhgiaKhac.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            danhgia = edtDanhgiaKhac.getText().toString();
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });

                }
            }
        });



        ratingDanhgia.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                r = String.valueOf(ratingDanhgia.getRating());
            }
        });

        btnGuiDanhgia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, String> map = new HashMap<>();
                map.put("idproduct", product.getIdsp());
                map.put("iduser", FirebaseAuth.getInstance().getCurrentUser().getUid());
                map.put("noidung", danhgia);
                map.put("rate", r);
                db.collection("BinhLuan").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        hoaDonDaGiaoAdapter.notifyDataSetChanged();
                        Toast.makeText(CTHDActivity.this, "Đánh giá thành công", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
            }
        });

        btnHuyDanhgia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }

    @Override
    public void getDataHD(String id, String uid, String ghichu, String diachi, String hoten, String ngaydat, String phuongthuc, String sdt, String tongtien, Long type) {

    }

    @Override
    public void OnSucess() {

    }

    @Override
    public void OnFail() {

    }


}