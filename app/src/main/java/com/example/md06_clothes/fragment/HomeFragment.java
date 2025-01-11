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

import de.hdodenhof.circleimageview.CircleImageView;
import me.relex.circleindicator.CircleIndicator;

public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeHome;
    private Product product;


    // Data Product
    private  ArrayList<Product> arr_ds_sp,arr_sp_nb,arr_sp_du,arr_sp_hq,arr_sp_mc,arr_sp_yt,arr_sp_lau,arr_sp_gy;
    private ProductAdapter productDSAdapter,productNBAdapter,productDUAdapter,productHQAdapter,productMCAdapter,productYTAdapter,productLauAdapter,productGYAdapter;
    private RecyclerView rcvDSSP,rcvSPNoiBat,rcvSPDoUong,rcvSPHQ,rcvSPMC,rcvSPYT,rcvSPLau,rcvSPGY;

    // Banner
    private ArrayList<String> arrayList;
    private ViewPager viewPager;
    private CircleIndicator circleIndicator;
    private BannerAdapter bannerAdapter;

    // Infor User
    private Toolbar toolbarHome;
    private View view;
    private CircleImageView cirAvatarHome;
    private TextView tvNameHome, tvEmailHome;
    public static final int MY_REQUEST_CODE = 10;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Loai Product
    private RecyclerView rcvLoaiProduct;
    private LoaiProductAdapter loaiProductAdapter;
    private List<LoaiProduct> mlistproduct;

    //Search Data
    private EditText edtSearchHome;

    private ImageView imgHomeCart;

    private TextView tvNumberCart; // Hiển thị số lượng sản phẩm trong giỏ hàng
    private String userId; // ID của người dùng hiện tại



    private void LoadFavorite() {
        firestore.collection("Favorite").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("ALL")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() > 0){
                            int num = 0;
                            Log.d("numCart", "Number: " + queryDocumentSnapshots.size());
                            for (QueryDocumentSnapshot q : queryDocumentSnapshots){
                                num++;
                                tvNumberCart.setVisibility(View.VISIBLE);
                                tvNumberCart.setText(num+"");
                            }
                        } else {
                            tvNumberCart.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void replace(Fragment fragment){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NotNull MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_two);
        View view = MenuItemCompat.getActionView(menuItem);

        CircleImageView cirToolbarProfile = view.findViewById(R.id.cir_toolbar_profile);
        firestore.collection("User").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Profile")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()>0){
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            if(documentSnapshot!=null){
                                try{
                                    if(documentSnapshot.getString("avatar").length()>0){
                                        Picasso.get().load(documentSnapshot.getString("avatar").trim()).into(cirToolbarProfile);
                                    }
                                }catch (Exception e){
                                    Log.d("ERROR",e.getMessage());
                                }
                            }
                        }
                    }
                });
        cirToolbarProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Menu two clicked", Toast.LENGTH_SHORT).show();
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {

        int itemID = item.getItemId();
        if (itemID == R.id.menu_one){
            Toast.makeText(getContext(), "Menu one", Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            replace(new ProfileFragment());
        }
        switch (item.getItemId()){
            case R.id.menu_one:
                Toast.makeText(getContext(), "Menu one", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_two:
                replace(new ProfileFragment());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void Event() {

        edtSearchHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });


        imgHomeCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CartActivity.class);
                startActivity(intent);
            }
        });

        swipeHome.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeHome.setRefreshing(false);
                        LoadFavorite();
                    }
                }, 1000);

            }
        });

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

    // Firestore Snapshot Listener để cập nhật liên tục
    private void listenToCartChanges() {
        firestore.collection("GioHang")
                .document(userId)
                .collection("ALL")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("CartError", "Không thể lắng nghe thay đổi giỏ hàng", e);
                        return;
                    }
                    int totalQuantity = 0;
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Long quantity = doc.getLong("soluong");
                            if (quantity != null) {
                                totalQuantity += quantity;
                            }
                        }
                    }
                    updateCartCount(totalQuantity); // Cập nhật số lượng
                });
    }





    private void loadCartCount() {
        // Truy cập sub-collection "ALL" trong document của người dùng
        firestore.collection("GioHang")
                .document(userId)
                .collection("ALL")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalQuantity = 0; // Biến để lưu tổng số lượng sản phẩm
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Lấy số lượng từ trường "soluong"
                        Long quantity = doc.getLong("soluong");
                        if (quantity != null) {
                            totalQuantity += quantity; // Cộng dồn vào tổng
                        }
                    }
                    updateCartCount(totalQuantity); // Cập nhật số lượng lên giao diện
                })
                .addOnFailureListener(e -> {
                    Log.e("CartError", "Không thể tải dữ liệu giỏ hàng", e);
                });
    }

    /**
     * Hàm cập nhật số lượng giỏ hàng lên giao diện
     */
    private void updateCartCount(int totalQuantity) {
        tvNumberCart.setVisibility(View.VISIBLE); // Luôn hiển thị
        if (totalQuantity > 0) {
            tvNumberCart.setText(String.valueOf(totalQuantity)); // Cập nhật số lượng
        } else {
            tvNumberCart.setText("0"); // Hiển thị "0" nếu không có sản phẩm
        }
    }



    // Danh sách Product
    public  void  GetDataDSSanPham(){
        firestore.collection("SanPham").
                whereEqualTo("type",1).
                get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()>0){
                            for(QueryDocumentSnapshot d : queryDocumentSnapshots){
                                arr_ds_sp.add(new Product(d.getId(),d.getString("tensp"),
                                        d.getLong("giatien"),d.getString("hinhanh"),
                                        d.getString("loaisp"),d.getString("mota"),
                                        d.getLong("soluong"),d.getString("size"),
                                        d.getLong("type"),d.getString("chatlieu")));
                            }
                            productDSAdapter = new ProductAdapter(getContext(), arr_ds_sp, 1, new IClickOpenBottomSheet() {
                                @Override
                                public void onClickOpenBottomSheet(int position) {

                                    // Do something
                                    product = arr_ds_sp.get(position);
                                    TruyenData();
                                }
                            });
                            rcvDSSP.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
                            rcvDSSP.setAdapter(productDSAdapter);
                        }

                    }
                });
    }


    // Sản phẩm nổi bật
    public  void  GetDataSPNoiBat(){
        firestore.collection("SanPham").
                whereEqualTo("type",2).
                get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()>0){
                            for(QueryDocumentSnapshot d : queryDocumentSnapshots){
                                arr_sp_nb.add(new Product(d.getId(),d.getString("tensp"),
                                        d.getLong("giatien"),d.getString("hinhanh"),
                                        d.getString("loaisp"),d.getString("mota"),
                                        d.getLong("soluong"),d.getString("size"),
                                        d.getLong("type"),d.getString("chatlieu")));
                            }
                            productNBAdapter = new ProductAdapter(getContext(), arr_sp_nb, 2, new IClickOpenBottomSheet() {
                                @Override
                                public void onClickOpenBottomSheet(int position) {
                                    // Do something
                                    product = arr_sp_nb.get(position);
                                    TruyenData();
                                }
                            });
                            rcvSPNoiBat.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
                            rcvSPNoiBat.setAdapter(productNBAdapter);
                        }

                    }
                });
    }

    public  void  GetDataSPDoUong(){
        firestore.collection("SanPham").
                whereEqualTo("type",3).
                get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()>0){
                            for(QueryDocumentSnapshot d : queryDocumentSnapshots){
                                // lấy id trên firebase
                                arr_sp_du.add(new Product(d.getId(),d.getString("tensp"),
                                        d.getLong("giatien"),d.getString("hinhanh"),
                                        d.getString("loaisp"),d.getString("mota"),
                                        d.getLong("soluong"),d.getString("size"),
                                        d.getLong("type"),d.getString("chatlieu")));
                            }
                            productDUAdapter = new ProductAdapter(getContext(), arr_sp_du, 3, new IClickOpenBottomSheet() {
                                @Override
                                public void onClickOpenBottomSheet(int position) {

                                    // Do something
                                    product = arr_sp_du.get(position);
                                    TruyenData();
                                }
                            });
                            rcvSPDoUong.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
                            rcvSPDoUong.setAdapter(productDUAdapter);
                        }

                    }
                });
    }


    public  void  GetDataSPHanQuoc(){
        firestore.collection("SanPham").
                whereEqualTo("type",4).
                get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()>0){
                            for(QueryDocumentSnapshot d : queryDocumentSnapshots){
                                // lấy id trên firebase
                                arr_sp_hq.add(new Product(d.getId(),d.getString("tensp"),
                                        d.getLong("giatien"),d.getString("hinhanh"),
                                        d.getString("loaisp"),d.getString("mota"),
                                        d.getLong("soluong"),d.getString("size"),
                                        d.getLong("type"),d.getString("chatlieu")));
                            }
                            productHQAdapter = new ProductAdapter(getContext(), arr_sp_hq, 4, new IClickOpenBottomSheet() {
                                @Override
                                public void onClickOpenBottomSheet(int position) {

                                    // Do something
                                    product = arr_sp_hq.get(position);
                                    TruyenData();
                                }
                            });
                            rcvSPHQ.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
                            rcvSPHQ.setAdapter(productHQAdapter);
                        }

                    }
                });
    }

    public  void  GetDataSPMiCay(){
        firestore.collection("SanPham").
                whereEqualTo("type",5).
                get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()>0){
                            for(QueryDocumentSnapshot d : queryDocumentSnapshots){
                                arr_sp_mc.add(new Product(d.getId(),d.getString("tensp"),
                                        d.getLong("giatien"),d.getString("hinhanh"),
                                        d.getString("loaisp"),d.getString("mota"),
                                        d.getLong("soluong"),d.getString("size"),
                                        d.getLong("type"),d.getString("chatlieu")));
                            }
                            productMCAdapter = new ProductAdapter(getContext(), arr_sp_mc, 5, new IClickOpenBottomSheet() {
                                @Override
                                public void onClickOpenBottomSheet(int position) {

                                    // Do something
                                    product = arr_sp_mc.get(position);
                                    TruyenData();
                                }
                            });
                            rcvSPMC.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
                            rcvSPMC.setAdapter(productMCAdapter);
                        }

                    }
                });
    }
    // Sản phẩm Yêu thích
    public  void  GetDataSPYeuThich(){
        firestore.collection("SanPham").
                whereEqualTo("type",6).
                get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()>0){
                            for(QueryDocumentSnapshot d : queryDocumentSnapshots){
                                // lấy id trên firebase
                                arr_sp_yt.add(new Product(d.getId(),d.getString("tensp"),
                                        d.getLong("giatien"),d.getString("hinhanh"),
                                        d.getString("loaisp"),d.getString("mota"),
                                        d.getLong("soluong"),d.getString("size"),
                                        d.getLong("type"),d.getString("chatlieu")));
                            }
                            productYTAdapter = new ProductAdapter(getContext(), arr_sp_yt, 6, new IClickOpenBottomSheet() {
                                @Override
                                public void onClickOpenBottomSheet(int position) {
                                    // Do something
                                    product = arr_sp_yt.get(position);
                                    TruyenData();
                                }
                            });
                            rcvSPYT.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
                            rcvSPYT.setAdapter(productYTAdapter);
                        }

                    }
                });
    }

    public  void  GetDataSPLau(){
        firestore.collection("SanPham").
                whereEqualTo("type",7).
                get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()>0){
                            for(QueryDocumentSnapshot d : queryDocumentSnapshots){
                                // lấy id trên firebase
                                arr_sp_lau.add(new Product(d.getId(),d.getString("tensp"),
                                        d.getLong("giatien"),d.getString("hinhanh"),
                                        d.getString("loaisp"),d.getString("mota"),
                                        d.getLong("soluong"),d.getString("size"),
                                        d.getLong("type"),d.getString("chatlieu")));
                            }
                            productLauAdapter = new ProductAdapter(getContext(), arr_sp_lau, 7, new IClickOpenBottomSheet() {
                                @Override
                                public void onClickOpenBottomSheet(int position) {

                                    // Do something
                                    product = arr_sp_lau.get(position);
                                    TruyenData();
                                }
                            });
                            rcvSPLau.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
                            rcvSPLau.setAdapter(productLauAdapter);
                        }

                    }
                });
    }
    // Sản phẩm Gợi ý
    public  void  GetDataSPGoiY(){
        firestore.collection("SanPham").
                whereEqualTo("type",8).
                get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()>0){
                            for(QueryDocumentSnapshot d : queryDocumentSnapshots){
                                // lấy id trên firebase
                                arr_sp_gy.add(new Product(d.getId(),d.getString("tensp"),
                                        d.getLong("giatien"),d.getString("hinhanh"),
                                        d.getString("loaisp"),d.getString("mota"),
                                        d.getLong("soluong"),d.getString("size"),
                                        d.getLong("type"),d.getString("chatlieu")));
                            }
                            productGYAdapter = new ProductAdapter(getContext(), arr_sp_gy, 8, new IClickOpenBottomSheet() {
                                @Override
                                public void onClickOpenBottomSheet(int position) {

                                    // Do something
                                    product = arr_sp_gy.get(position);
                                    TruyenData();
                                }
                            });
                            rcvSPGY.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
                            rcvSPGY.setAdapter(productGYAdapter);
                        }

                    }
                });
    }


    private void Banner() {
        arrayList = new ArrayList<>();
        firestore= FirebaseFirestore.getInstance();
        firestore.collection("Banner").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot d : queryDocumentSnapshots){
                    arrayList.add(d.getString("hinhanh"));
                }
                bannerAdapter = new BannerAdapter(getContext(), arrayList, new IClickCTHD() {
                    @Override
                    public void onClickCTHD(int pos) {

                        Intent intent = new Intent(getContext(), SearchActivity.class);
                        startActivity(intent);
                    }
                });
                viewPager.setAdapter(bannerAdapter);
                circleIndicator.setViewPager(viewPager);
                bannerAdapter.registerDataSetObserver(circleIndicator.getDataSetObserver());
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    //3s sang 1 banner khác
                    public void run() {
                        int k=viewPager.getCurrentItem();
                        if(k>=arrayList.size()-1){
                            k  = 0;
                        }else{
                            k++;
                        }
                        handler.postDelayed(this,3000);
                        viewPager.setCurrentItem(k,true);

                    }
                },3000);

            }
        });
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

    private void InitWidget() {
        imgHomeCart = view.findViewById(R.id.img_home_cart);
        swipeHome = view.findViewById(R.id.swipe_home);
        tvNumberCart = view.findViewById(R.id.tv_number_cart);
        edtSearchHome = view.findViewById(R.id.edt_search_home);

        toolbarHome = view.findViewById(R.id.toolbar_home);
        cirAvatarHome = view.findViewById(R.id.cir_avatar_home);
        tvNameHome = view.findViewById(R.id.tv_name_home);
        tvEmailHome = view.findViewById(R.id.tv_email_home);
        viewPager = view.findViewById(R.id.viewpager);
        circleIndicator = view.findViewById(R.id.circle_indicator);

        rcvLoaiProduct = view.findViewById(R.id.rcv_loai_product);
        loaiProductAdapter = new LoaiProductAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rcvLoaiProduct.setLayoutManager(linearLayoutManager);
        getListLoaiProduct();
        rcvDSSP = view.findViewById(R.id.rcv_ds_sanpham);
        rcvSPNoiBat = view.findViewById(R.id.rcv_sp_noibat);
        rcvSPDoUong = view.findViewById(R.id.rcv_sp_douong);
        rcvSPHQ = view.findViewById(R.id.rcv_sp_thoitrang1);
        rcvSPMC = view.findViewById(R.id.rcv_sp_micay);
        rcvSPYT = view.findViewById(R.id.rcv_sp_yeuthich);
        rcvSPLau = view.findViewById(R.id.rcv_sp_lau);
        rcvSPGY = view.findViewById(R.id.rcv_sp_goiy);
        // Lấy ID người dùng hiện tại từ Firebase Authentication
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    private List<LoaiProduct> getListLoaiProduct() {
        mlistproduct = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        firestore.collection("LoaiProduct").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot d : queryDocumentSnapshots) {
                        LoaiProduct loaiProduct = new LoaiProduct(
                                d.getString("tenloai"),
                                d.getString("hinhanhloai")
                        );
                        mlistproduct.add(loaiProduct);
                    }
                    // Gán dữ liệu và cập nhật adapter
                    loaiProductAdapter.setData(mlistproduct, new IClickLoaiProduct() {
                        @Override
                        public void onClickItemLoaiProduct(int position) {
                            Intent intent = new Intent(getContext(), CategoryActivity.class);
                            intent.putExtra("loaiproduct", mlistproduct.get(position).getTenloai());
                            startActivity(intent);
                        }
                    });
                    rcvLoaiProduct.setAdapter(loaiProductAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("error", "Lỗi: " + e.getMessage());
                Toast.makeText(getContext(), "Lỗi khi tải loại sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mlistproduct;
    }


    private void TruyenData(){
        Intent intent = new Intent(getContext(), DetailSPActivity.class);
        intent.putExtra("search", product);
        startActivity(intent);
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
}