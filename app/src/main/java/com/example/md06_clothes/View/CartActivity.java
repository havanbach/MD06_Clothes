package com.example.md06_clothes.View;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.md06_clothes.Adapter.GiohangAdapter;
import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.Presenter.GioHangPresenter;
import com.example.md06_clothes.R;
import com.example.md06_clothes.my_interface.GioHangView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import org.jetbrains.annotations.NotNull;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class CartActivity extends AppCompatActivity implements GioHangView {

    private ScrollView scrollViewCart;
    private TextView tvNullCart;
    private View view;

    private RecyclerView rcvGioHang;
    private GiohangAdapter giohangAdapter;
    private GioHangPresenter gioHangPresenter;
    public ArrayList<Product> listGiohang;
    private  TextView tvDongia, tvPhiVanChuyen, tvTongTien;
    private TextView btnThanhToan;
    private ImageView imgBackCart;

    private Intent intent;
    private Product product;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Hiển thị dialog thanh toán
    private Spinner spinnerPhuongthuc;
    private  String s[]={"Thanh toán khi nhận hàng"};
    private String tienthanhtoan = "";
    private String hoten, diachi, sdt, ghichu;
    private String ngaydat, phuongthuc;

    int total;
    //    Chat chat; Chưa fix
    Number number;

    String sanpham = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        InitWidget();
        DeleteDataGioHang();
        TongTienGioHang();
        Event();
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

        listGiohang = new ArrayList<>();
        gioHangPresenter = new GioHangPresenter(CartActivity.this);
        gioHangPresenter.HandlegetDataGioHang();

    }

    private void Event() {
        btnThanhToan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listGiohang.size()>0){
                    DiaLogThanhToan();
                }else{
                    Toast.makeText(CartActivity.this, "Giỏ hàng của bạn đang trống !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imgBackCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ImageView imgBackEmptyCart = findViewById(R.id.img_back_empty_cart);
        imgBackEmptyCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Quay lại màn hình trước
            }
        });

    }

    private void DiaLogThanhToan() {
        Dialog dialog = new Dialog(CartActivity.this);
        dialog.setContentView(R.layout.dialog_thanhtoan);
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        CustomInit(dialog);
    }
    private void CustomInit(Dialog dialog) {
        spinnerPhuongthuc = dialog.findViewById(R.id.spinner_phuongthuc);
        EditText edthoten = dialog.findViewById(R.id.edt_hoten);
        EditText edtdiachi = dialog.findViewById(R.id.edt_diachi);
        EditText edtsdt = dialog.findViewById(R.id.edt_sdt);
        EditText edtghichu = dialog.findViewById(R.id.edt_ghichu);
        TextView tvtongtien = dialog.findViewById(R.id.tv_tongtien);
        Button btnxacnhan = dialog.findViewById(R.id.btn_xacnhan);
        ImageView btnCancel = dialog.findViewById(R.id.btn_cancel);
        dialog.setCanceledOnTouchOutside(false);

        ArrayAdapter arrayAdapter = new ArrayAdapter(CartActivity.this,
                android.R.layout.simple_list_item_1, s);
        spinnerPhuongthuc.setAdapter(arrayAdapter);

        // Set info dialog
        tienthanhtoan = tvTongTien.getText().toString().trim();
        db.collection("User").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Profile").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()>0) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            if(documentSnapshot!=null){
                                edthoten.setText(documentSnapshot.getString("hoten"));
                                edtdiachi.setText(documentSnapshot.getString("diachi"));
                                edtsdt.setText(documentSnapshot.getString("sdt"));
                            }
                        }
                    }
                });
        tvtongtien.setText(tienthanhtoan);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        btnxacnhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Product product : listGiohang) {
                    sanpham += product.getTensp() + " x " + product.getSoluong() + "\n";
                }
                hoten = edthoten.getText().toString().trim();
                diachi = edtdiachi.getText().toString().trim();
                sdt = edtsdt.getText().toString().trim();
                ghichu = edtghichu.getText().toString().trim();
                if (hoten.length() > 0) {
                    if (diachi.length() > 0) {
                        if (sdt.length() > 0) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            Calendar calendar = Calendar.getInstance();
                            ngaydat = simpleDateFormat.format(calendar.getTime());
                            phuongthuc = spinnerPhuongthuc.getSelectedItem().toString();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("ghichu", ghichu);
                            hashMap.put("ngaydat", ngaydat);
                            hashMap.put("diachi", diachi);
                            hashMap.put("sdt", sdt);
                            hashMap.put("hoten", hoten);
                            hashMap.put("phuongthuc", phuongthuc);
                            hashMap.put("tongtien", tienthanhtoan);
                            hashMap.put("trangthai", 1);
                            hashMap.put("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            db.collection("HoaDon")
                                    .add(hashMap).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            String idhoadon = task.getResult().getId();
                                            for (Product product : listGiohang) {
                                                HashMap<String, Object> map_chitiet = new HashMap<>();
                                                map_chitiet.put("id_hoadon", idhoadon);
                                                map_chitiet.put("id_product", product.getIdsp());
                                                map_chitiet.put("soluong", product.getSoluong());
                                                db.collection("ChitietHoaDon").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                        .collection("ALL").add(map_chitiet);
                                            }
                                            // Cập nhật số lượng sản phẩm
                                            updateProductQuantities();
                                            // Xóa giỏ hàng và điều hướng
                                            db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .collection("ALL").get().addOnSuccessListener(queryDocumentSnapshots -> {
                                                        for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                                                            db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                    .collection("ALL").document(q.getId()).delete();
                                                        }
                                                        listGiohang.clear();
                                                        giohangAdapter.notifyDataSetChanged();
                                                        TongTienGioHang();
                                                        scrollViewCart.setVisibility(View.INVISIBLE);
                                                        tvNullCart.setVisibility(View.VISIBLE);
                                                        Intent intent = new Intent(CartActivity.this, OrderSuccessActivity.class);
                                                        intent.putExtra("idhoadon", idhoadon);
                                                        intent.putExtra("hoten", hoten);
                                                        intent.putExtra("diachi", diachi);
                                                        intent.putExtra("sdt", sdt);
                                                        intent.putExtra("ghichu", ghichu);
                                                        intent.putExtra("ngaydat", ngaydat);
                                                        intent.putExtra("phuongthuc", phuongthuc);
                                                        intent.putExtra("tienthanhtoan", tienthanhtoan);
                                                        intent.putExtra("sanpham", sanpham);
                                                        startActivity(intent);
                                                        finish();
                                                    });
                                        }
                                    });
                        } else {
                            Toast.makeText(CartActivity.this, "Số điện thoại không để trống", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CartActivity.this, "Địa chỉ không để trống", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CartActivity.this, "Họ tên không để trống", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateProductQuantities() {
        for (Product product : listGiohang) {
            String productId = product.getIdsp();
            long quantityPurchased = product.getSoluong();

            // Lấy thông tin sản phẩm từ Firestore
            db.collection("SanPham").document(productId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    long currentQuantity = documentSnapshot.getLong("soluong");
                    long updatedQuantity = currentQuantity - quantityPurchased;

                    // Cập nhật lại số lượng trong Firestore
                    db.collection("SanPham").document(productId).update("soluong", updatedQuantity)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("UpdateQuantity", "Số lượng sản phẩm " + productId + " đã được cập nhật.");
                                } else {
                                    Log.e("UpdateQuantity", "Lỗi khi cập nhật số lượng sản phẩm " + productId);
                                }
                            });
                }
            }).addOnFailureListener(e -> {
                Log.e("UpdateQuantity", "Không thể lấy thông tin sản phẩm: " + productId, e);
            });
        }
    }

    public  void TongTienGioHang() {
        int tongtien = 0;
        tvPhiVanChuyen.setText(String.valueOf(10000));
        int phi = Integer.parseInt(tvPhiVanChuyen.getText().toString());
        for (Product product: listGiohang){
            tongtien += product.getGiatien() * product.getSoluong();
        }
        tvDongia.setText(String.valueOf(tongtien));
        int dongia = Integer.parseInt(tvDongia.getText().toString());
        tvTongTien.setText(NumberFormat.getInstance().format(phi + dongia));
    }
    private void DeleteDataGioHang(){
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return true;
            }
            //chức năng xóa sp trong giỏ hàng
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                AlertDialog.Builder buidler = new AlertDialog.Builder(CartActivity.this);
                buidler.setMessage("Bạn có muốn xóa  sản phẩm " + listGiohang.get(pos).getTensp() + " không?");
                buidler.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gioHangPresenter.HandleDeleteDataGioHang(listGiohang.get(pos).getId());
                        listGiohang.remove(pos);
                        TongTienGioHang();
                        giohangAdapter.notifyDataSetChanged();
                        if (listGiohang.size() == 0){
                            scrollViewCart.setVisibility(View.INVISIBLE);
                            tvNullCart.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(CartActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    }
                });
                buidler.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        giohangAdapter.notifyDataSetChanged();
                    }
                });
                buidler.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rcvGioHang);
    }


    @Override
    public void OnSucess() {
    }

    @Override
    public void OnFail() {
    }

    @Override
    public void getDataSanPham(String id, String idsp, String tensp, Long giatien, String hinhanh, String loaisp, String mota, Long soluong, String size, Long type, String chatlieu) {
        try{
            listGiohang.add(new Product(id,idsp,tensp,giatien,hinhanh,loaisp,mota,soluong,size,type,chatlieu));
            product = new Product(id,idsp,tensp,giatien,hinhanh,loaisp,mota,soluong,size,type,chatlieu);
            Log.d("product", "Sản phẩm: " + product.getId() + product.getTensp() + product.getSoluong() + product.getGiatien());

            if (listGiohang.size() != 0){
                scrollViewCart.setVisibility(View.VISIBLE);
                tvNullCart.setVisibility(View.GONE);
            } else {
                scrollViewCart.setVisibility(View.GONE);
                tvNullCart.setVisibility(View.VISIBLE);
            }
            giohangAdapter = new GiohangAdapter(CartActivity.this,listGiohang, CartActivity.this);
            rcvGioHang.setLayoutManager(new LinearLayoutManager(CartActivity.this));
            rcvGioHang.setAdapter(giohangAdapter);
            TongTienGioHang();
        }catch (Exception e){

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("CHECKED","checked1");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Thực hiện các thao tác cập nhật hóa đơn, chi tiết hóa đơn
        HashMap<String, Object> hashMap2 = new HashMap<>();
        hashMap2.put("ghichu", ghichu);
        hashMap2.put("ngaydat", ngaydat);
        hashMap2.put("diachi", diachi);
        hashMap2.put("sdt", sdt);
        hashMap2.put("hoten", hoten);
        hashMap2.put("phuongthuc", phuongthuc);
        hashMap2.put("tongtien", tienthanhtoan);
        hashMap2.put("trangthai", 1); // Đã xử lý
        hashMap2.put("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());

        db.collection("HoaDon")
                .add(hashMap2).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            String idhoadon = task.getResult().getId();
                            for (Product sanPhamModels : listGiohang) {
                                HashMap<String, Object> map_chitiet = new HashMap<>();
                                map_chitiet.put("id_hoadon", task.getResult().getId());
                                map_chitiet.put("id_product", sanPhamModels.getIdsp());
                                map_chitiet.put("soluong", sanPhamModels.getSoluong());
                                db.collection("ChitietHoaDon").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("ALL").add(map_chitiet).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {
                                                    db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                            .collection("ALL").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                    for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                                                                        db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                                .collection("ALL").document(q.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            listGiohang.clear();
                                                                                            giohangAdapter.notifyDataSetChanged();
                                                                                            TongTienGioHang();
                                                                                            scrollViewCart.setVisibility(View.INVISIBLE);
                                                                                            tvNullCart.setVisibility(View.VISIBLE);
                                                                                            Log.d("idhoadon", "ID hóa đơn là: " + idhoadon);
                                                                                        } else {
                                                                                            Toast.makeText(CartActivity.this, "Thất bại", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });

                                                }

                                            }
                                        });

                            }
                            Intent intent = new Intent(CartActivity.this, OrderSuccessActivity.class);
                            intent.putExtra("idhoadon", idhoadon);
                            intent.putExtra("hoten", hoten);
                            intent.putExtra("diachi", diachi);
                            intent.putExtra("sdt", sdt);
                            intent.putExtra("ghichu", ghichu);
                            intent.putExtra("ngaydat", ngaydat);
                            intent.putExtra("phuongthuc", phuongthuc);
                            intent.putExtra("tienthanhtoan", tienthanhtoan);
                            intent.putExtra("sanpham", sanpham);
                            intent.putExtra("serialzable", listGiohang);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

}