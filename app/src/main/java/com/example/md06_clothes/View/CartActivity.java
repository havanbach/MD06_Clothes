package com.example.md06_clothes.View;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.momo.momo_partner.AppMoMoLib;

public class CartActivity extends AppCompatActivity implements GioHangView {

    private ScrollView scrollViewCart;
    private TextView tvNullCart, tvDongia, tvPhiVanChuyen, tvTongTien, btnThanhToan;
    private ImageView imgBackCart, imgback;
    private RecyclerView rcvGioHang;
    Number number;

    private GiohangAdapter giohangAdapter;
    private GioHangPresenter gioHangPresenter;
    private ArrayList<Product> listGiohang;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String[] paymentMethods = {"Thanh toán khi nhận hàng","Thanh toán MOMO"};
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
        imgBackCart = findViewById(R.id.img_back_empty_cart);
        imgback = findViewById(R.id.img_back_cart);
        listGiohang = new ArrayList<>();
        gioHangPresenter = new GioHangPresenter(this);
        imgBackCart.setOnClickListener(v -> finish());
        imgback.setOnClickListener(v -> finish());
    }

    private void SetupRecyclerView() {
        giohangAdapter = new GiohangAdapter(this, listGiohang, this);
        rcvGioHang.setLayoutManager(new LinearLayoutManager(this));
        rcvGioHang.setAdapter(giohangAdapter);

        // Swipe to delete functionality
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
                validateCartBeforeCheckout();
            }
        });
    }


    // Hàm kiểm tra tồn kho
    private void validateCartBeforeCheckout() {
        boolean[] isValid = {true}; // Sử dụng mảng để cập nhật giá trị trong callback Firebase

        for (Product product : listGiohang) {
            for (SizeQuantity sizeQuantity : product.getSizes()) {
                String productId = product.getIdsp(); // ID sản phẩm
                String size = sizeQuantity.getSize(); // Kích thước
                int requestedQuantity = sizeQuantity.getSoluong(); // Số lượng yêu cầu

                // Lấy tồn kho từ Firestore
                db.collection("SanPham").document(productId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                List<HashMap<String, Object>> stockSizes =
                                        (List<HashMap<String, Object>>) documentSnapshot.get("sizes");

                                boolean sizeFound = false;
                                for (HashMap<String, Object> stock : stockSizes) {
                                    if (stock.get("size").equals(size)) {
                                        int availableStock = ((Long) stock.get("soluong")).intValue();

                                        // Kiểm tra nếu số lượng yêu cầu lớn hơn tồn kho
                                        if (requestedQuantity > availableStock) {
                                            Toast.makeText(
                                                    this,
                                                    "Sản phẩm " + product.getTensp() + " (Size: " + size + ") chỉ còn " + availableStock + " sản phẩm!",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                            isValid[0] = false;
                                        }
                                        sizeFound = true;
                                        break;
                                    }
                                }

                                if (!sizeFound) {
                                    Toast.makeText(
                                            this,
                                            "Sản phẩm " + product.getTensp() + " không có kích thước " + size + " trong kho!",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    isValid[0] = false;
                                }
                            } else {
                                Toast.makeText(
                                        this,
                                        "Không tìm thấy sản phẩm " + product.getTensp() + " trong kho!",
                                        Toast.LENGTH_SHORT
                                ).show();
                                isValid[0] = false;
                            }

                            // Nếu tất cả kiểm tra đều hợp lệ, chuyển đến màn hình thanh toán
                            if (isValid[0]) {
                                ShowPaymentDialog();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi khi kiểm tra tồn kho!", Toast.LENGTH_SHORT).show();
                            isValid[0] = false;
                        });
            }
        }
    }


    private void LoadCartData() {
        gioHangPresenter.HandlegetDataGioHang();
    }

    private void ShowDeleteConfirmationDialog(int pos) {
        new AlertDialog.Builder(this)
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

        // Populate user info from Firestore
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, paymentMethods);
        spinnerPhuongthuc.setAdapter(adapter);

        btnxacnhan.setOnClickListener(v -> {
            hoten = edthoten.getText().toString().trim();
            diachi = edtdiachi.getText().toString().trim();
            sdt = edtsdt.getText().toString().trim();
            ghichu = edtghichu.getText().toString().trim();
            phuongthuc = spinnerPhuongthuc.getSelectedItem().toString();

            tienthanhtoan = tvtongtien.getText().toString();

            if (hoten.isEmpty() || diachi.isEmpty() || sdt.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate sizes and quantities in the cart
            for (Product product : listGiohang) {
                for (SizeQuantity size : product.getSizes()) {
                    if (size.getSoluong() <= 0) {
                        Toast.makeText(this, "Số lượng không hợp lệ cho sản phẩm: " + product.getTensp(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            ProcessPayment(dialog, edthoten, edtdiachi, edtsdt, edtghichu, spinnerPhuongthuc, tvtongtien);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }


    private void ProcessPayment(Dialog dialog, EditText edthoten, EditText edtdiachi, EditText edtsdt, EditText edtghichu, Spinner spinnerPhuongthuc, TextView tvtongtien) {
        hoten = edthoten.getText().toString().trim();
        diachi = edtdiachi.getText().toString().trim();
        sdt = edtsdt.getText().toString().trim();
        ghichu = edtghichu.getText().toString().trim();
        phuongthuc = spinnerPhuongthuc.getSelectedItem().toString();
        tienthanhtoan = tvtongtien.getText().toString();

        if (hoten.isEmpty() || diachi.isEmpty() || sdt.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        ngaydat = simpleDateFormat.format(calendar.getTime());

        HashMap<String, Object> orderData = new HashMap<>();
        orderData.put("hoten", hoten);
        orderData.put("diachi", diachi);
        orderData.put("sdt", sdt);
        orderData.put("ghichu", ghichu);
        orderData.put("phuongthuc", phuongthuc);
        orderData.put("tongtien", tienthanhtoan);
        orderData.put("ngaydat", ngaydat);
        orderData.put("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        orderData.put("trangthai", 1);

        db.collection("HoaDon")
                .add(orderData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String orderId = task.getResult().getId();

                        // Xử lý chi tiết hóa đơn
                        for (Product product : listGiohang) {
                            HashMap<String, Object> orderDetail = new HashMap<>();
                            orderDetail.put("id_hoadon", orderId);
                            orderDetail.put("id_product", product.getIdsp());
                            orderDetail.put("sizes", product.getSizes());

                            // Lưu chi tiết hóa đơn
                            db.collection("ChitietHoaDon").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .collection("ALL").add(orderDetail)
                                    .addOnCompleteListener(orderTask -> {
                                        if (orderTask.isSuccessful()) {
                                            // Trừ số lượng sản phẩm
                                            updateProductStock(product.getIdsp(), product.getSizes());
                                        } else {
                                            Toast.makeText(this, "Lỗi khi lưu chi tiết hóa đơn!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        ClearCart();

                        // Chuyển sang màn hình thanh toán thành công
                        Intent intent = new Intent(CartActivity.this, OrderSuccessActivity.class);
                        intent.putExtra("idhoadon", orderId);
                        intent.putExtra("hoten", hoten);
                        intent.putExtra("diachi", diachi);
                        intent.putExtra("sdt", sdt);
                        intent.putExtra("ghichu", ghichu);
                        intent.putExtra("ngaydat", ngaydat);
                        intent.putExtra("phuongthuc", phuongthuc);
                        intent.putExtra("tienthanhtoan", tienthanhtoan);
                        intent.putExtra("sanpham", listGiohang);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Đặt hàng thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //thanh toán momo
    private void requestPayment() {

        AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT);
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.MAP);
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);
        Map<String, Object> eventValue = new HashMap<>();
        //client Required
        long mahd =   System.currentTimeMillis();
        try {
            number = NumberFormat.getInstance().parse(tienthanhtoan);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        eventValue.put("merchantname", "Afforda Company - Nguyen Van Chinh"); //Tên đối tác. được đăng ký tại   . VD: Google, Apple, Tiki , CGV Cinemas
        eventValue.put("merchantcode", "MOMO1NRV20220112"); //Mã đối tác, được cung cấp bởi MoMo tại https://business.momo.vn
        eventValue.put("amount", Integer.parseInt(String.valueOf(number))); //Kiểu integer
        eventValue.put("orderId", "order"+mahd); //uniqueue id cho Bill order, giá trị duy nhất cho mỗi đơn hàng
        eventValue.put("orderLabel", "Mã đơn hàng"); //gán nhãn

        //client Optional - bill info
        eventValue.put("merchantnamelabel", "Dịch vụ");//gán nhãn

        eventValue.put("fee", Integer.parseInt(String.valueOf(number))); //Kiểu integer
        eventValue.put("description", "Mô tả"); //mô tả đơn hàng - short description

        //client extra data
        eventValue.put("requestId",  "MOMO1NRV20220112"+"merchant_billId_"+System.currentTimeMillis());
        eventValue.put("partnerCode", "MOMO1NRV20220112");
        Log.d("end", "end1");
        //Example extra data
        JSONObject objExtraData = new JSONObject();
        try {
            objExtraData.put("site_code", "008");
            objExtraData.put("site_name", "Thanh Toán Food");
            objExtraData.put("screen_code", 0);
            objExtraData.put("screen_name", "Đặc Biệt");
            String name ="";
            for(Product sanPham : listGiohang){
                name+=sanPham.getTensp()+",";

            }
            objExtraData.put("movie_name", name);
            objExtraData.put("movie_format", "Đồ ăn");
            Log.d("end", "end2");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("end", "Lỗi: " + e);
        }
        eventValue.put("extraData", objExtraData.toString());
        eventValue.put("extra", "");
        Log.d("end", "end3");
        AppMoMoLib.getInstance().requestMoMoCallBack(CartActivity.this, eventValue);
        Log.d("end", "end4");

    }

    // Hàm trừ số lượng sản phẩm trong Firestore
    private void updateProductStock(String productId, List<SizeQuantity> sizeQuantities) {
        db.collection("SanPham").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<HashMap<String, Object>> stockSizes =
                                (List<HashMap<String, Object>>) documentSnapshot.get("sizes");

                        if (stockSizes != null) {
                            // Duyệt qua danh sách size và cập nhật số lượng
                            for (SizeQuantity size : sizeQuantities) {
                                for (HashMap<String, Object> stock : stockSizes) {
                                    if (stock.get("size").equals(size.getSize())) {
                                        int availableStock = ((Long) stock.get("soluong")).intValue();
                                        int requestedQuantity = size.getSoluong();

                                        if (availableStock < requestedQuantity) {
                                            Log.e("UpdateStock", "Sản phẩm không đủ tồn kho: " + size.getSize());
                                            Toast.makeText(this, "Sản phẩm " + size.getSize() + " không đủ số lượng!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        // Trừ số lượng trong kho
                                        stock.put("soluong", availableStock - requestedQuantity);
                                    }
                                }
                            }

                            // Cập nhật lại tồn kho một lần
                            db.collection("SanPham").document(productId)
                                    .update("sizes", stockSizes)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("UpdateStock", "Cập nhật tồn kho thành công!");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("UpdateStock", "Lỗi khi cập nhật tồn kho!", e);
                                    });
                        } else {
                            Log.e("UpdateStock", "Danh sách size bị null!");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UpdateStock", "Lỗi khi lấy dữ liệu sản phẩm!", e);
                });
    }




    private void ClearCart() {
        db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("ALL").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .collection("ALL").document(doc.getId()).delete();
                    }
                    listGiohang.clear();
                    giohangAdapter.notifyDataSetChanged();
                    UpdateCartSummary();
                    CheckCartVisibility();
                });
    }

    public void UpdateCartSummary() {
        int total = 0;
        int shippingFee = 10000;
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
        if (listGiohang.isEmpty()) {
            scrollViewCart.setVisibility(View.GONE);
            tvNullCart.setVisibility(View.VISIBLE);
        } else {
            scrollViewCart.setVisibility(View.VISIBLE);
            tvNullCart.setVisibility(View.GONE);
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
                // Tạo bản sao sản phẩm cho từng size
                Product product = new Product(
                        id,
                        id_product,
                        tensp,
                        giatien,
                        hinhanh,
                        loaisp,
                        mota,
                        new ArrayList<>(List.of(size)), // Tách riêng size hiện tại
                        type,
                        chatlieu
                );

                // Thêm vào danh sách giỏ hàng
                listGiohang.add(product);
            }
        }

        // Cập nhật giao diện giỏ hàng
        giohangAdapter.notifyDataSetChanged();
        UpdateCartSummary();
        CheckCartVisibility();
    }

}
