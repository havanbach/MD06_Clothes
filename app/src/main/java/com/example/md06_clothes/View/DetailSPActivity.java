package com.example.md06_clothes.View;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md06_clothes.Adapter.BinhLuanAdapter;
import com.example.md06_clothes.Adapter.SizeAdapter;
import com.example.md06_clothes.Models.Binhluan;
import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.Models.SizeQuantity;
import com.example.md06_clothes.Presenter.BinhLuanPresenter;
import com.example.md06_clothes.Presenter.GioHangPresenter;
import com.example.md06_clothes.R;
import com.example.md06_clothes.my_interface.BinhLuanView;
import com.example.md06_clothes.my_interface.GioHangView;
import com.example.md06_clothes.my_interface.IClickCTHD;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailSPActivity extends AppCompatActivity implements GioHangView, BinhLuanView {

    private AppCompatToggleButton toggleButtonFavorite;
    private FloatingActionButton btnAddCartDetail;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private TextView tvGiaDetail, tvSizesDetail, tvChatlieuDetail, tvMoTaDetail;
    private ImageView imgDetail;
    private RecyclerView rcvSizes;
    private LinearLayout linearShowAllCmt;
    private BinhLuanPresenter binhLuanPresenter;
    private ArrayList<Binhluan> mListBinhluan;
    private BinhLuanAdapter adapter;
    private RecyclerView rcvBinhluan;
    private Product product;
    private BottomSheetDialog bottomSheetDialog;
    SizeQuantity sizeQuantity;
    private GioHangPresenter gioHangPresenter;
    private SizeAdapter sizeAdapter;
    private List<SizeQuantity> sizes = new ArrayList<>();
    private int selectedSizeIndex = -1; // Chỉ số size được chọn
    private TextView tvTenBottom, tvGiaBottom, tvSoluongBottom, tvMotaBottom;
    private ImageView imgBottom, btnMinusBottom, btnPlusBottom;
    private Button btnBottom;
    private int slBottom = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_spactivity);
        sizeQuantity = new SizeQuantity();
        InitWidget();
        Init();
        Event();
    }

    private void Event() {
        linearShowAllCmt.setOnClickListener(view -> {
            Intent intent = new Intent(DetailSPActivity.this, CommentActivity.class);
            intent.putExtra("allcmt", product);
            startActivity(intent);
        });

        btnAddCartDetail.setOnClickListener(view -> {
            if (sizeQuantity.getSize() == null) {
                Toast.makeText(this, "Vui lòng chọn size!", Toast.LENGTH_SHORT).show();
                return;
            }
            setBottomSheetDialog();
            tvTenBottom.setText(product.getTensp());
            tvGiaBottom.setText(NumberFormat.getInstance().format(product.getGiatien()) + "");
            tvMotaBottom.setText(product.getMota());
            Picasso.get().load(product.getHinhanh()).into(imgBottom);
            initBottomSheet();
            bottomSheetDialog.show();
        });
    }

    private void setBottomSheetDialog() {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.layout_persistent_bottom_sheet);
        tvTenBottom = bottomSheetDialog.findViewById(R.id.tv_ten_bottom);
        imgBottom = bottomSheetDialog.findViewById(R.id.img_bottom);
        tvGiaBottom = bottomSheetDialog.findViewById(R.id.tv_gia_bottom);
        btnMinusBottom = bottomSheetDialog.findViewById(R.id.btn_minus_bottom);
        btnPlusBottom = bottomSheetDialog.findViewById(R.id.btn_plus_bottom);
        tvSoluongBottom = bottomSheetDialog.findViewById(R.id.tv_soluong_bottom);
        tvMotaBottom = bottomSheetDialog.findViewById(R.id.tv_mota_bottom);
        btnBottom = bottomSheetDialog.findViewById(R.id.btn_bottom);
    }

    public void initBottomSheet() {
        btnMinusBottom.setVisibility(View.GONE);
        btnMinusBottom.setOnClickListener(view -> {
            slBottom = Integer.parseInt(tvSoluongBottom.getText().toString()) - 1;
            tvSoluongBottom.setText(String.valueOf(slBottom));
            btnMinusBottom.setVisibility(slBottom < 2 ? View.GONE : View.VISIBLE);
        });

        btnPlusBottom.setOnClickListener(view -> {
            slBottom = Integer.parseInt(tvSoluongBottom.getText().toString()) + 1;
            tvSoluongBottom.setText(String.valueOf(slBottom));
            btnMinusBottom.setVisibility(slBottom < 2 ? View.GONE : View.VISIBLE);
        });

        btnBottom.setOnClickListener(view -> {
            int selectedQuantity = Integer.parseInt(tvSoluongBottom.getText().toString());
            if (sizeQuantity.getSoluong() < selectedQuantity) {
                Toast.makeText(DetailSPActivity.this, "Số lượng sản phẩm không đủ!", Toast.LENGTH_SHORT).show();
            } else {
                gioHangPresenter.AddCart(product.getId(), sizeQuantity.getSize(), (long) selectedQuantity);
                bottomSheetDialog.dismiss();
                Toast.makeText(DetailSPActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void Init() {
        Intent intent = getIntent();
        product = (Product) intent.getSerializableExtra("search");
        boolean isFromCart = intent.getBooleanExtra("from_cart", false);
        if (isFromCart) {
            getProductById(product.getIdsp());
        } else {
            getProductById(product.getId());
        }
    }

    private void InitWidget() {
        gioHangPresenter = new GioHangPresenter(this);
        toggleButtonFavorite = findViewById(R.id.toogle_btn_favorite);
        btnAddCartDetail = findViewById(R.id.btn_addcart_detail);
        tvGiaDetail = findViewById(R.id.tv_gia_detail);
        tvChatlieuDetail = findViewById(R.id.tv_chatlieu_detail);
        tvMoTaDetail = findViewById(R.id.tv_mota_detail);
        imgDetail = findViewById(R.id.img_detail);
        rcvSizes = findViewById(R.id.rcv_sizes); // RecyclerView hiển thị danh sách size
        linearShowAllCmt = findViewById(R.id.linear_show_all_cmt);
        rcvBinhluan = findViewById(R.id.rcv_binhluan);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        mListBinhluan = new ArrayList<>();

        ImageView btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void getDataBinhLuan(String idbinhluan, String idproduct, String iduser, String rate, String noidung) {
        mListBinhluan.add(new Binhluan(idbinhluan, idproduct, iduser, rate, noidung));
        adapter = new BinhLuanAdapter(DetailSPActivity.this, mListBinhluan, new IClickCTHD() {
            @Override
            public void onClickCTHD(int pos) {

            }
        });

        rcvBinhluan.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rcvBinhluan.setAdapter(adapter);
    }

    @Override
    public void OnSucess() {
        Toast.makeText(this, "Thêm giỏ hàng thành công", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnFail() {
        Toast.makeText(this, "Thất bại khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void getDataSanPham(String id, String id_product, String tensp, Long giatien, String hinhanh, String loaisp, String mota, List<SizeQuantity> sizes, Long type, String chatlieu) {
    }

    public void onDefaultToggleClick(View view) {
        boolean ToggleButtonState = toggleButtonFavorite.isChecked();
        if (ToggleButtonState) {
            HashMap<String, String> map = new HashMap<>();
            map.put("idproduct", product.getId());
            map.put("iduser", FirebaseAuth.getInstance().getCurrentUser().getUid());
            db.collection("Favorite").add(map).addOnSuccessListener(documentReference -> {});
        } else {
            db.collection("Favorite").whereEqualTo("idproduct", product.getId())
                    .whereEqualTo("iduser", FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                            db.collection("Favorite").document(q.getId()).delete();
                        }
                    });
        }
    }

    public void getProductById(String productId) {
        db.collection("SanPham").document(productId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        product = document.toObject(Product.class);
                        updateUIWithProduct(product);
                        Log.e("DetailSPActivity", "tìm thấy sản phẩm với ID: " + productId);
                    } else {
                        Log.e("DetailSPActivity", "Không tìm thấy sản phẩm với ID: " + productId);
                    }
                })
                .addOnFailureListener(e -> Log.e("DetailSPActivity", "Lỗi khi lấy sản phẩm: " + e.getMessage()));
    }

    private void updateUIWithProduct(Product product) {
        if (product != null) {
            db.collection("Favorite").whereEqualTo("iduser", FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                            if (q.getString("idproduct").equals(product.getId())) {
                                toggleButtonFavorite.setChecked(true);
                            }
                        }
                    });

            collapsingToolbarLayout.setTitle(product.getTensp());
            tvGiaDetail.setText(NumberFormat.getInstance().format(product.getGiatien()));
            tvChatlieuDetail.setText(product.getChatlieu());
            tvMoTaDetail.setText(product.getMota());
            Picasso.get().load(product.getHinhanh()).into(imgDetail);
            sizes = product.getSizes();
            sizeAdapter = new SizeAdapter(sizes, position -> {
                sizeQuantity = sizes.get(position);
                Log.d("SIZE", " - Size: " + sizeQuantity.getSize() + " - Số lượng: " + sizeQuantity.getSoluong());
            });
            rcvSizes.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
            rcvSizes.setAdapter(sizeAdapter);
            binhLuanPresenter = new BinhLuanPresenter(this);
            binhLuanPresenter.HandleGetBinhLuanLimit(product.getId());
        }
    }
}
