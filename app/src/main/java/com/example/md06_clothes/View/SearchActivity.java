package com.example.md06_clothes.View;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.md06_clothes.Adapter.LichSuSearchAdapter;
import com.example.md06_clothes.Adapter.SearchAdapter;
import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.Presenter.ProductPresenter;
import com.example.md06_clothes.Presenter.StoryPresenter;
import com.example.md06_clothes.R;
import com.example.md06_clothes.my_interface.IClickCTHD;
import com.example.md06_clothes.my_interface.ProductView;
import com.example.md06_clothes.my_interface.StoryView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity implements ProductView, StoryView {

    private SwipeRefreshLayout swipeSearch;
    private ImageView imgMic, imgQRCode;
    private ImageView imgBackSearch;
    private SearchView searchView;
    private RecyclerView rcvSearch, rcvLichSuSearch;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ArrayList<Product> mlistsearch;
    private SearchAdapter adapter;

    private ProductPresenter productPresenter;
    private StoryPresenter storyPresenter;

    private LichSuSearchAdapter lichSuSearchAdapter;
    private ArrayList<String> mlistStory;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    // AutoComplete Text
    private ArrayList<Product> mlistAuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        InitWidget();
        Event();
    }

    private void Event() {
        imgBackSearch.setOnClickListener(view -> finish());

        imgMic.setOnClickListener(view -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy thử nói gì đó");

            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
            } catch (Exception e) {
                Toast.makeText(SearchActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        imgQRCode.setOnClickListener(view -> {
            ScanOptions scanOptions = new ScanOptions();
            scanOptions.setPrompt("Đưa mã QR của bạn vào máy ảnh");
            scanOptions.setBeepEnabled(true);
            scanOptions.setOrientationLocked(true);
            scanOptions.setCaptureActivity(CaptureAct.class);
            barLauncher.launch(scanOptions);
        });

        swipeSearch.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            mlistStory.clear();
            storyPresenter.HandleGetStory(FirebaseAuth.getInstance().getCurrentUser().getUid());
            lichSuSearchAdapter.setdata(SearchActivity.this, mlistStory, pos -> {
                String s = mlistStory.get(pos);
                searchView.setQuery(s, false);
            });
            swipeSearch.setRefreshing(false);
        }, 500));
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            try {
                for (Product product : mlistsearch) {
                    if (result.getContents().equals(product.getId())) {
                        Intent intent = new Intent(SearchActivity.this, DetailSPActivity.class);
                        intent.putExtra("search", product);
                        startActivity(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Hủy quét mã", Toast.LENGTH_SHORT).show();
        }
    });

    private void StorySearch(String text) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("noidungtimkiem", text);
        firestore.collection("LichSuTimKiem").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Story").add(hashMap);
    }

    private void InitWidget() {
        swipeSearch = findViewById(R.id.swipe_search);
        imgQRCode = findViewById(R.id.img_qrcode);
        imgMic = findViewById(R.id.img_mic);
        imgBackSearch = findViewById(R.id.img_back_search);
        searchView = findViewById(R.id.search_view);
        rcvSearch = findViewById(R.id.rcv_search_monan);
        rcvLichSuSearch = findViewById(R.id.rcv_lichsu);

        productPresenter = new ProductPresenter(this);
        storyPresenter = new StoryPresenter(this);
        mlistsearch = new ArrayList<>();
        mlistStory = new ArrayList<>();
        mlistAuto = new ArrayList<>();

        // Khởi tạo adapter để tránh lỗi null
        lichSuSearchAdapter = new LichSuSearchAdapter();

        rcvSearch.setVisibility(View.GONE);

        productPresenter.HandleGetDataProduct();
        storyPresenter.HandleGetStory(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    public void OnSucess() {

    }

    @Override
    public void OnFail() {

    }

    @Override
    public void getDataProduct(String id, String ten, Long gia, String hinhanh, String loaisp, String mota, Long soluong, String size, Long type, String chatlieu) {
        mlistsearch.add(new Product(id, ten, gia, hinhanh, loaisp, mota, soluong, size, type, chatlieu));
        mlistAuto.add(new Product(ten));
        adapter = new SearchAdapter(SearchActivity.this, mlistsearch, pos -> {
            Product product = mlistsearch.get(pos);
            Intent intent = new Intent(SearchActivity.this, DetailSPActivity.class);
            intent.putExtra("search", product);
            startActivity(intent);
        });
        rcvSearch.setLayoutManager(new LinearLayoutManager(SearchActivity.this, RecyclerView.VERTICAL, false));
        rcvSearch.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                StorySearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                rcvSearch.setVisibility(View.VISIBLE);
                adapter.filter(newText);
                return true;
            }
        });
    }

    @Override
    public void getDataStory(String noidung) {
        mlistStory.add(noidung);
        lichSuSearchAdapter.setdata(SearchActivity.this, mlistStory, pos -> {
            String s = mlistStory.get(pos);
            searchView.setQuery(s, false);
        });
        GridLayoutManager manager = new GridLayoutManager(SearchActivity.this, 6);
        rcvLichSuSearch.setLayoutManager(manager);
        rcvLichSuSearch.setAdapter(lichSuSearchAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            searchView.setQuery(result.get(0), false);
        }
    }
}
