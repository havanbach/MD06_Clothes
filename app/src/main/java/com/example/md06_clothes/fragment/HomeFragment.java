package com.example.md06_clothes.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;


import com.example.md06_clothes.Adapter.BannerAdapter;
import com.example.md06_clothes.Adapter.LoaiProductAdapter;
import com.example.md06_clothes.Adapter.ProductAdapter;
import com.example.md06_clothes.Models.LoaiProduct;
import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.Models.SizeQuantity;
import com.example.md06_clothes.R;
import com.example.md06_clothes.View.CartActivity;
import com.example.md06_clothes.View.CategoryActivity;

import com.example.md06_clothes.View.DetailSPActivity;
import com.example.md06_clothes.View.SearchActivity;
import com.example.md06_clothes.my_interface.IClickCTHD;
import com.example.md06_clothes.my_interface.IClickLoaiProduct;
import com.example.md06_clothes.my_interface.IClickOpenBottomSheet;
import com.example.md06_clothes.ultil.NetworkUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.relex.circleindicator.CircleIndicator;
public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeHome;
    private Product product;

    // Data Product
    private ArrayList<Product> arr_ds_sp, arr_sp_nb, arr_sp_du, arr_sp_hq, arr_sp_mc, arr_sp_yt, arr_sp_lau, arr_sp_gy;
    private ProductAdapter productDSAdapter, productNBAdapter, productDUAdapter, productHQAdapter, productMCAdapter, productYTAdapter, productLauAdapter, productGYAdapter;
    private RecyclerView rcvDSSP, rcvSPNoiBat, rcvSPDoUong, rcvSPHQ, rcvSPMC, rcvSPYT, rcvSPLau, rcvSPGY;

    // Banner
    private ArrayList<String> arrayList;
    private ViewPager viewPager;
    private CircleIndicator circleIndicator;
    private BannerAdapter bannerAdapter;

    // Infor User
    private Toolbar toolbarHome;
    private CircleImageView cirAvatarHome;
    private TextView tvNameHome, tvEmailHome, tvNumberCart;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String userId;

    // Loai Product
    private RecyclerView rcvLoaiProduct;
    private LoaiProductAdapter loaiProductAdapter;
    private List<LoaiProduct> mlistproduct;

    // Search
    private EditText edtSearchHome;
    private ImageView imgHomeCart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        InitWidget(view);
        InitProduct();

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        listenToCartChanges();
//        loadCartCount();

        if (NetworkUtil.isNetworkConnected(getContext())) {
            LoadInfor();
            LoadBanner();
            LoadProductData();
            LoadFavorite();
        }

        SetupEventListeners();
        return view;
    }

    private void InitWidget(View view) {
        swipeHome = view.findViewById(R.id.swipe_home);
        tvNumberCart = view.findViewById(R.id.tv_number_cart);
        edtSearchHome = view.findViewById(R.id.edt_search_home);
        imgHomeCart = view.findViewById(R.id.img_home_cart);
        toolbarHome = view.findViewById(R.id.toolbar_home);
        cirAvatarHome = view.findViewById(R.id.cir_avatar_home);
        tvNameHome = view.findViewById(R.id.tv_name_home);
        tvEmailHome = view.findViewById(R.id.tv_email_home);
        viewPager = view.findViewById(R.id.viewpager);
        circleIndicator = view.findViewById(R.id.circle_indicator);

        rcvLoaiProduct = view.findViewById(R.id.rcv_loai_product);
        loaiProductAdapter = new LoaiProductAdapter();
        rcvLoaiProduct.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        GetLoaiProducts();

        rcvDSSP = view.findViewById(R.id.rcv_ds_sanpham);
        rcvSPNoiBat = view.findViewById(R.id.rcv_sp_noibat);
        rcvSPDoUong = view.findViewById(R.id.rcv_sp_douong);
        rcvSPHQ = view.findViewById(R.id.rcv_sp_thoitrang1);
        rcvSPMC = view.findViewById(R.id.rcv_sp_micay);
        rcvSPYT = view.findViewById(R.id.rcv_sp_yeuthich);
        rcvSPLau = view.findViewById(R.id.rcv_sp_lau);
        rcvSPGY = view.findViewById(R.id.rcv_sp_goiy);
    }

    private void InitProduct() {
        arr_ds_sp = new ArrayList<>();
        arr_sp_nb = new ArrayList<>();
        arr_sp_du = new ArrayList<>();
        arr_sp_hq = new ArrayList<>();
        arr_sp_mc = new ArrayList<>();
        arr_sp_yt = new ArrayList<>();
        arr_sp_lau = new ArrayList<>();
        arr_sp_gy = new ArrayList<>();
    }

    private void SetupEventListeners() {
        edtSearchHome.setOnClickListener(v -> startActivity(new Intent(getActivity(), SearchActivity.class)));

        imgHomeCart.setOnClickListener(v -> startActivity(new Intent(getActivity(), CartActivity.class)));

        swipeHome.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> {
                swipeHome.setRefreshing(false);
                LoadFavorite();
                LoadProductData();
            }, 1000);
        });
    }


    private void LoadBanner() {
        arrayList = new ArrayList<>();
        firestore.collection("Banner")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot d : queryDocumentSnapshots) {
                        arrayList.add(d.getString("hinhanh"));
                    }
                    bannerAdapter = new BannerAdapter(getContext(), arrayList, pos -> {
                        startActivity(new Intent(getContext(), SearchActivity.class));
                    });
                    viewPager.setAdapter(bannerAdapter);
                    circleIndicator.setViewPager(viewPager);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int next = (viewPager.getCurrentItem() + 1) % arrayList.size();
                            viewPager.setCurrentItem(next, true);
                            new Handler().postDelayed(this, 3000);
                        }
                    }, 3000);
                });
    }

    private void LoadProductData() {
        LoadProducts(1, arr_ds_sp, rcvDSSP);
        LoadProducts(2, arr_sp_nb, rcvSPNoiBat);
        LoadProducts(3, arr_sp_du, rcvSPDoUong);
        LoadProducts(4, arr_sp_hq, rcvSPHQ);
        LoadProducts(5, arr_sp_mc, rcvSPMC);
        LoadProducts(6, arr_sp_yt, rcvSPYT);
        LoadProducts(7, arr_sp_lau, rcvSPLau);
        LoadProducts(8, arr_sp_gy, rcvSPGY);
    }

    private void LoadProducts(int type, ArrayList<Product> productList, RecyclerView recyclerView) {
        firestore.collection("SanPham")
                .whereEqualTo("type", type)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e("LoadProducts", "Lỗi khi lắng nghe dữ liệu sản phẩm!", error);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        // Xóa danh sách hiện tại để cập nhật lại
                        productList.clear();

                        for (QueryDocumentSnapshot d : queryDocumentSnapshots) {
                            List<SizeQuantity> sizes = new ArrayList<>();
                            List<Map<String, Object>> sizesData = (List<Map<String, Object>>) d.get("sizes");
                            if (sizesData != null) {
                                for (Map<String, Object> size : sizesData) {
                                    sizes.add(new SizeQuantity((String) size.get("size"), ((Long) size.get("soluong")).intValue()));
                                }
                            }

                            // Thêm sản phẩm vào danh sách
                            productList.add(new Product(d.getId(), d.getString("tensp"),
                                    d.getLong("giatien"), d.getString("hinhanh"),
                                    d.getString("loaisp"), d.getString("mota"),
                                    sizes, d.getLong("type"), d.getString("chatlieu")));
                        }

                        // Tạo và gán Adapter mới sau khi dữ liệu thay đổi
                        ProductAdapter productAdapter = new ProductAdapter(getContext(), productList, type, position -> {
                            Product selectedProduct = productList.get(position);
                            Intent intent = new Intent(getContext(), DetailSPActivity.class);
                            intent.putExtra("search", selectedProduct);
                            startActivity(intent);
                        });
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
                        recyclerView.setAdapter(productAdapter);
                    }
                });
    }



    private void LoadFavorite() {
        firestore.collection("Favorite")
                .document(userId)
                .collection("ALL")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalFavorite = queryDocumentSnapshots.size();

                });
    }

    private void listenToCartChanges() {
        firestore.collection("GioHang")
                .document(userId)
                .collection("ALL")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("CartListener", "Error listening to cart changes", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        int totalQuantity = 0;
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            List<Map<String, Object>> sizesData = (List<Map<String, Object>>) doc.get("sizes");
                            if (sizesData != null) {
                                for (Map<String, Object> size : sizesData) {
                                    Long quantity = (Long) size.get("soluong");
                                    if (quantity != null) {
                                        totalQuantity += quantity.intValue();
                                    }
                                }
                            }
                        }
                        tvNumberCart.setText(String.valueOf(totalQuantity));
                    }
                });
    }


    private void loadCartCount() {
        firestore.collection("GioHang")
                .document(userId)
                .collection("ALL")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalQuantity = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        List<Map<String, Object>> sizesData = (List<Map<String, Object>>) doc.get("sizes");
                        if (sizesData != null) {
                            for (Map<String, Object> size : sizesData) {
                                Long quantity = (Long) size.get("soluong");
                                if (quantity != null) {
                                    totalQuantity += quantity.intValue();
                                }
                            }
                        }
                    }
                    updateCartCount(totalQuantity);
                })
                .addOnFailureListener(e -> {
                    Log.e("CartCount", "Failed to load cart count", e);
                });
    }


    private void updateCartCount(int totalQuantity) {
        tvNumberCart.setVisibility(totalQuantity > 0 ? View.VISIBLE : View.GONE);
    }

    private void GetLoaiProducts() {
        firestore.collection("LoaiProduct")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mlistproduct = new ArrayList<>();
                    for (QueryDocumentSnapshot d : queryDocumentSnapshots) {
                        mlistproduct.add(new LoaiProduct(d.getString("tenloai"), d.getString("hinhanhloai")));
                    }
                    loaiProductAdapter.setData(mlistproduct, position -> {
                        Intent intent = new Intent(getContext(), CategoryActivity.class);
                        intent.putExtra("loaiproduct", mlistproduct.get(position).getTenloai());
                        startActivity(intent);
                    });
                    rcvLoaiProduct.setAdapter(loaiProductAdapter);
                });
    }
    public TextView getTvNameHome(){
        return tvNameHome;
    }
    public TextView getTvEmailHome(){
        return tvEmailHome;
    }
    public CircleImageView getCirAvatarHome(){
        return cirAvatarHome;
    }
    private void LoadInfor() {
        tvEmailHome.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        firestore.collection("User").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Profile")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()>0){
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            if(documentSnapshot!=null){
                                try{
                                    tvNameHome.setText(documentSnapshot.getString("hoten").length()>0 ?
                                            documentSnapshot.getString("hoten") : "");

                                    if(documentSnapshot.getString("avatar").length()>0){
                                        Picasso.get().load(documentSnapshot.getString("avatar").trim()).into(cirAvatarHome);
                                    }
                                }catch (Exception e){
                                    Log.d("ERROR",e.getMessage());
                                }
                            }
                        }
                    }
                });
    }
}
