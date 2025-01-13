package com.example.md06_clothes.View;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md06_clothes.Adapter.GiohangAdapter;
import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.Models.SizeQuantity;
import com.example.md06_clothes.Presenter.GioHangPresenter;
import com.example.md06_clothes.R;
import com.example.md06_clothes.my_interface.GioHangView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vn.momo.momo_partner.AppMoMoLib;

public class CartActivity extends AppCompatActivity implements GioHangView {

    private ScrollView scrollViewCart;
    private TextView tvNullCart, tvDongia, tvPhiVanChuyen, tvTongTien, btnThanhToan;
    private ImageView imgBackCart,imgBackEmptyCart;
    private RecyclerView rcvGioHang;

    private GiohangAdapter giohangAdapter;
    private GioHangPresenter gioHangPresenter;
    private ArrayList<Product> listGiohang;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String[] paymentMethods = {"Thanh toán khi nhận hàng", "Thanh toán MoMo"};
    private String tienthanhtoan, hoten, diachi, sdt, ghichu, ngaydat, phuongthuc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        InitWidget();
        SetupRecyclerView();
        LoadCartData();
        SetupEvents();
    }

    private void InitWidget() {
        scrollViewCart = findViewById(R.id.scrollView_cart);
        tvNullCart = findViewById(R.id.tv_null_cart);
        rcvGioHang = findViewById(R.id.rcv_giohang);
        tvDongia = findViewById(R.id.tv_dongia);
        tvPhiVanChuyen = findViewById(R.id.tv_phivanchuyen);
        tvTongTien = findViewById(R.id.tv_tongtien);
        btnThanhToan = findViewById(R.id.btn_thanhtoan);
        imgBackCart = findViewById(R.id.img_back_cart);
        imgBackEmptyCart = findViewById(R.id.img_back_empty_cart);

        listGiohang = new ArrayList<>();
        gioHangPresenter = new GioHangPresenter(this);

        // Xử lý nút Back khi giỏ hàng không trống
        if (imgBackCart != null) {
            imgBackCart.setOnClickListener(v -> finish());
        }

        // Xử lý nút Back khi giỏ hàng trống
        if (imgBackEmptyCart != null) {
            imgBackEmptyCart.setOnClickListener(v -> finish());
        }

    }

    private void SetupRecyclerView() {
        giohangAdapter = new GiohangAdapter(this, listGiohang, this);
        rcvGioHang.setLayoutManager(new LinearLayoutManager(this));
        rcvGioHang.setAdapter(giohangAdapter);

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                ShowDeleteConfirmationDialog(pos);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(rcvGioHang);
    }

    private void SetupEvents() {
        btnThanhToan.setOnClickListener(v -> {
            if (listGiohang.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
            } else {
                ShowPaymentDialog();
            }
        });
    }

    private void LoadCartData() {
        gioHangPresenter.HandlegetDataGioHang();
    }

    private void ShowDeleteConfirmationDialog(int pos) {
        new android.app.AlertDialog.Builder(this)
                .setMessage("Bạn có muốn xóa sản phẩm " + listGiohang.get(pos).getTensp() + " không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    gioHangPresenter.HandleDeleteDataGioHang(listGiohang.get(pos).getId());
                    listGiohang.remove(pos);
                    giohangAdapter.notifyDataSetChanged();
                    UpdateCartSummary();
                    CheckCartVisibility();
                    Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", (dialog, which) -> giohangAdapter.notifyDataSetChanged())
                .show();
    }


    private void ShowPaymentDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_thanhtoan);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Spinner spinnerPhuongthuc = dialog.findViewById(R.id.spinner_phuongthuc);
        EditText edthoten = dialog.findViewById(R.id.edt_hoten);
        EditText edtdiachi = dialog.findViewById(R.id.edt_diachi);
        EditText edtsdt = dialog.findViewById(R.id.edt_sdt);
        EditText edtghichu = dialog.findViewById(R.id.edt_ghichu);
        TextView tvtongtien = dialog.findViewById(R.id.tv_tongtien);
        Button btnxacnhan = dialog.findViewById(R.id.btn_xacnhan);
        ImageView btnCancel = dialog.findViewById(R.id.btn_cancel);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, paymentMethods);
        spinnerPhuongthuc.setAdapter(adapter);

        db.collection("User").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Profile")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        edthoten.setText(doc.getString("hoten"));
                        edtdiachi.setText(doc.getString("diachi"));
                        edtsdt.setText(doc.getString("sdt"));
                    }
                });

        tvtongtien.setText(tvTongTien.getText().toString());

        btnxacnhan.setOnClickListener(v -> {
            hoten = edthoten.getText().toString().trim();
            diachi = edtdiachi.getText().toString().trim();
            sdt = edtsdt.getText().toString().trim();
            ghichu = edtghichu.getText().toString().trim();
            phuongthuc = spinnerPhuongthuc.getSelectedItem().toString();
            tienthanhtoan = tvtongtien.getText().toString();

            // Lấy ngày đặt hiện tại
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            ngaydat = simpleDateFormat.format(System.currentTimeMillis());

            if (hoten.isEmpty() || diachi.isEmpty() || sdt.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            //sdt phaải là số
            if (!sdt.matches("\\d+")) {
                Toast.makeText(this, "Số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            // sdt phai la 10 so
            if (!sdt.matches("\\d{10}")) {
                Toast.makeText(this, "Số điện thoại không hợp lệ! Số điện thoại phải có đúng 10 chữ số.", Toast.LENGTH_SHORT).show();
                return;
            }


            if (spinnerPhuongthuc.getSelectedItemPosition() == 0) {
                // Nếu chọn "Thanh toán khi nhận hàng"
                handleCashOnDelivery(dialog);
            } else {
                // Nếu chọn "Thanh toán MoMo"
                requestPaymentMoMo(dialog);
            }
        });


        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void handleCashOnDelivery(Dialog dialog) {
        HashMap<String, Object> orderData = new HashMap<>();
        orderData.put("hoten", hoten);
        orderData.put("diachi", diachi);
        orderData.put("sdt", sdt);
        orderData.put("ghichu", ghichu);
        orderData.put("phuongthuc", "Thanh toán khi nhận hàng");
        orderData.put("tongtien", tienthanhtoan);
        orderData.put("ngaydat", ngaydat);
        orderData.put("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        orderData.put("trangthai", 1);

        db.collection("HoaDon")
                .add(orderData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String orderId = task.getResult().getId();

                        for (Product product : listGiohang) {
                            for (SizeQuantity size : product.getSizes()) {
                                HashMap<String, Object> orderDetail = new HashMap<>();
                                orderDetail.put("id_hoadon", orderId);
                                orderDetail.put("id_product", product.getIdsp());
                                orderDetail.put("size", size.getSize());
                                orderDetail.put("soluong", size.getSoluong());
                                db.collection("ChitietHoaDon").add(orderDetail);
                            }
                        }

                        clearCart();

                        Intent intent = new Intent(CartActivity.this, OrderSuccessActivity.class);
                        intent.putExtra("idhoadon", orderId);
                        intent.putExtra("ngaydat", ngaydat);
                        intent.putExtra("hoten", hoten);
                        intent.putExtra("diachi", diachi);
                        intent.putExtra("sdt", sdt);
                        intent.putExtra("phuongthuc", "Thanh toán khi nhận hàng");
                        intent.putExtra("ghichu", ghichu);
                        intent.putExtra("tienthanhtoan", tienthanhtoan);
                        intent.putExtra("sanpham", listGiohang);
                        startActivity(intent);

                        dialog.dismiss();
                        finish();
                    } else {
                        Toast.makeText(this, "Đặt hàng thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void requestPaymentMoMo(Dialog dialog) {
        AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT); // Chuyển sang môi trường phát triển, Môi trường sản xuất (PRODUCTION):
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);                          //Thực hiện giao dịch thật
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);

        HashMap<String, Object> eventValue = new HashMap<>();
        long currentTimestamp = System.currentTimeMillis();

        try {
            // Lấy số tiền từ chuỗi và chuyển sang số
            Number number = NumberFormat.getInstance().parse(tienthanhtoan.replace(",", ""));
            eventValue.put("amount", number.intValue()); // Số tiền thanh toán
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thông tin yêu cầu từ phía merchant (bắt buộc)
        eventValue.put("merchantname", "MD06 Clothes"); // Tên merchant
        eventValue.put("merchantcode", "MOMOBKUN20180529"); // Mã merchant MoMo
        eventValue.put("orderId", "order" + currentTimestamp); // Mã đơn hàng duy nhất
        eventValue.put("orderLabel", "Mã đơn hàng");

        // Các trường tùy chọn
        eventValue.put("merchantnamelabel", "MD06_ClothesStore"); // Nhãn merchant
        eventValue.put("fee", 0); // Phí thanh toán
        eventValue.put("description", "Thanh toán đơn hàng tại MD06 Clothes");

        // Dữ liệu bổ sung (extraData)
        JSONObject objExtraData = new JSONObject();
        try {
            objExtraData.put("site_code", "008");
            objExtraData.put("site_name", "Thanh Toán Clothes");
            objExtraData.put("screen_code", 0);
            objExtraData.put("screen_name", "Đặc Biệt");

            // Gửi thông tin sản phẩm trong giỏ hàng
            StringBuilder productNames = new StringBuilder();
            for (Product product : listGiohang) {
                productNames.append(product.getTensp()).append(",");
            }
            objExtraData.put("movie_name", productNames.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        eventValue.put("extraData", objExtraData.toString());

        // Gọi hàm thanh toán MoMo
        AppMoMoLib.getInstance().requestMoMoCallBack(CartActivity.this, eventValue);

        dialog.dismiss(); // Đóng dialog
    }




    private void clearCart() {
        db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("ALL").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .collection("ALL").document(doc.getId()).delete();
                    }
                    listGiohang.clear();
                    giohangAdapter.notifyDataSetChanged();
                    UpdateCartSummary();
                    CheckCartVisibility();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == RESULT_OK) {
            if (data != null) {
                int status = data.getIntExtra("status", -1);
                String message = data.getStringExtra("message");

                if (status == 0) { // Thanh toán thành công
                    Toast.makeText(this, "Thanh toán MoMo thành công!", Toast.LENGTH_SHORT).show();
                    requestPaymentMoMoSuccess(); // Gọi phương thức để lưu hóa đơn
                } else {
                    Toast.makeText(this, "Thanh toán thất bại: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    private void requestPaymentMoMoSuccess() {
        HashMap<String, Object> orderData = new HashMap<>();
        orderData.put("hoten", hoten);
        orderData.put("diachi", diachi);
        orderData.put("sdt", sdt);
        orderData.put("ghichu", ghichu);
        orderData.put("phuongthuc", "Thanh toán MoMo");
        orderData.put("tongtien", tienthanhtoan);
        orderData.put("ngaydat", ngaydat);
        orderData.put("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        orderData.put("trangthai", 1); // Trạng thái đơn hàng

        // Lưu hóa đơn vào Firestore
        db.collection("HoaDon")
                .add(orderData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String orderId = task.getResult().getId();
                        // Lưu chi tiết sản phẩm
                        for (Product product : listGiohang) {
                            // Lưu thông tin kích thước và số lượng
                            HashMap<String, Object> orderDetail = new HashMap<>();
                            orderDetail.put("id_hoadon", orderId);
                            orderDetail.put("id_product", product.getIdsp());
                            orderDetail.put("sizes", product.getSizes());
                            // Lưu chi tiết sản phẩm vào Firestore
                            db.collection("ChitietHoaDon").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .collection("ALL").add(orderDetail);
                        }
                        // Xóa giỏ hàng sau khi lưu hóa đơn thành công
                        clearCart();

                        Intent intent = new Intent(CartActivity.this, OrderSuccessActivity.class);
                        intent.putExtra("idhoadon", orderId);
                        intent.putExtra("ngaydat", ngaydat);
                        intent.putExtra("hoten", hoten);
                        intent.putExtra("diachi", diachi);
                        intent.putExtra("sdt", sdt);
                        intent.putExtra("phuongthuc", "Thanh toán MoMo");
                        intent.putExtra("ghichu", ghichu);
                        intent.putExtra("tienthanhtoan", tienthanhtoan);
                        intent.putExtra("sanpham", listGiohang);
                        startActivity(intent);

                        finish(); // Kết thúc CartActivity
                    } else {
                        Toast.makeText(this, "Đặt hàng thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }






    // phí ship là 1000 vnd
    public void UpdateCartSummary() {
        int total = 0;
        int shippingFee = 1000;

        for (Product product : listGiohang) {
            for (SizeQuantity size : product.getSizes()) {
                total += product.getGiatien() * size.getSoluong();
            }
        }
        tvDongia.setText(NumberFormat.getInstance().format(total));
        tvPhiVanChuyen.setText(NumberFormat.getInstance().format(shippingFee));
        tvTongTien.setText(NumberFormat.getInstance().format(total + shippingFee));
    }


    private void CheckCartVisibility() {
        // Kiểm tra nếu danh sách giỏ hàng trống
        if (listGiohang.isEmpty()) {
            scrollViewCart.setVisibility(View.GONE);
            tvNullCart.setVisibility(View.VISIBLE);
            // Hiển thị nút quay lại khi giỏ hàng trống
            if (imgBackEmptyCart != null) imgBackEmptyCart.setVisibility(View.VISIBLE);
            if (imgBackCart != null) imgBackCart.setVisibility(View.GONE);
        } else {
            scrollViewCart.setVisibility(View.VISIBLE);
            tvNullCart.setVisibility(View.GONE);
            // Hiển thị nút quay lại khi giỏ hàng có sp
            if (imgBackEmptyCart != null) imgBackEmptyCart.setVisibility(View.GONE);
            if (imgBackCart != null) imgBackCart.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void OnSucess() {
        Toast.makeText(this, "Thành công", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnFail() {
        Toast.makeText(this, "Thất bại", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void getDataSanPham(String id, String id_product, String tensp, Long giatien, String hinhanh, String loaisp, String mota, List<SizeQuantity> sizes, Long type, String chatlieu) {
        if (sizes != null && !sizes.isEmpty()) {
            for (SizeQuantity size : sizes) {
                Product product = new Product(
                        id,
                        id_product,
                        tensp,
                        giatien,
                        hinhanh,
                        loaisp,
                        mota,
                        new ArrayList<>(List.of(size)),
                        type,
                        chatlieu
                );
                listGiohang.add(product);
            }
        }
        giohangAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
        UpdateCartSummary(); // Cập nhật tổng tiền
        CheckCartVisibility(); // Kiểm tra trạng thái giỏ hàng
    }

}
