package com.example.md06_clothes.View;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.example.md06_clothes.Models.SizeQuantity;
import com.example.md06_clothes.Presenter.ProductPresenter;
import com.example.md06_clothes.Presenter.StoryPresenter;
import com.example.md06_clothes.R;
import com.example.md06_clothes.my_interface.ProductView;
import com.example.md06_clothes.my_interface.StoryView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity implements ProductView, StoryView {

    // Các thành phần giao diện
    private SwipeRefreshLayout swipeSearch; // kéo để làm mới
    private ImageView imgMic; // tìm kiếm bằng giọng nói
    private ImageView imgBackSearch;
    private SearchView searchView;
    private RecyclerView rcvSearch, rcvLichSuSearch; // RecyclerView sản phẩm , lịch sử tìm kiếm

    // Firebase Firestore để lưu và truy xuất dữ liệu
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Dssp và lịch sử tìm kiếm
    private ArrayList<Product> mlistsearch;
    private SearchAdapter adapter; // Adapter cho RecyclerView hiển thị dssp
    private ArrayList<String> mlistStory;
    private LichSuSearchAdapter lichSuSearchAdapter; // Adapter cho lịch sử tìm kiếm

    private ProductPresenter productPresenter; // Presenter lấy danh sách sản phẩm
    private StoryPresenter storyPresenter; // Presenter lấy lịch sử tìm kiếm
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000; // Mã yêu cầu nhận diện giọng nói
    // REQUEST_CODE_SPEECH_INPUT được sử dụng để xác định mục đích của yêu cầu (Intent) khi ứng dụng nhận được kết quả từ onActivityResult.
    // Đây là cách để phân biệt giữa nhiều yêu cầu khác nhau mà một Activity có thể gửi đi.
    // Giá trị 1000 là một số nguyên ngẫu nhiên, không có ý nghĩa đặc biệt có thể thay thế bằng bất kỳ số nào khác (ví dụ, 1, 2000, v.v.), miễn là bạn nhất quán khi sử dụng trong mã.

    // Danh sách tự động hoàn thành
    private ArrayList<Product> mlistAuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Khởi tạo
        InitWidget();

        // xóa dữ liệu search trùng
        removeDuplicateKeywords();

        // Gán sự kiện
        Event();
    }

    // Xử lý các sự kiện trong giao diện
    private void Event() {
        imgBackSearch.setOnClickListener(view -> finish());

        // Xử lý sự kiện khi nhấn vào nút mic (tìm kiếm bằng giọng nói)
        imgMic.setOnClickListener(view -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // Khởi động nhận diện giọng nói
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy thử nói gì đó");

            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT); // Mở giao diện nhận diện giọng nói
            } catch (Exception e) {
                Toast.makeText(SearchActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Làm mới lịch sử tìm kiếm khi người dùng kéo xuống
        swipeSearch.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            mlistStory.clear(); // Xóa danh sách lịch sử cũ
            storyPresenter.HandleGetStory(FirebaseAuth.getInstance().getCurrentUser().getUid()); // Lấy lại lịch sử từ Firestore
            lichSuSearchAdapter.setdata(SearchActivity.this, mlistStory, pos -> {
                String s = mlistStory.get(pos); // Tự động điền từ khóa vào thanh tìm kiếm khi chọn
                searchView.setQuery(s, false);
            });
            swipeSearch.setRefreshing(false); // Dừng hiệu ứng làm mới
        }, 1000));
    }

    // Lưu lịch sử tìm kiếm , lọc trùng từ khóa
    private void StorySearch(String text) {
        firestore.collection("LichSuTimKiem")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Story")
                .whereEqualTo("noidungtimkiem", text) // Kiểm tra xem từ khóa đã tồn tại chưa
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Nếu từ khóa chưa tồn tại, thêm vào Firestore
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("noidungtimkiem", text);
                        firestore.collection("LichSuTimKiem")
                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .collection("Story")
                                .add(hashMap);
                    }
                })
                .addOnFailureListener(e ->
                        System.err.println("Lỗi khi kiểm tra từ khóa: " + e.getMessage()));
    }
    // Lấy tất cả các tài liệu trong collection con Story.
    // Sử dụng HashSet để lưu các từ khóa duy nhất (không cho phép trùng lặp).
    // Đánh dấu các tài liệu có từ khóa bị trùng bằng cách thêm ID của chúng vào danh sách duplicateIds.
    // Xóa các tài liệu có ID nằm trong danh sách duplicateIds.
    private void removeDuplicateKeywords() {
        firestore.collection("LichSuTimKiem")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Story")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    HashSet<String> uniqueKeywords = new HashSet<>(); // Lưu trữ các từ khóa duy nhất
                    ArrayList<String> duplicateIds = new ArrayList<>(); // Lưu trữ ID của các tài liệu trùng lặp

                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        String keyword = snapshot.getString("noidungtimkiem");
                        if (!uniqueKeywords.add(keyword)) {
                            // Nếu từ khóa đã tồn tại, thêm ID vào danh sách cần xóa
                            duplicateIds.add(snapshot.getId());
                        }
                    }

                    // Xóa các tài liệu bị trùng lặp
                    for (String id : duplicateIds) {
                        firestore.collection("LichSuTimKiem")
                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .collection("Story")
                                .document(id)
                                .delete()
                                .addOnSuccessListener(unused ->
                                        System.out.println("Xóa từ khóa trùng lặp thành công: " + id))
                                .addOnFailureListener(e ->
                                        System.err.println("Lỗi khi xóa từ khóa trùng lặp: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e ->
                        System.err.println("Lỗi khi lấy dữ liệu từ Firestore: " + e.getMessage()));
    }



    // Khởi tạo các thành phần giao diện và biến
    private void InitWidget() {
        swipeSearch = findViewById(R.id.swipe_search); // Layout làm mới
        imgMic = findViewById(R.id.img_mic); // Icon mic
        imgBackSearch = findViewById(R.id.img_back_search); // Nút quay lại
        searchView = findViewById(R.id.search_view); // Thanh tìm kiếm
        rcvSearch = findViewById(R.id.rcv_search_monan); // RecyclerView danh sách sản phẩm
        rcvLichSuSearch = findViewById(R.id.rcv_lichsu); // RecyclerView lịch sử tìm kiếm

        productPresenter = new ProductPresenter(this); // Presenter quản lý sản phẩm
        storyPresenter = new StoryPresenter(this); // Presenter quản lý lịch sử tìm kiếm

        mlistsearch = new ArrayList<>();
        mlistStory = new ArrayList<>();
        mlistAuto = new ArrayList<>();

        lichSuSearchAdapter = new LichSuSearchAdapter(); // Adapter lịch sử tìm kiếm

        rcvSearch.setVisibility(View.GONE); // Ẩn danh sách sản phẩm ban đầu

        productPresenter.HandleGetDataProduct(); // Lấy dữ liệu sản phẩm từ Firestore
        storyPresenter.HandleGetStory(FirebaseAuth.getInstance().getCurrentUser().getUid()); // Lấy lịch sử tìm kiếm từ Firestore
    }

    @Override
    public void OnSucess() {
    }

    @Override
    public void OnFail() {
    }

    @Override
    public void getDataProduct(String id, String ten, Long gia, String hinhanh, String loaisp, String mota, List<SizeQuantity> sizes, Long type, String chatlieu) {
        mlistsearch.add(new Product(id, ten, gia, hinhanh, loaisp, mota, sizes, type, chatlieu)); // Thêm sản phẩm vào danh sách
        mlistAuto.add(new Product(ten)); // Thêm sản phẩm vào danh sách tự động hoàn thành

        // Khởi tạo adapter và gán cho RecyclerView
        adapter = new SearchAdapter(SearchActivity.this, mlistsearch, pos -> {
            Product product = mlistsearch.get(pos); // Lấy sản phẩm được chọn
            Intent intent = new Intent(SearchActivity.this, DetailSPActivity.class); // Chuyển sang màn hình chi tiết
            intent.putExtra("search", product);
            startActivity(intent);
        });

        rcvSearch.setLayoutManager(new LinearLayoutManager(SearchActivity.this, RecyclerView.VERTICAL, false));
        rcvSearch.setAdapter(adapter);

        // Lắng nghe sự kiện trên thanh tìm kiếm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                StorySearch(query); // Lưu lịch sử tìm kiếm khi người dùng nhấn Enter
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                rcvSearch.setVisibility(View.VISIBLE); // Hiển thị danh sách sản phẩm
                adapter.filter(newText); // Lọc danh sách sản phẩm theo từ khóa
                return true;
            }
        });
    }




    // Nhận dữ liệu lịch sử tìm kiếm từ Firestore, Lọc trùng khi hiển thị danh sách lịch sử tìm kiếm
    @Override
    public void getDataStory(String noidung) {
        if (!mlistStory.contains(noidung)) { // Chỉ thêm từ khóa nếu chưa tồn tại trong danh sách
            mlistStory.add(noidung);
        }
        lichSuSearchAdapter.setdata(SearchActivity.this, mlistStory, pos -> {
            String s = mlistStory.get(pos); // Lấy từ khóa được chọn
            searchView.setQuery(s, false); // Tự động điền vào thanh tìm kiếm
        });

        // Hiển thị lịch sử tìm kiếm dưới dạng lưới
        GridLayoutManager manager = new GridLayoutManager(SearchActivity.this, 6);
        rcvLichSuSearch.setLayoutManager(manager);
        rcvLichSuSearch.setAdapter(lichSuSearchAdapter);
    }


    // Xử lý kết quả trả về từ mic (giọng nói)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); // Lấy kết quả giọng nói
            searchView.setQuery(result.get(0), false); // Tự động điền kết quả vào thanh tìm kiếm
        }
    }
}
